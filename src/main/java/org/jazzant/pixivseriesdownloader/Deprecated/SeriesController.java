package org.jazzant.pixivseriesdownloader.Deprecated;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import org.jazzant.pixivseriesdownloader.SeriesModel;


public class SeriesController {
    private SeriesViewBuilder viewBuilder;
    private SeriesInteractor interactor;

    public SeriesController(){
        SeriesModel model = new SeriesModel();
        interactor = new SeriesInteractor(model);
        viewBuilder = new SeriesViewBuilder(model, this::saveSeries, interactor::parseSeries);
    }

    private void saveSeries(Runnable onSaveSucceed){
        Task<Boolean> saveTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return interactor.saveSeries();
            }
        };
        saveTask.setOnSucceeded(event->{
            onSaveSucceed.run();
            if(!saveTask.getValue()){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Series with this ID already exists in the database.");
                alert.show();
            }
        });
        Thread saveThread = new Thread(saveTask);
        saveThread.start();
    }

    public Region getView(){
        return viewBuilder.build();
    }
}
