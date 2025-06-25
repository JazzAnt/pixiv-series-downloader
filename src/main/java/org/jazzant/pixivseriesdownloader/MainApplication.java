package org.jazzant.pixivseriesdownloader;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        stage.setScene(new Scene(new SeriesController().getView()));
        stage.show();
    }
}
