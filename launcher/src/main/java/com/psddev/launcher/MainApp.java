package com.psddev.launcher;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MainApp.class);


    public static void main(String[] args) throws Exception {
        launch(args);
    }

    public void start(Stage stage) throws Exception {

        stage.getIcons().add(new Image("file:resources/images/psd-icon.png"));

        String fxmlFile = "/fxml/main.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent rootNode = (Parent)loader.load(getClass().getResourceAsStream(fxmlFile));

        Scene scene = new Scene(rootNode, 700, 500);
        scene.getStylesheets().add("/styles/styles.css");

        stage.setTitle("Brightspot Launcher");
        stage.setScene(scene);

        stage.show();
    }
}
