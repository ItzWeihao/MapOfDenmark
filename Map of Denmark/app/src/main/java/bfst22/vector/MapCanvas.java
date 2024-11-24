package bfst22.vector;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;
import java.util.*;

// defines the canvas of our map; panning, zooming, painting etc.
// Whenever we add new interaction with the map, we use this class.
public class MapCanvas extends Canvas {
    private Model model;
    private Affine trans;
    private GraphicsContext gc;
    public float[] minPos, maxPos, originPos, mousePos, rtMousePos;
    public double zoom_current;
    public final int minZoom = 1, maxZoom = 100000;
    public boolean zoomMagnifyingGlass = false;
    public long repaintTime, avgRT, avgRTNum;
    public Painter painter;
    public ZoomBox zoombox;
    public PinPoints pinpoints;
    public DebugProperties deprop;
    public boolean drags;
    public String backgroundColor;

    /* ----------------------------------------------------------------------------------------------------------------- *
     * ------------------------------------------------ General Methods ------------------------------------------------ *
     * ----------------------------------------------------------------------------------------------------------------- */
    // Runs upon startup (setting default pan, zoom for example).
    public void init(final Model model) {
        this.model = model;
        this.backgroundColor = "#b5d2dd";
        this.deprop = new DebugProperties();
        this.reset();
        this.zoom(42000);
        this.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
            if(this.zoomMagnifyingGlass) event.consume();
        });
    }

    public void reset(){
        this.minPos = new float[]{0,0};
        this.maxPos = new float[]{0,0};
        this.originPos = new float[]{0,0};
        this.mousePos = new float[]{0,0};
        this.rtMousePos = new float[]{0,0};
        this.painter = new Painter();
        this.zoombox = new ZoomBox();
        this.pinpoints = new PinPoints();
        this.repaintTime = this.avgRT = this.avgRTNum = 0;
        this.trans = new Affine();
        this.gc = super.getGraphicsContext2D();
        this.gc.setFillRule(FillRule.NON_ZERO);
        this.zoom_current = 1;
        this.drags = false;
    }

    // https://stackoverflow.com/questions/12636613/how-to-calculate-moving-average-without-keeping-the-count-and-data-total
    private void calcRollingAvg(){
        this.avgRTNum = this.avgRTNum < 100 ? this.avgRTNum+1 : 1;
        this.avgRT = (this.avgRT * (this.avgRTNum-1) + this.repaintTime) / this.avgRTNum;
    }

    private void magnifyingGlass(MouseEvent e){
        if(this.zoomMagnifyingGlass){
            if(e.getButton() == MouseButton.PRIMARY) this.zoomTo(2);
            else if(e.getButton() == MouseButton.SECONDARY) this.zoomTo(0.5);
            this.goToPosAbsolute(this.mousePos);
        }
    }

    private void doDrag(boolean state){
        this.drags = this.pinpoints.drag(this.mousePos,this.zoom_current,state);
    }

    /* ----------------------------------------------------------------------------------------------------------------- *
     * ----------------------------------------------- Painting Methods ------------------------------------------------ *
     * ----------------------------------------------------------------------------------------------------------------- */
    // Draws all of the elements of our map.
    private void repaint() {
        this.gc.setTransform(new Affine());

        // Background color
        //this.gc.setFill(Color.web("#b5d2d"));
        this.gc.setFill(Color.web(backgroundColor));
        this.gc.fillRect(0, 0, super.getWidth(), super.getHeight());

        // Performs linear mapping between Point2D points. Our trans is Affine:
        // https://docs.oracle.com/javase/8/javafx/api/javafx/scene/transform/Affine.html
        this.gc.setTransform(this.trans);

        if(this.model.isLoaded()) {
            this.repaintTime = System.nanoTime();

            double padding = this.deprop.get("debugVisBox") ? 100 : -25;
            Set<Drawable> range = (Set<Drawable>)(Set<?>) this.model.kdtree.rangeSearch(new double[]{this.minPos[1] + this.z(padding), this.minPos[0] + this.z(padding)},
                    new double[]{this.maxPos[1] - this.z(padding), this.maxPos[0] - this.z(padding)});

            // Only display if set to do so, else display nothing at all
            if(!this.model.yamlObj.draw.hide || !this.model.yamlObj.draw.nodraw) {
                // Loops through all the key features and sets the default styling for all its objects
                for (Map.Entry<String, keyFeature> e1 : this.model.yamlObj.keyfeatures.entrySet()) {
                    keyFeature element = e1.getValue();
                    if (!element.draw.hide && !element.draw.nodraw) {
                        this.setStylingDefault();

                        // Loops through all value features and sets first eventual key feature styling and then eventual any value styles set
                        for (Map.Entry<String, valueFeature> e2 : element.valuefeatures.entrySet()) {
                            valueFeature element2 = e2.getValue();

                            if (!element2.draw.hide && !element2.draw.nodraw) {
                                for (Drawable draw : element2.drawable) {
                                    if (range.contains(draw) || Objects.requireNonNull(element2.draw).always_draw) {
                                        this.setStyling(element.draw);
                                        this.setStyling(element2.draw);

                                        if ((element.draw.fill && element.draw.zoom_level < this.zoom_current
                                          || element2.draw.fill && element2.draw.zoom_level < this.zoom_current)
                                                && !this.deprop.get("debugDisplayWireframe")) {
                                            draw.fill(this.gc);
                                        }
                                        if (element.draw.stroke && element.draw.zoom_level < this.zoom_current
                                         || element2.draw.stroke && element2.draw.zoom_level < this.zoom_current)
                                            draw.stroke(this.gc);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            this.repaintTime = System.nanoTime() - this.repaintTime;
            this.calcRollingAvg();
            this.painter.stroke(this.gc, this.mousePos, this.zoom_current);
            this.setStylingDefault();
            this.pinpoints.draw(this.gc,this.zoom_current,this.mousePos);
            this.splitsTree();
            this.drawGraph(model.graph);
            this.drawShortestPath(model.dijkstraSP);
            this.drawBounds();
            this.strokeCursor();
            this.strokeBox(padding);
        }
    }

    private void drawEdge(Edge e){
        this.gc.moveTo(e.getFrom().lat,e.getFrom().lon);
        this.gc.lineTo(e.getTo().lat,e.getTo().lon);
    }

    private void drawGraph(Graph graph)
    {
            this.gc.setStroke(Color.RED);
            this.gc.setLineWidth(0.000030);
            gc.beginPath();
            for(Edge e : graph.edges()){
                    drawEdge(e);
            }
            this.gc.stroke();
            this.gc.closePath();

    }

    private void drawShortestPath(DijkstraSP dijkstra){
        if(dijkstra == null) return;
            this.gc.setStroke(Color.BLUE);
            this.gc.setLineWidth(this.z(1));
            gc.beginPath();
            for(Edge e : dijkstra.pathTo(dijkstra.target)){
                drawEdge(e);
            }
            this.gc.stroke();
            this.gc.closePath();
    }

    // Sets the current styling options for graphicscontext based on eventual keyfeature/valuefeature values provided
    private void setStyling(final featureDraw draw){
        if(draw != null) {
            if (draw.stroke_color != null)  this.gc.setStroke(Color.web(draw.stroke_color));
            if (draw.line_width != 0)       this.gc.setLineWidth(draw.line_width);
            if (draw.fill_color != null)    this.gc.setFill(Color.web(draw.fill_color));
            if (draw.dash_size != 0)        this.gc.setLineDashes(draw.dash_size);
            if (draw.force_stroke_color != null)  this.gc.setStroke(Color.web(draw.force_stroke_color));
            if (draw.force_fill_color != null)    this.gc.setFill(Color.web(draw.force_fill_color));
        }
    }

    // Sets the default styling options for graphicscontext in case no values for keyfeature/valuefeature are provided
    private void setStylingDefault(){
        this.gc.setFont(new Font("Arial",this.z(11)));
        this.gc.setFill(Color.BLACK);
        this.gc.setLineWidth(0.00001);
        this.gc.setStroke(Color.BLACK);
        this.gc.setFillRule(FillRule.NON_ZERO);
        this.gc.setLineDashes(1);
        this.gc.setGlobalAlpha(1);
    }

    public void darkMode()
    {
        this.backgroundColor = "#31428c";

        model.yamlObj.keyfeatures.forEach((key, value) -> value.valuefeatures.forEach((keyVF, valueVF) -> {

            if (valueVF != null) {

                if (key.equals("highway"))
                {
                    valueVF.draw.force_stroke_color = Color.web("#343742").toString();
                }
                else if (key.equals("building"))
                {
                    value.draw.force_stroke_color = Color.web("#586a8a").toString();
                    value.draw.force_fill_color = Color.web("#586a8a").toString();

                }
                else if (keyVF.equals("water"))
                {
                    valueVF.draw.force_stroke_color = Color.web("#31428c").toString();
                    valueVF.draw.force_fill_color = Color.web("#31428c").toString();
                }
                else
                {
                    valueVF.draw.force_stroke_color = Color.web("#3f4a5c").toString(); //Color.web(value.draw.stroke_color).darker().toString();
                    valueVF.draw.force_fill_color = Color.web("#3f4a5c").toString();
                }
            }
        }));
    }

    public void lightMode()
    {
        this.backgroundColor = "#b5d2dd";

        model.yamlObj.keyfeatures.forEach((key, value) -> value.valuefeatures.forEach((keyVF, valueVF) -> {
            value.draw.force_stroke_color = null;
            value.draw.force_fill_color = null;
            valueVF.draw.force_stroke_color = null;
            valueVF.draw.force_fill_color = null;

        }));
    }

    /* ----------------------------------------------------------------------------------------------------------------- *
     * ------------------------------------------------- Event Methods ------------------------------------------------- *
     * ----------------------------------------------------------------------------------------------------------------- */
    public void scrolled(final double dy){
        this.zoomTo(Math.pow(1.003, dy));
    }

    public void dragged(final MouseEvent e, final float[] p){
        this.doDrag(true);
        this.panTo(p);
        this.setMousePos(p);
        this.zoombox.drag(this.gc,this.mousePos,this.zoom_current);
        this.painter.drag(this.mousePos);
    }

    public void pressed(final MouseEvent e){
        this.magnifyingGlass(e);
        this.zoombox.press(this.mousePos);
        this.painter.press(this.mousePos);
    }

    public void released(final MouseEvent e){
        this.doDrag(false);
        this.zoombox.release(this,this.mousePos);
        this.painter.release();
    }

    public void moved(float[] p){
        this.setMousePos(p);
    }

    /* ----------------------------------------------------------------------------------------------------------------- *
     * -------------------------------------------- Canvas Drawing Methods --------------------------------------------- *
     * ----------------------------------------------------------------------------------------------------------------- */
    private void strokeBox(double padding){
        if(this.deprop.get("debugVisBox") && this.model.isLoaded()){
            padding = this.z(padding);
            double csize = this.z(5);

            this.gc.setLineWidth(this.z(1));
            this.gc.setStroke(Color.BLUE);
            this.gc.setLineDashes(this.z(3));
            this.gc.beginPath();
            this.gc.moveTo(this.minPos[0]+padding,this.minPos[1]+padding);
            this.gc.lineTo(this.minPos[0]+padding,this.maxPos[1]-padding);
            this.gc.lineTo(this.maxPos[0]-padding,this.maxPos[1]-padding);
            this.gc.lineTo(this.maxPos[0]-padding,this.minPos[1]+padding);
            this.gc.lineTo(this.minPos[0]+padding,this.minPos[1]+padding);
            this.gc.stroke();
            this.gc.closePath();
            this.gc.setFill(Color.BLACK);
            this.gc.fillOval(this.originPos[0],this.originPos[1],csize,csize);
            this.gc.fillOval(this.minPos[0]+padding-csize,this.minPos[1]+padding-csize,csize,csize);
            this.gc.fillOval(this.maxPos[0]-padding,this.minPos[1]+padding-csize,csize,csize);
            this.gc.fillOval(this.maxPos[0]-padding,this.maxPos[1]-padding,csize,csize);
            this.gc.fillOval(this.minPos[0]+padding-csize,this.maxPos[1]-padding,csize,csize);

            if(this.deprop.get("debugDisableHelpText")) {
                this.gc.fillText("relative origin (" + String.format("%.5f", this.originPos[0]) + "," + String.format("%.5f", this.originPos[1]) + ")", this.originPos[0] + csize, this.originPos[1] - csize);
                this.gc.fillText("top left (" + String.format("%.5f", this.minPos[0] + padding) + "," + String.format("%.5f", this.minPos[1] + padding) + ")", this.minPos[0] + padding + csize, this.minPos[1] + padding - csize);
                this.gc.fillText("top right (" + String.format("%.5f", this.maxPos[0] - padding) + "," + String.format("%.5f", this.minPos[1] + padding) + ")", this.maxPos[0] - padding + csize, this.minPos[1] + padding - csize);
                this.gc.fillText("bottom right (" + String.format("%.5f", this.maxPos[0] - padding) + "," + String.format("%.5f", this.maxPos[1] - padding) + ")", this.maxPos[0] - padding + csize, this.maxPos[1] - padding - csize);
                this.gc.fillText("bottom left (" + String.format("%.5f", this.minPos[0] + padding) + "," + String.format("%.5f", this.maxPos[1] - padding) + ")", this.minPos[0] + padding + csize, this.maxPos[1] - padding - csize);
            }
        }
    }

    private void strokeCursor(){
        if(this.deprop.get("debugCursor") && this.model.isLoaded()){
            this.gc.setLineWidth(1);
            this.gc.setFill(Color.BLUE);
            this.gc.fillOval(this.mousePos[0],this.mousePos[1],this.z(5),this.z(5));
            if(this.deprop.get("debugDisableHelpText")) this.gc.fillText("cursor (" + String.format("%.5f", this.mousePos[0]) + "," + String.format("%.5f", this.mousePos[1]) + ")",this.mousePos[0]+this.z(5),this.mousePos[1]-this.z(5));
            this.gc.setFill(Color.BLACK);
        }
    }

    private void splitsTree(){
        if(this.deprop.get("debugSplits") && this.model.isLoaded()){
            List<float[]> lines = this.model.kdtree.getSplits();
            this.gc.setLineWidth(this.z(2.5));
            this.gc.setStroke(Color.GREEN);
            this.gc.setLineDashes(0);

            for(int i = 0; i < lines.size(); i+=2){
                this.gc.beginPath();
                this.gc.moveTo(lines.get(i)[0],lines.get(i)[1]);
                this.gc.lineTo(lines.get(i+1)[0],lines.get(i+1)[1]);
                this.gc.stroke();
                this.gc.closePath();
            }
        }
    }

    private void drawBounds(){
        if(this.deprop.get("debugBoundingBox") && this.model.isLoaded()){
            this.gc.setLineWidth(this.z(1));
            this.gc.setLineDashes(0);
            this.gc.setStroke(Color.RED);
            this.gc.beginPath();
            this.gc.moveTo(this.model.minBoundsPos[1],this.model.minBoundsPos[0]);
            this.gc.lineTo(this.model.maxBoundsPos[1], this.model.minBoundsPos[0]);
            this.gc.lineTo(this.model.maxBoundsPos[1],this.model.maxBoundsPos[0]);
            this.gc.lineTo(this.model.minBoundsPos[1],this.model.maxBoundsPos[0]);
            this.gc.lineTo(this.model.minBoundsPos[1],this.model.minBoundsPos[0]);
            this.gc.stroke();
            this.gc.closePath();
            this.gc.setFill(Color.RED);

            double csize = this.z(5);
            this.gc.fillOval(this.model.originBoundsPos[0],this.model.originBoundsPos[1], csize, csize);
            this.gc.fillText("boundary origin (" + String.format("%.5f", this.model.originBoundsPos[0])
                    + "," + String.format("%.5f", this.model.originBoundsPos[1]) + ")", this.model.originBoundsPos[0] + csize, this.model.originBoundsPos[1] - csize);
        }
    }

    /* ----------------------------------------------------------------------------------------------------------------- *
     * ------------------------------------------- Canvas Interaction Methods ------------------------------------------ *
     * ----------------------------------------------------------------------------------------------------------------- */
    // Allows the user to navigate around the map by panning.
    // this is used in onMouseDragged from Controller.
    public void panTo(float[] pos){
        float dx = pos[0] - this.rtMousePos[0];
        float dy = pos[1] - this.rtMousePos[1];
        float[] diff = new float[]{dx,dy};

        if(!this.zoombox.isZooming() && !this.painter.isDrawing() && !this.drags) this.pan(diff);
        if(!this.isInBounds() && !this.deprop.get("debugFreeMovement")) this.pan(new float[]{diff[0]*-1,diff[1]*-1});
    }

    private void pan(float[] pos) {
        this.setScale(pos);
        this.trans.prependTranslation(pos[0],pos[1]);
        this.repaint();
    }

    public void zoomTo(final double factor){
        if((this.zoom_current * factor) > this.minZoom && (this.zoom_current * factor) < this.maxZoom) this.zoom(factor);
    }

    // Allows the user to zoom in on the map.
    // this is used in onScroll from Controller.
    private void zoom(final double factor){
        float[] oldPos = this.originPos;
        this.zoom_current *= factor;
        this.trans.prependScale(factor, factor);
        this.setScale(new float[]{0,0});
        this.goToPosAbsolute(oldPos);
    }

    private void setScale(final float[] pos){
        double minx = this.minPos[0] - this.z(pos[0]);
        double miny = this.minPos[1] - this.z(pos[1]);
        this.minPos = new float[]{(float) minx,(float) miny};

        double maxx = minx + this.z(super.getWidth());
        double maxy = (miny + this.z(super.getHeight())) - this.z(25);
        this.maxPos = new float[]{(float) maxx,(float) maxy};

        double originx = minx+(maxx-minx) / 2;
        double originy = miny+(maxy-miny) / 2;
        this.originPos = new float[]{(float) originx,(float) originy};
    }

    public void setMousePos(final float[] point){
        double dx = this.z(point[0]) + this.minPos[0];
        double dy = this.z(point[1]) + this.minPos[1];
        this.mousePos = new float[]{(float) dx,(float) dy};
        this.rtMousePos = point;
        this.repaint();
    }

    public void update(){
        this.setScale(new float[]{0,0});
        this.repaint();
    }

    public void checkInBounds(){
        if(!this.isInBounds() && !this.deprop.get("debugFreeMovement")) this.placeInBounds();
    }

    private boolean isInBounds(){
        return (this.originPos[0] >= this.model.minBoundsPos[1] && this.originPos[0] <= this.model.maxBoundsPos[1]
                && this.originPos[1] >= this.model.minBoundsPos[0] && this.originPos[1] <= this.model.maxBoundsPos[0]);
    }

    // https://math.stackexchange.com/questions/127613/closest-point-on-circle-edge-from-point-outside-inside-the-circle
    private void placeInBounds(){
        double r = (this.model.maxBoundsPos[1]-this.model.originBoundsPos[0]);
        double d = Math.sqrt(Math.pow(this.originPos[0]-this.model.originBoundsPos[0],2)+Math.pow(this.originPos[1]-this.model.originBoundsPos[1],2));
        double x = this.model.originBoundsPos[0] + r * (this.originPos[0]-this.model.originBoundsPos[0])/d;
        double y = this.model.originBoundsPos[1] + r * (this.originPos[1]-this.model.originBoundsPos[1])/d;
        this.goToPosAbsolute(new float[]{(float) x,(float) y});
    }

    public void clearScreen(){
        this.gc.setFill(Color.WHITE);
        this.repaint();
    }

    public void goToPosAbsolute(final float[] pos){
        double dx = this.rz(pos[0] - this.originPos[0]);
        double dy = this.rz(pos[1] - this.originPos[1]);
        this.pan(new float[]{(float) -dx,(float) -dy});
    }

    public void goToPosRelative(final float[] pos){
        this.pan(new float[]{(float) this.rz(pos[0]),(float) this.rz(pos[1])});
    }

    public void centerPos(){
        float dx = (this.model.maxBoundsPos[1] + this.model.minBoundsPos[1])/2;
        float dy = (this.model.maxBoundsPos[0] + this.model.minBoundsPos[0])/2;
        this.goToPosAbsolute(new float[]{dx,dy});
    }

    public void centerMap(){
        this.centerPos();
        this.pan(new float[]{0,-50});
        this.update();
    }

    /* ----------------------------------------------------------------------------------------------------------------- *
     * ------------------------------------------------- Misc Methods -------------------------------------------------- *
     * ----------------------------------------------------------------------------------------------------------------- */
    public double z(double num){
        return (num / this.zoom_current);
    }

    public double rz(double num){
        return (num * this.zoom_current);
    }
}