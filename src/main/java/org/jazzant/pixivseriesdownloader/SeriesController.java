package org.jazzant.pixivseriesdownloader;

import javafx.scene.layout.Region;
import javafx.util.Builder;


public class SeriesController {
    private SeriesViewBuilder viewBuilder;
    private SeriesInteractor interactor;

    public SeriesController(){
        SeriesModel model = new SeriesModel();
        viewBuilder = new SeriesViewBuilder(model);
        interactor = new SeriesInteractor(model);
    }

    public Region getView(){
        return viewBuilder.build();
    }
}
