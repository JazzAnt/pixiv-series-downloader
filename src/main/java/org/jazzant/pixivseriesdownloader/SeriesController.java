package org.jazzant.pixivseriesdownloader;

import javafx.scene.layout.Region;
import javafx.util.Builder;


public class SeriesController {
    private SeriesViewBuilder viewBuilder;
    private SeriesInteractor interactor;

    public SeriesController(){
        SeriesModel model = new SeriesModel();
        interactor = new SeriesInteractor(model);
        viewBuilder = new SeriesViewBuilder(model, interactor::saveSeries);
    }

    public Region getView(){
        return viewBuilder.build();
    }
}
