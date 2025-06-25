package org.jazzant.pixivseriesdownloader;

import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Builder;

public class SeriesViewBuilder implements Builder<Region> {
    private final SeriesModel model;
    public SeriesViewBuilder(SeriesModel model){
        this.model = model;
    }

    @Override
    public Region build() {
        return new VBox();
    }
}
