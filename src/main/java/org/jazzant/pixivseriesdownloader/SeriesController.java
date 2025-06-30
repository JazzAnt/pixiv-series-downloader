package org.jazzant.pixivseriesdownloader;

import javafx.scene.layout.Region;


public class SeriesController {
    private SeriesViewBuilder viewBuilder;
    private SeriesInteractor interactor;

    public SeriesController(){
        SeriesModel model = new SeriesModel();
        interactor = new SeriesInteractor(model);
        viewBuilder = new SeriesViewBuilder(model, interactor::saveSeries, interactor::parseSeries);
    }

    public Region getView(){
        return viewBuilder.build();
    }
}
