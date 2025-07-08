package org.jazzant.pixivseriesdownloader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private SeriesBroker broker = new SeriesBroker();
    public static void main(String[] args) {launch(args);}

    @Override
    public void start(Stage stage) throws IOException {
        Parser.initialize();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("add-series-view.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);

//        Image image = new Image("icon.png"); //searches from the src folder
//        stage.getIcons().add(image);

        AddSeriesController addSeriesController = fxmlLoader.getController();
        addSeriesController.setBroker(broker);

        stage.setTitle("Pixiv Series Downloader");
        stage.setScene(scene);
        stage.setOnCloseRequest(event->Parser.quit());
        stage.show();

    }
}
