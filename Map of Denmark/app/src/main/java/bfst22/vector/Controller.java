package bfst22.vector;

import javafx.beans.Observable;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.*;

// Responsible for controlling/updating the current view and manipulating dataflow of model.
public class Controller {
    private Stage stage;
	private Model model;
    private List<String> loadedMaps;
    private ContextMenu canvasCM;

	@FXML private MapCanvas canvas;
    @FXML private TitledPane pinPointSidebar;
    @FXML private ScrollPane vBox_scrollpane;
    @FXML private HBox paintBox;
    @FXML private Pane somePane;
    @FXML private VBox vbox_slider, routePlanVBox, routeVBoxPane;
    @FXML private ToolBar paintBar, toolsBar, statusBar;
    @FXML private BorderPane someBorderPane;
	@FXML private MenuItem unloadFileButton;
    @FXML private Menu recentMapsSubmenu;
    @FXML private CheckMenuItem infoSidebar, sliderSidebar, debugSidebar;
    @FXML private ToggleGroup mapdisplay, brushModeGroup;
    @FXML private ColorPicker paintColourPicker;
    @FXML private Spinner<Double> paintStrokeSize;
    @FXML private Spinner<Integer> paintFontSize;
    @FXML private HBox search_root;
    @FXML private GridPane search_pane;
    @FXML private HBox search_box;
    @FXML private Button searchButton;
    @FXML private Button clearButton;
    @FXML private Button routeFindButton;
    @FXML private Button routeSwitchButton;
    @FXML private Button slider_button_increase, slider_button_decrease;
    @FXML private Slider slider_bar;
    @FXML private TextFieldSuggestion searchField, startAddress, targetAddress;
    @FXML private ToggleButton zoomBoxButton;
    @FXML private ToggleButton zoomMagnifyingGlass;
    @FXML private ToggleButton pinpointButton;
    @FXML private ToggleGroup routeTransport;
    @FXML private ComboBox<String> fontBox;
    @FXML private TreeView<String> featuresTreeView;
    @FXML private ListView<HBox> pinPointList;
    @FXML private StackPane center_stack;
    @FXML private VBox topmenu;
    @FXML private Label routeErrorLabel;
    @FXML private ScrollPane routeTextPane;
    @FXML private Label distance;
    @FXML private Separator routeSeperator;

    // Debug menu variables
    @FXML private ScrollPane vbox_debug_scrollpane;
    @FXML private Label canvas_min;
    @FXML private Label canvas_max;
    @FXML private Label canvas_origin;
    @FXML private Label canvas_mouse;
    @FXML private Label canvas_zoom;
    @FXML private Label canvas_bounds_min;
    @FXML private Label canvas_bounds_max;
    @FXML private Label canvas_nodes;
    @FXML private Label canvas_ways;
    @FXML private Label canvas_relations;
    @FXML private Label canvas_load_time;
    @FXML private Label canvas_repaint_time;
    @FXML private Label canvas_avg_repaint_time;
    @FXML private Label canvas_map_name;
    @FXML private Label canvas_map_size;

    /* ----------------------------------------------------------------------------------------------------------------- *
     * ------------------------------------------------ General Methods ------------------------------------------------ *
     * ----------------------------------------------------------------------------------------------------------------- */
    // Runs upon start of program: Initializes our MapCanvas based on model.
    public Controller(final Model model, final Stage primarystage) {
        primarystage.setScene(new Scene(Objects.requireNonNull(Controller.smartFXMLLoader(this, "View.fxml"))));
        primarystage.setWidth(this.someBorderPane.getPrefWidth());
        primarystage.setHeight(this.someBorderPane.getPrefHeight());

        this.model = model;
        this.stage = primarystage;
        this.loadedMaps = new ArrayList<>();
        this.canvasCM = new ContextMenu();

        this.someBorderPane.setBottom(null);
        this.canvas.init(model);
        this.canvas.pinpoints.init(pinPointList);
        this.addRecentLoadedMap(this.model.currFileName);
        this.canvas.centerMap();
        this.generateTreeView();
        this.generateContextMenu();
        this.slider_bar.setValue(this.canvas.zoom_current);
        this.searchField.init(this.model.searchTree, this.canvas, true);
        this.startAddress.init(this.model.searchTree, this.canvas, false);
        this.targetAddress.init(this.model.searchTree, this.canvas, false);

        if(this.canvas.deprop.get("debugSideBar")){
            this.canvas.deprop.toggle("debugSideBar");
            this.debugSidebar.setSelected(true);
            this.debugSidebarClicked(new ActionEvent());
        }

        this.someBorderPane.prefWidthProperty().bind(stage.widthProperty());
        this.someBorderPane.prefHeightProperty().bind(stage.heightProperty());
        this.someBorderPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> {
            this.canvas.setWidth(newValue.doubleValue());
            this.canvas.update();
            this.canvas.checkInBounds();
        });
        this.someBorderPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> {
            this.canvas.setHeight(newValue.doubleValue() - this.topmenu.getHeight() - 38 - (this.someBorderPane.getBottom()!=null?38:0));
            this.canvas.update();
            this.canvas.checkInBounds();
        });
        this.someBorderPane.setOnKeyPressed(e -> {
            this.canvas.painter.keyPress(e.getText());
            this.canvas.update();
        });
        this.fontBox.getItems().addAll(Font.getFamilies());
        this.fontBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> this.canvas.painter.setFont(newValue));
    }

    private void generateContextMenu(){
        MenuItem addPoint = new MenuItem("Add Pin Point Here");
        addPoint.setGraphic(new FontIcon("fas-map-pin:12"));
        addPoint.setOnAction(item -> this.canvas.pinpoints.newWindow(this.canvas));
        this.canvasCM.getItems().add(addPoint);
        this.canvas.setOnContextMenuRequested(e -> this.canvasCM.show(this.canvas, e.getScreenX(), e.getScreenY()));
    }

    public static Parent smartFXMLLoader(Object con, String filename) {
        try {
            FXMLLoader loader = new FXMLLoader(con.getClass().getResource(filename));
            loader.setController(con);
            return loader.load();
        } catch(IOException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    private void updateDebugInfo(){
        if(!this.model.isLoaded() || this.model.isLoaded() && this.vbox_debug_scrollpane.isVisible()){
            this.canvas_min.setText(String.format("%-27s%s", "min:", String.format("%.5f", this.canvas.minPos[0]) + ", " + String.format("%.5f", this.canvas.minPos[1])));
            this.canvas_max.setText(String.format("%-26.5s%s", "max:", String.format("%.5f", this.canvas.maxPos[0]) + ", " + String.format("%.5f", this.canvas.maxPos[1])));
            this.canvas_origin.setText(String.format("%-26s%s", "origin:", String.format("%.5f", this.canvas.originPos[0]) + ", " + String.format("%.5f", this.canvas.originPos[1])));
            this.canvas_mouse.setText(String.format("%-24s%s", "mouse:", String.format("%.5f", this.canvas.mousePos[0]) + ", " + String.format("%.5f", this.canvas.mousePos[1])));
            this.canvas_zoom.setText(String.format("%-25s%s", "zoom:", String.format("%.5f", this.canvas.zoom_current)));
            this.canvas_bounds_min.setText(String.format("%-21s%s", "bounds min:", String.format("%.5f", this.model.minBoundsPos[1]) + ", " + String.format("%.5f", this.model.minBoundsPos[0])));
            this.canvas_bounds_max.setText(String.format("%-20s%s", "bounds max:", String.format("%.5f", this.model.maxBoundsPos[1]) + ", " + String.format("%.5f", this.model.maxBoundsPos[0])));
            this.canvas_nodes.setText(String.format("%-25s%s", "nodes:", this.model.nodecount));
            this.canvas_ways.setText(String.format("%-26s%s", "ways:", this.model.waycount));
            this.canvas_relations.setText(String.format("%-25s%s", "relations:", this.model.relcount));
            this.canvas_load_time.setText(String.format("%-24s%d ms", "load time:", this.model.loadTime/1000000));
            this.canvas_repaint_time.setText(String.format("%-23s%d ms", "repaint time:", this.canvas.repaintTime/1000000));
            this.canvas_avg_repaint_time.setText(String.format("%-20s%d ms", "avg repaint time:", this.canvas.avgRT/1000000));
        }
    }

    private void updateStatusInfo(){
        this.canvas_map_name.setText(String.format("%s%s", "File name: ", this.model.currFileName));
        this.canvas_map_size.setText(String.format("%s%d bytes", "File size: ", this.model.filesize));
    }

    private String inputWindow(String title, String contentText){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setContentText(contentText);
        dialog.setResizable(false);
        dialog.setHeaderText(null);
        dialog.setGraphic(null);

        return dialog.showAndWait().orElse(null);
    }

    private void generateTreeView(){
        CheckBoxTreeItem<String> root = new CheckBoxTreeItem<>("Map Elements");
        root.setExpanded(true);
        root.selectedProperty().addListener(this::treeboxselected);

        this.featuresTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
        this.featuresTreeView.setRoot(root);

        for(Map.Entry<String,keyFeature> feature : this.model.yamlObj.keyfeatures.entrySet()){
            CheckBoxTreeItem<String> featureString = new CheckBoxTreeItem<>(feature.getKey());
            featureString.selectedProperty().addListener(this::treeboxselected);
            root.getChildren().add(featureString);
            for(Map.Entry<String,valueFeature> subfeature : feature.getValue().valuefeatures.entrySet()){
                CheckBoxTreeItem<String> subfeatureString = new CheckBoxTreeItem<>(subfeature.getKey());
                featureString.getChildren().add(subfeatureString);
                subfeatureString.selectedProperty().addListener(this::treeboxselected);
                subfeatureString.setSelected(true);
            }
        }
    }

    private void treeboxselected(Observable box){
        this.featuresTreeView.getRoot().getChildren().forEach(keyFeature -> keyFeature.getChildren().forEach(valueFeature -> {
            keyFeature keyobj = this.model.yamlObj.keyfeatures.get(keyFeature.getValue());
            valueFeature valueobj = keyobj.valuefeatures.get(valueFeature.getValue());
            CheckBoxTreeItem<String> valuebox = ((CheckBoxTreeItem<String>) valueFeature);
            valueobj.draw.hide = !valuebox.isSelected();
        }));

        this.canvas.update();
    }

    /* ----------------------------------------------------------------------------------------------------------------- *
     * ---------------------------------------------- Map Loading Methods ---------------------------------------------- *
     * ----------------------------------------------------------------------------------------------------------------- */
    private void loadMap(String filename) throws XMLStreamException, IOException, ClassNotFoundException {
        this.addRecentLoadedMap(filename);
        this.model.unload();
        this.model.load(filename);
        this.canvas.reset();
        this.canvas.zoomTo(42000);
        this.canvas.centerPos();
        this.canvas.panTo(new float[]{0,-50});
        this.canvas.setDisable(false);
        this.unloadFileButton.setDisable(false);
        this.updateStatusInfo();
        this.updateDebugInfo();
    }

    private void unloadMap(){
        this.model.unload();
        this.canvas.reset();
        this.canvas.setDisable(true);
        this.canvas.clearScreen();
        this.unloadFileButton.setDisable(true);
        this.updateStatusInfo();
        this.updateDebugInfo();
    }

    private void addRecentLoadedMap(String filename){
        this.loadedMaps.remove(filename);
        this.loadedMaps.add(filename);
        if (this.loadedMaps.size() > 10) this.loadedMaps.remove(this.loadedMaps.size()-1);
        this.recentMapsSubmenu.getItems().clear();

        for (int i = this.loadedMaps.size()-1; i > -1; i--) {
            String map = this.loadedMaps.get(i).replace("\\","/");
            MenuItem entry = new MenuItem((this.loadedMaps.size()-1-i) + ". " + map);
            entry.setUserData(map);
            entry.setOnAction(event -> {
                this.model.unload();
                this.canvas.reset();
                try {
                    this.model.load(entry.getUserData().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.canvas.centerPos();
                canvas.setDisable(false);
                this.unloadFileButton.setDisable(false);
            });

            this.recentMapsSubmenu.getItems().add(entry);
            this.recentMapsSubmenu.setDisable(false);
        }
    }

    /* ----------------------------------------------------------------------------------------------------------------- *
     * ------------------------------------------------ Menubar Methods ------------------------------------------------ *
     * ----------------------------------------------------------------------------------------------------------------- */
    @FXML private void onMenuButtonPress(ActionEvent e){
        this.vBox_scrollpane.setVisible(!this.vBox_scrollpane.isVisible());
        this.infoSidebar.setSelected(this.vBox_scrollpane.isVisible());
    }

    @FXML private void onZoomBoxButtonPressed(ActionEvent e){
        this.canvas.zoombox.setState(this.zoomBoxButton.isSelected());
    }

    @FXML private void onZoomMagnifyingGlassButtonPressed(ActionEvent e){
        this.canvas.zoomMagnifyingGlass = !this.canvas.zoomMagnifyingGlass;
    }

    @FXML private void onSearchButtonPressed(ActionEvent e) {
        List<TernarySearchTree.Address> addresses = this.model.searchTree.searchSuggestions(searchField.getText().toLowerCase(Locale.ROOT));
        if(!addresses.isEmpty()){
            TernarySearchTree.Address suggestion = addresses.get(0);
            this.canvas.goToPosAbsolute(new float[]{suggestion.coordPos[0],suggestion.coordPos[1]});
            this.canvas.zoomTo(300000);
            searchField.setText(suggestion.toString());
        }
    }

    @FXML private void sliderButtonIncreasePressed(MouseEvent e){
        this.slider_bar.increment();
        this.sliderBarPressed(e);
    }

    @FXML private void sliderButtonDecreasePressed(MouseEvent e){
        this.slider_bar.decrement();
        this.sliderBarPressed(e);
    }

    @FXML private void sliderBarPressed(MouseEvent e){
        this.canvas.zoomTo(((int) this.slider_bar.getValue())/this.canvas.zoom_current);
    }

    @FXML private void onClearButtonPressed(ActionEvent e){
        searchField.clear();
    }

    @FXML private void onPaintFillCheckboxPressed(ActionEvent e){
        this.canvas.painter.toggleFill();
    }

    @FXML private void onPaintButtonPressed(ActionEvent e){
        if(this.brushModeGroup.getSelectedToggle() == null) this.canvas.painter.setDrawMode(-1);
        else this.canvas.painter.setDrawMode(Integer.parseInt((String) this.brushModeGroup.getSelectedToggle().getUserData()));
    }

    @FXML private void onPaintColorButtonPressed(ActionEvent e){
        this.canvas.painter.setColour(this.paintColourPicker.getValue());
    }

    @FXML private void spinnerPaintStrokeSizeButtonPressed(KeyEvent e){
        this.canvas.painter.setStroke(paintStrokeSize.getValue());
    }

    @FXML private void spinnerPaintFontSizeButtonPressed(KeyEvent e){
        this.canvas.painter.setFontSize(this.paintFontSize.getValue());
    }

    @FXML private void findClosestRoute(MouseEvent e){
        TernarySearchTree.Address start = this.startAddress.getSelectedAddress();
        TernarySearchTree.Address target = this.targetAddress.getSelectedAddress();
        this.routeErrorLabel.setVisible(false);
        this.routePlanVBox.setVisible(false);

        if(start == null || target == null) {
            this.routeErrorLabel.setText("Choose start- and target addresses!");
            this.routeErrorLabel.setVisible(true);
            return;
        }

        String userdata = routeTransport.getSelectedToggle().getUserData().toString();
        VehicleType type = VehicleType.MOTORCAR;
        if(userdata.equals("1")) type = VehicleType.BICYCLE;
        else if(userdata.equals("2")) type = VehicleType.FOOT;

        float[] startPos = this.model.NNRoutetree.findNN(start.coordPos,type);
        float[] targetPos = this.model.NNRoutetree.findNN(target.coordPos,type);
        PolyPoint s = null, t = null;

        for(Edge f : model.graph.edges()){
            if(f.getFrom().lat == startPos[0] && f.getFrom().lon == startPos[1]) s = f.getFrom();
            if(f.getTo().lat == targetPos[0] && f.getTo().lon == targetPos[1]) t = f.getTo();
        }

        this.model.dijkstraSP = new DijkstraSP(model.graph,s,t,type);

        if(this.model.dijkstraSP.pathTo(t) == null){
            this.routeErrorLabel.setText("There is no such route!");
            this.routeErrorLabel.setVisible(true);
            return;
        }

        Directions directions = new Directions();
        ArrayList<Edge> directionList = new ArrayList<>();
        for(Edge f : this.model.dijkstraSP.pathTo(t)){
            directionList.add(f);
        }

        PolyPoint first = directionList.get(0).getFrom();
        PolyPoint last = directionList.get(directionList.size()-1).getTo();
        Distance d = new Distance();
        this.distance.setText("Total distance: " + String.format("%.1f",d.haversineFormula(first,last)) + " kilometers.");

        int j = 1;
        float difference;
        List<HBox> route = new ArrayList<>();

        this.routeVBoxPane.getChildren().clear();
        route.add(directions.turn(j++, 0, 0, directionList.get(0).getFrom(), null, this.canvas, false));
        for(int i = 0; i < directionList.size() - 1; i+=3) {
            float getAngle = directions.getAngle(directionList.get(i).getFrom(), directionList.get(i).getTo());

            if (i + 3 >= directionList.size()) break;
            if (i == 0) {
                difference = directions.getAngleDifference(directionList.get(i).getFrom(), directionList.get(i).getTo(), directionList.get(i).getFrom(), directionList.get(i).getTo());
                route.add(directions.turn(j++, getAngle, difference, directionList.get(i).getFrom(), directionList.get(i).getTo(),this.canvas,false));
            } else if (i > 0) {
                difference = directions.getAngleDifference(directionList.get(i).getFrom(), directionList.get(i).getTo(), directionList.get(i + 3).getFrom(), directionList.get(i + 3).getTo());
                if (difference > 90) //Turns
                    route.add(directions.turn(j++, getAngle, difference, directionList.get(i + 3).getFrom(), directionList.get(i + 3).getTo(),this.canvas,false));
                if (difference < 90 && (!Objects.equals(directionList.get(i).getFrom().address, directionList.get(i + 3).getFrom().address))) //Continue
                    route.add(directions.turn(j++, getAngle, difference, directionList.get(i + 3).getFrom(), directionList.get(i + 3).getTo(),this.canvas,false));
            }
        }
        route.add(directions.turn(j, 0, 0, directionList.get(directionList.size()-1).getFrom(), null, this.canvas, true));

        this.routeVBoxPane.getChildren().addAll(route);
        this.routePlanVBox.setVisible(true);
    }

    @FXML private void switchOrderRoute(MouseEvent e){
        TernarySearchTree.Address tempAddr = this.targetAddress.getSelectedAddress();
        this.targetAddress.setSelectedAddress(this.startAddress.getSelectedAddress());
        this.startAddress.setSelectedAddress(tempAddr);
        this.findClosestRoute(e);
    }

    /* ----------------------------------------------------------------------------------------------------------------- *
     * ------------------------------------------------- Mouse Methods ------------------------------------------------- *
     * ----------------------------------------------------------------------------------------------------------------- */
    // handles an event of scrolling and increases/decreases the zoom level of the map.
    @FXML private void onScroll(final ScrollEvent e) {
        this.canvas.scrolled(e.getDeltaY());
        this.slider_bar.setValue(this.canvas.zoom_current);
        this.updateDebugInfo();
    }

    // handles panning in the program
    @FXML private void onMouseDragged(final MouseEvent e) {
        this.canvas.dragged(e,new float[]{(float) e.getX(), (float) e.getY()});
        this.updateDebugInfo();
    }

    // updates the variable lastMouse upon pressing (necessary for onMouseDragged)
    @FXML private void onMousePressed(final MouseEvent e) {
        this.canvasCM.hide();
        if(e.getClickCount() == 2) this.canvas.pinpoints.doubleClick(this.canvas);
        this.canvas.pressed(e);
        this.updateDebugInfo();
    }

    // updates upon releasing
    @FXML private void onMouseReleased(final MouseEvent e) {
        this.canvas.released(e);
        this.updateDebugInfo();
    }

    // updates the mouse position on the screen upon moving
    @FXML private void onMouseMoved(final MouseEvent e){
        this.canvas.moved(new float[]{(float) e.getX(), (float) e.getY()});
        this.updateDebugInfo();
    }

    /* ----------------------------------------------------------------------------------------------------------------- *
     * --------------------------------------------- Menu Dropdown Methods --------------------------------------------- *
     * ----------------------------------------------------------------------------------------------------------------- */
    // when the menubar 'File' section button 'Load Default map' is clicked
    @FXML private void onDefaultLoadClicked(final ActionEvent e) throws Exception {
        this.loadMap("data/small.osm.zip");
    }

    // when the menubar 'File' section button 'Import Custom map' is clicked
    @FXML private void onBrowseOSMClicked(final ActionEvent e) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose OSM File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Map File", "*.osm","*.zip","*.obj"));
        File file = fileChooser.showOpenDialog(this.stage);

        if(file != null) this.loadMap(file.getAbsolutePath());
    }

    // when the menubar 'File' section button 'Unload map' is clicked
    @FXML private void unloadFileButtonClicked(final ActionEvent e){
        this.unloadMap();
    }

    // when the menubar 'File' section button 'Exit' is clicked
    @FXML private void exitButtonClicked(final ActionEvent e){
        System.exit(0);
    }

    // when the menubar 'Edit' section button 'Zoom In' is clicked
    @FXML private void zoomInClicked(final ActionEvent e){
        this.canvas.zoomTo(1.2);
    }

    // when the menubar 'Edit' section button 'Zoom Out' is clicked
    @FXML private void zoomOutClicked(final ActionEvent e){
        this.canvas.zoomTo(0.8);
    }

    // when the menubar 'Edit' section button 'Change Zoom Level' is clicked
    @FXML private void changeZoomLevelClicked(final ActionEvent e){
        String zlevel = this.inputWindow("Change Zoom Level","Syntax: 42000");
        if(zlevel != null && !zlevel.isEmpty()) this.canvas.zoomTo(Integer.parseInt(zlevel)/this.canvas.zoom_current);
    }

    // when the menubar 'View' section button 'Paint Bar' is clicked
    @FXML private void paintBarButtonClicked(final ActionEvent e){
        this.paintBar.setVisible(!this.paintBar.isVisible());
        this.canvas.setHeight(this.canvas.getHeight() - (this.paintBar.isVisible() ? 30 : -30));
        this.canvas.update();
    }

    // when the menubar 'View' section button 'Tools Bar' is clicked
    @FXML private void toolsBarButtonClicked(final ActionEvent e){
        this.toolsBar.setVisible(!this.toolsBar.isVisible());
        this.canvas.setHeight(this.canvas.getHeight() - (this.toolsBar.isVisible() ? 30 : -30));
        this.canvas.update();
    }

    // when the menubar 'View' section button 'Status Bar' is clicked
    @FXML private void statusBarMenuClicked(final ActionEvent e){
        this.someBorderPane.setBottom(this.someBorderPane.getBottom() == null ? statusBar : null);
        this.canvas.setHeight(this.canvas.getHeight() - (this.someBorderPane.getBottom() != null ? 40 : -40));
        this.canvas.update();
        this.updateStatusInfo();
    }

    // when the menubar 'View' section button 'Info Sidebar' is clicked
    @FXML private void infoSidebarClicked(final ActionEvent e){
        this.onMenuButtonPress(e);
    }

    // when the menubar 'View' section button 'Slider Sidebar' is clicked
    @FXML private void sliderSidebarClicked(final ActionEvent e){
        this.vbox_slider.setVisible(!this.vbox_slider.isVisible());
        this.vbox_debug_scrollpane.setVisible(false);
        this.debugSidebar.setSelected(false);
    }

    // when the menubar 'View' section button 'Debug Sidebar' is clicked
    @FXML private void debugSidebarClicked(final ActionEvent e){
		this.canvas.deprop.toggle("debugSideBar");
        this.vbox_debug_scrollpane.setVisible(!this.vbox_debug_scrollpane.isVisible());
        this.sliderSidebar.setSelected(false);
        this.vbox_slider.setVisible(false);
        this.updateDebugInfo();
    }

    // when the menubar 'Tools' section button 'Change Absolute Coordinates' is clicked
    @FXML private void changeAbsoluteCoordClicked(final ActionEvent e){
        String abscoords = this.inputWindow("Change Absolute Coordinates","Syntax: -12.345, 67.890");
        if(abscoords != null){
            String[] dialogvalue = abscoords.split(",");
            if(dialogvalue.length == 2) this.canvas.goToPosAbsolute(new float[]{Float.parseFloat(dialogvalue[0]), Float.parseFloat(dialogvalue[1])});
        }
    }

    // when the menubar 'Tools' section button 'Change Relative Coordinates' is clicked
    @FXML private void changeRelativeCoordClicked(final ActionEvent e){
        String relcoords = this.inputWindow("Change Relative Coordinates","Syntax: -12.345, 67.890");
        if(relcoords != null){
            String[] dialogvalue = relcoords.split(",");
            if(dialogvalue.length == 2) this.canvas.goToPosRelative(new float[]{Float.parseFloat(dialogvalue[0]), Float.parseFloat(dialogvalue[1])});
        }
    }

    // when the menubar 'Tools' section button 'Center Screen Position' is clicked
    @FXML private void centerScreenPosition(final ActionEvent e){
        this.canvas.centerPos();
        this.canvas.panTo(new float[]{0,-50});
    }

    // when the menubar 'Tools' section button 'Display Filled' is clicked
    @FXML private void debugDisplayFilledClicked(final ActionEvent e){
        this.canvas.deprop.set("debugDisplayWireframe", false);
        this.canvas.update();
        this.canvas.deprop.set("debugDisplayWireframe", false);
        this.canvas.deprop.set("debugDarkMode", false);
        this.canvas.lightMode();
        this.canvas.update();
    }

    @FXML private void debugDisplayDarkFilledClicked(final ActionEvent e){
        this.canvas.deprop.set("debugDarkMode", true);
        this.canvas.darkMode();
        this.canvas.update();
    }

    // when the menubar 'Tools' section button 'Display Wireframe' is clicked
    @FXML private void debugDisplayWireframeClicked(final ActionEvent e){
        this.canvas.deprop.set("debugDisplayWireframe", true);
        this.canvas.update();
    }

    // when the menubar 'Tools' section button 'Enable Cursor Pointer' is clicked
    @FXML private void debugCursorClicked(final ActionEvent e) throws IOException {
        this.canvas.deprop.toggle("debugCursor");
        this.canvas.update();
    }

    // when the menubar 'Tools' section button 'Enable Kd-Tree VisBox' is clicked
    @FXML private void debugVisBoxClicked(final ActionEvent e){
        this.canvas.deprop.toggle("debugVisBox");
        this.canvas.update();
    }

    // when the menubar 'Tools' section button 'Enable Kd-Tree Splits' is clicked
    @FXML private void debugSplitsClicked(final ActionEvent e) throws IOException {
        this.canvas.deprop.toggle("debugSplits");
        this.canvas.update();
    }

    @FXML private void debugNeighborClicked(final ActionEvent e){
        this.canvas.deprop.toggle("debugNeighbor");
        this.canvas.update();
    }

    // when the menubar 'Tools' section button 'Enable Free Movement' is clicked
    @FXML private void debugFreeMovementClicked(final ActionEvent e){
		this.canvas.deprop.toggle("debugFreeMovement");
        this.canvas.checkInBounds();
    }

    // when the menubar 'Tools' section button 'Disable Help Text' is clicked
    @FXML private void debugHelpTextClicked(final ActionEvent e){
		this.canvas.deprop.toggle("debugDisableHelpText");
        this.canvas.update();
    }

    // when the menubar 'Tools' section button 'Disable Bounding Box' is clicked
    @FXML private void debugBoundingBoxClicked(final ActionEvent e) throws IOException {
        this.canvas.deprop.toggle("debugBoundingBox");
        this.canvas.update();
    }

    // when the menubar 'Help' section button 'About...' is clicked
    @FXML private void aboutButtonClicked(final ActionEvent e){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setContentText("Map of Denmark\nIT-Copenhagen First-Year-Project\n2022 - Group #1");
        alert.setResizable(false);
        alert.show();
    }
}