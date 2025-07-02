package org.jazzant.pixivseriesdownloader;

import javafx.concurrent.Task;
import javafx.scene.layout.Region;


public class SeriesController {
    private SeriesViewBuilder viewBuilder;
    private SeriesInteractor interactor;

    public SeriesController(){
        SeriesModel model = new SeriesModel();
        interactor = new SeriesInteractor(model);
        viewBuilder = new SeriesViewBuilder(model, this::saveSeries, interactor::parseSeries);
    }

    private void saveSeries(){
        Task<Void> saveTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                interactor.saveSeries();
                return null;
            }
        };
        Thread saveThread = new Thread(saveTask);
        saveThread.start();
    }

    public Region getView(){
        return viewBuilder.build();
    }
}
