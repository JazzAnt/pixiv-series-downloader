package org.jazzant.pixivseriesdownloader;

import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.util.Builder;

public class SeriesViewBuilder implements Builder<Region> {
    private final SeriesModel model;
    private final Runnable saveHandler;

    public SeriesViewBuilder(SeriesModel model, Runnable saveHandler){
        this.model = model;
        this.saveHandler = saveHandler;
    }

    @Override
    public Region build() {
        BorderPane view = new BorderPane();

        view.setCenter(createCenter());
        view.setBottom(saveButton());

        return view;
    }

    public Node createCenter(){
        VBox view = new VBox(
                seriesField(),
                groupField(),
                titleField()
        );

        return view;
    }

    public Node seriesField(){
        return new HBox(
                promptLabel("Series Link"),
                boundTextField(model.getSeriesLinkProperty())
        );
    }
    public Node groupField(){
        return new HBox(
                promptLabel("Group Name"),
                boundTextField(model.getDirectoryGroupProperty())
        );
    }
    public Node titleField(){
        return new HBox(
                promptLabel("Title"),
                boundTextField(model.getDirectoryTitleProperty())
        );
    }

    private Node saveButton(){
       Button saveButton = new Button("Save");
       saveButton.setOnAction(event->saveHandler.run());
       HBox view = new HBox(saveButton);
       view.setAlignment(Pos.CENTER_RIGHT);
       return view;
    }

    private Node boundTextField(StringProperty boundProperty){
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(boundProperty);
        return textField;
    }

    private Node promptLabel(String contents){
        return styledLabel(contents, "prompt-label");
    }

    private Node styledLabel(String contents, String styleClass){
        Label label = new Label(contents);
        label.getStyleClass().add(styleClass);
        return label;
    }
}
