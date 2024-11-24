package bfst22.vector;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipInputStream;
import javax.xml.stream.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

// Handles the logic of our data and storing it appropriately.
public class Model {

    // Declares and instantiates lines, containing all lines needed to be drawn.
    // Like HashMap, it has key (the enum waytype) and value (list of all lines w/ that waytype).
    public MapFeature yamlObj;
    public KdTree kdtree;
    public NNKdTree NNRoutetree;
    public TernarySearchTree searchTree;
    public float[] minBoundsPos, maxBoundsPos, originBoundsPos; // lat, lon
    public int nodecount, waycount, relcount;
    public String currFileName;
    public long loadTime, filesize;
	public VehicleType vehicleType;
    public Graph graph;
    public DijkstraSP dijkstraSP;
    public Distance distance;
    public String address;


    // Loads our OSM file, supporting various formats: .zip and .osm, then convert it into an .obj.
    public void load(String filename) throws IOException, XMLStreamException, FactoryConfigurationError, ClassNotFoundException {
        this.currFileName = filename;

        switch (filename.substring(filename.lastIndexOf('.') + 1)) {
            case "zip" -> {
                ZipInputStream zip = new ZipInputStream(new FileInputStream(filename));
                zip.getNextEntry();
                this.loadOSM(zip);
                this.saveObj(filename);
            } case "osm" -> {
                this.loadOSM(new FileInputStream(filename));
                this.saveObj(filename);
            } case "obj" -> {
                this.loadTime = System.nanoTime();
                try (ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)))) {
                    this.minBoundsPos = new float[]{input.readFloat(), input.readFloat()};
                    this.maxBoundsPos = new float[]{input.readFloat(), input.readFloat()};
                    this.originBoundsPos = new float[]{input.readFloat(), input.readFloat()};
                    this.nodecount = input.readInt();
                    this.waycount = input.readInt();
                    this.relcount = input.readInt();
                    this.kdtree = (KdTree) input.readObject();
                    this.searchTree = (TernarySearchTree) input.readObject();
                    this.yamlObj = (MapFeature) input.readObject();
                }
                this.loadTime = System.nanoTime() - this.loadTime;
                this.filesize = Files.size(Paths.get(this.currFileName));
            }
        }
    }

    public void unload(){
        this.kdtree = null;
        this.NNRoutetree = null;
        this.searchTree = null;
        this.yamlObj = null;
        this.minBoundsPos = this.maxBoundsPos = this.originBoundsPos = new float[]{0,0};
        this.nodecount = this.waycount = this.relcount = 0;
        this.currFileName = "<No File Loaded>";
        this.loadTime = this.filesize = 0;
    }

    public boolean isLoaded(){
        return this.kdtree != null;
    }

    // Saves the .obj file in our project hierachy.
    private void saveObj(String basename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(basename + ".obj"))) {
            out.writeDouble(this.minBoundsPos[0]);
            out.writeDouble(this.minBoundsPos[1]);
            out.writeDouble(this.maxBoundsPos[0]);
            out.writeDouble(this.maxBoundsPos[1]);
            out.writeDouble(this.originBoundsPos[0]);
            out.writeDouble(this.originBoundsPos[1]);
            out.writeInt(nodecount);
            out.writeInt(waycount);
            out.writeInt(relcount);
            out.writeObject(kdtree);
            out.writeObject(searchTree);
            out.writeObject(yamlObj);
        }
    }


    // Parses and reads the loaded .osm file, interpreting the data however it is configured.
    private void loadOSM(InputStream input) throws XMLStreamException, FactoryConfigurationError, IOException {
        this.loadTime = System.nanoTime();
        this.filesize = Files.size(Paths.get(this.currFileName));
        this.yamlObj = new Yaml(new Constructor(MapFeature.class)).load(this.getClass().getResourceAsStream("WayConfig.yaml"));
        this.kdtree = new KdTree();
        this.NNRoutetree = new NNKdTree();
        this.searchTree = new TernarySearchTree();

        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new BufferedInputStream(input)); // Reads the .osm file, being an XML file.
        NodeMap id2node = new NodeMap(); // Converts IDs into nodes (uncertain about this).
        Map<Long, Drawable> id2way = new HashMap<>(); // Saves the ID of a particular way (Long) and stores the way as a value (OSMWay).
        List<PolyPoint> nodes = new ArrayList<>(); // A list of nodes drawing a particular element of map. Is cleared when fully drawn.
        List<Drawable> rel = new ArrayList<>(); // Saves all relations.
        long relID = 0; // ID of the current relation.
        String keyFeature = null, valueFeature = null;
        boolean isMultiPoly = false, deleted = false;
        List<String> highwayTypes = new ArrayList<>(Arrays.asList("primary", "secondary", "tertiary", "residential","motorway","trunk","pedestrian"));
        graph = new Graph();
        boolean isHighway = false;
        boolean motorVehicle = true;
        boolean bicycle = false;
        boolean foot = false;
        boolean isOneWay = false;
        int HwyCount = 0;
        int speedlimit = 50; //Speed limit in towns
        Map<Integer, List<PolyPoint>> index2way = new HashMap<>();

        // Reads the entire .OSM file.
        while (reader.hasNext()) {
            int element = reader.next();

            if (element == XMLStreamConstants.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "bounds" -> { // Configures the longitude and latitude. An element present in all OSM files. Uncertain as to why, though adjusting the floats will make the map not draw.
                        float maxlat = -Float.parseFloat(reader.getAttributeValue(null, "minlat"));
                        float minlon = 0.56f * Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                        float minlat = -Float.parseFloat(reader.getAttributeValue(null, "maxlat"));
                        float maxlon = 0.56f * Float.parseFloat(reader.getAttributeValue(null, "maxlon"));

                        this.minBoundsPos = new float[]{minlat, minlon};
                        this.maxBoundsPos = new float[]{maxlat, maxlon};
                        this.originBoundsPos = new float[]{(maxlon + minlon) / 2, (maxlat + minlat) / 2};
                    } case "node" -> { // Parses information from a node, adding it to the id2node list.
                        long id = Long.parseLong(reader.getAttributeValue(null, "id"));
                        float lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                        float lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                        PolyPoint point = new PolyPoint(id, 0.56f * lon, -lat);
                        id2node.add(point);
                        id2way.put(relID,point);
                        this.searchTree.setAddressPos(0.56f * lon, -lat);
                        this.nodecount++;

                    } case "nd" -> { // parses reference to a node (ID) and adds it to the node list.
                        long ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                        nodes.add(id2node.get(ref));
                    } case "relation", "way" -> { // Parses the ID of the way and sets a default type. For future reference, type could probably be configured properly in this step.
                        relID = Long.parseLong(reader.getAttributeValue(null, "id"));
                    }
                    case "tag" -> { // Parses the key and value of tags, changing the waytype to the corresponding type.
                        String k = reader.getAttributeValue(null, "k");
                        String v = reader.getAttributeValue(null, "v");
                        isMultiPoly = (k.equals("type") && v.equals("multipolygon") || isMultiPoly);
                        if(k.contains("deleted:")) deleted = true;
                        if (k.contains("addr:")) {
                            switch (k) {
                                case "addr:city" -> searchTree.addAddressElement("city",v);
                                case "addr:housenumber" -> searchTree.addAddressElement("house",v);
                                case "addr:postcode" -> searchTree.addAddressElement("postcode",v);
                                case "addr:street" -> searchTree.addAddressElement("street", v);
                            }
                        }

                            if(k.equals("bicycle") && v.equals("yes")) bicycle = true;

                            if(k.equals("foot") && v.equals("yes")) foot = true;

                            if(k.equals("motorcar") && v.equals("yes")) motorVehicle = false;

                            if(k.equals("name"))  this.address = v;

                            if(k.equals("maxspeed")) {
                                try {
                                    speedlimit = Integer.parseInt(v);
                                }catch (NumberFormatException e){
                                    speedlimit = 50;
                                }
                            }
                            if(k.equals("oneway")) isOneWay = true;

                            if (this.yamlObj.keyfeatures.containsKey(k)) {
                            keyFeature = k;
                            valueFeature = v;
                            isHighway = false;

                                if (k.equals("highway")) {
                                    if (highwayTypes.contains(valueFeature)) {
                                    isHighway = true;
                                    graph.add(nodes);
                                }
                            }
                        }
                    } case "member" -> { // parses a member (a reference to a way belonging to a collection of ways; relations)
                        Drawable elm = id2way.get(Long.parseLong(reader.getAttributeValue(null, "ref")));

                        if (elm != null){
                            switch (reader.getAttributeValue(null, "role")){
                                case "outer" -> rel.add(new PolyGon((PolyLine) elm.clone(),false));
                                case "inner" -> rel.add(new PolyGon((PolyLine) elm.clone(),true));
                                default -> rel.add(elm.clone());
                            }
                        }
                    }
                }
            } else if(element == XMLStreamConstants.END_ELEMENT){
                if(deleted){
                    nodes.clear();
                    rel.clear();
                    isMultiPoly = deleted = false;
                    continue;
                }
                switch (reader.getLocalName()) {
                    case "node" -> searchTree.insertAddress();
					case "way" -> { // "way" - All lines in the program; linking point A to B
                        PolyLine way = new PolyLine(nodes);
                        this.kdtree.add(way,way);
                        this.yamlObj.addDrawable(keyFeature,valueFeature,way);
                        this.waycount++;
                        if (isHighway) {
                            index2way.put(HwyCount, new LinkedList<>());
                            for (PolyPoint p : nodes){
                                p.address = address;
                                p.foot = foot;
                                p.bicycle = bicycle;
                                p.motorVehicle = motorVehicle;
                                p.isOneway = isOneWay;
                                p.speedLimit = speedlimit;
                                index2way.get(HwyCount).add(p);
                            }
                            HwyCount++;

                            address = null;
                            foot = false;
                            bicycle = false;
                            motorVehicle = true;
                            isOneWay = false;
                            speedlimit = 50;
                        }
                        keyFeature = valueFeature = null;
                        id2way.put(relID, way);
                        nodes.clear();
                    } case "relation" -> { // is a collection of ways and has to be drawn separately with MultiPolygon.
                        PolyRelation multipoly = new PolyRelation(rel,isMultiPoly);
                        this.kdtree.add(multipoly,multipoly);
                        this.yamlObj.addDrawable(keyFeature,valueFeature,multipoly);
                        this.relcount++;

                        keyFeature = valueFeature = null;
                        isMultiPoly = false;
                        rel.clear();
                    }
                }
            }
        }

        this.kdtree.generateTree();
        this.kdtree.generateSplits();
        this.searchTree.generate();
        this.loadTime = System.nanoTime() - this.loadTime;

        this.graph.generate();
        for(int i = 0; i < index2way.size() - 1; i++){
            for(int j = 0; j < index2way.get(i).size() - 1; j++){
                this.graph.addEdge(index2way.get(i).get(j),index2way.get(i).get(j+1), index2way.get(i).get(j).speedLimit);
                if(!index2way.get(i).get(j).isOneway){
                    this.graph.addEdge(index2way.get(i).get(j+1),index2way.get(i).get(j), index2way.get(i).get(j+1).speedLimit);
                }
            }
        }

        for(Edge f : this.graph.edges()){
            List<PolyPoint> edgeList = new ArrayList<>();
            edgeList.add(f.getFrom());
            edgeList.add(f.getTo());
            this.NNRoutetree.add(new PolyLine(edgeList),f);
            edgeList.clear();
        }
        this.NNRoutetree.generateTree();
        this.NNRoutetree.generateSplits();
    }
}