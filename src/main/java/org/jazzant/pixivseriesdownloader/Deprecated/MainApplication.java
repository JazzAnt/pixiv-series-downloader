package org.jazzant.pixivseriesdownloader.Deprecated;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jazzant.pixivseriesdownloader.Parser;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parser.initialize();
        stage.setScene(new Scene(
                new SeriesController().getView()
        ));
        stage.show();
    }
}
