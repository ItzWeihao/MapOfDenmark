package bfst22.vector;

import javafx.application.Application;
import javafx.stage.Stage;

// Responsible for initiating the application; Model is instantiated with our OSM file and finally View.
public class App extends Application {
    @Override public void start(Stage primaryStage) throws Exception {
        new View(new Model(), primaryStage); // primaryStage is our main window
    }
}