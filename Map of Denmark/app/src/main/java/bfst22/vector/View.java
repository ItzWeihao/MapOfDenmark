package bfst22.vector;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

// Responsible for displaying model data.
public class View {
    public View(Model model, Stage stage) throws Exception {
        this.setDisplayBound(stage);

        model.load("data/bornholm.osm");
        new Controller(model,stage);

        stage.setTitle("Danmarkskort - Gruppe #1");
        stage.show();
    }

    // Setting the window displaybound so the scene spawns within the screen
    private void setDisplayBound(Stage primaryStage){
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((primScreenBounds.getHeight() - primaryStage.getHeight() / 2));
    }
}