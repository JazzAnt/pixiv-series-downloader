package org.jazzant.pixivseriesdownloader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    public static void main(String[] args) {launch(args);}

    @Override
    public void start(Stage stage) throws IOException {

        Parser.initialize();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("add-series-view.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 400, 300);

//        Image image = new Image("icon.png"); //searches from the src folder
//        stage.getIcons().add(image);

        stage.setTitle("Pixiv Series Downloader");
        stage.setScene(scene);
        stage.show();

    }
}
