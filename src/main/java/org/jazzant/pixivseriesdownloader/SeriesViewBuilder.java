package org.jazzant.pixivseriesdownloader;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
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
import javafx.util.converter.NumberStringConverter;

public class SeriesViewBuilder implements Builder<Region> {
    private final SeriesModel model;
    private final Runnable saveHandler;
    private final Runnable parseHandler;

    public SeriesViewBuilder(SeriesModel model, Runnable saveHandler, Runnable parseHandler){
        this.model = model;
        this.saveHandler = saveHandler;
        this.parseHandler = parseHandler;
    }

    @Override
    public Region build() {
        BorderPane view = new BorderPane();
        view.setTop(parseButton());
        view.setCenter(createCenter());
        view.setBottom(saveButton());

        return view;
    }

    public Node createCenter(){
        VBox view = new VBox(
                seriesLinkField(),
                groupDirField(),
                titleDirField(),
                titleField(),
                artistField()
        );
        return view;
    }

    public Node seriesLinkField(){
        return new HBox(
                promptLabel("Series Link"),
                boundTextField(model.getSeriesLinkProperty())
        );
    }
    public Node groupDirField(){
        return new HBox(
                promptLabel("Group Name"),
                boundTextField(model.getDirectoryGroupProperty())
        );
    }
    public Node titleDirField(){
        return new HBox(
                promptLabel("Title Name"),
                boundTextField(model.getDirectoryTitleProperty())
        );
    }

    public Node titleField(){
        return new HBox(
                promptLabel("Title"),
                boundTextField(model.getTitleProperty())
        );
    }
    public Node artistField(){
        return new HBox(
                promptLabel("Artist"),
                boundTextField(model.getArtistProperty())
        );
    }

    private Node parseButton(){
        Button parseButton = new Button("Parse");
        parseButton.setOnAction(event->parseHandler.run());
        HBox view = new HBox(parseButton);
        view.setAlignment(Pos.CENTER);
        return view;
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

    private Node boundTextField(IntegerProperty boundProperty){
        TextField textField = new TextField();
        Bindings.bindBidirectional(textField.textProperty(), boundProperty, new NumberStringConverter());
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
