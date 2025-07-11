package org.jazzant.pixivseriesdownloader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private final SeriesBroker broker = new SeriesBroker();
    private final Parser parser = new Parser();
    public static void main(String[] args) {launch(args);}

    @Override
    public void start(Stage stage) throws IOException {
        stage.setOnCloseRequest(windowEvent -> {
            parser.quit();
            Platform.exit();
        });

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        MainController controller = fxmlLoader.getController();
        controller.setBroker(broker);
        controller.setParser(parser);

        stage.setTitle("Pixiv Series Downloader");
        stage.setScene(scene);
        stage.show();
    }

    private void openAddSeriesWindow(){

    }
}
