package org.jazzant.pixivseriesdownloader;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
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

import java.util.Objects;

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
        view.getStylesheets().add(Objects.requireNonNull(this.getClass().getResource("/css/series.css")).toExternalForm());
        view.setTop(createTop());
        view.setCenter(createCenter());
        view.setBottom(createBottom());
        view.setMinWidth(400);
        view.setMinHeight(400);
        return view;
    }

    private Node createTop(){
        HBox view = new HBox(10,
                headerLabel("PIXIV SERIES DOWNLOADER"));
        view.setAlignment(Pos.CENTER);
        return view;
    }

    private Node createCenter(){
        VBox view = new VBox(10,
            seriesLinkField(),
                groupDirField(),
                titleDirField(),
                titleField(),
                artistField()
        );
        view.setFillWidth(true);
        view.setAlignment(Pos.TOP_LEFT);
        return view;
    }

    private Node createBottom(){
        HBox view = new HBox(10,
                parseButton(),
                saveButton()
        );
        view.setAlignment(Pos.CENTER_RIGHT);
        return view;
    }

    private Node seriesLinkField(){
        return new HBox(5,
                promptLabel("Series Link"),
                boundTextField(model.getSeriesLinkProperty())
        );
    }
    private Node groupDirField(){
        return new HBox(5,
                promptLabel("Group Name"),
                boundTextField(model.getDirectoryGroupProperty())
        );
    }
    private Node titleDirField(){
        return new HBox(5,
                promptLabel("Title Name"),
                boundTextField(model.getDirectoryTitleProperty())
        );
    }

    private Node titleField(){
        return new HBox(5,
                promptLabel("Title"),
                boundLabel(model.getTitleProperty())
        );
    }
    private Node artistField(){
        return new HBox(5,
                promptLabel("Artist"),
                boundLabel(model.getArtistProperty())
        );
    }

    private Node parseButton(){
        Button parseButton = new Button("Parse");
        parseButton.setOnAction(event->parseHandler.run());
        return parseButton;
    }

    private Node saveButton(){
       Button saveButton = new Button("Save");
       saveButton.setOnAction(event->saveHandler.run());
       return saveButton;
    }

    private Node boundTextField(StringProperty boundProperty){
        TextField textField = new TextField();
        textField.textProperty().bindBidirectional(boundProperty);
        textField.setPrefWidth(300);
        return textField;
    }

    private Node boundTextField(IntegerProperty boundProperty){
        TextField textField = new TextField();
        Bindings.bindBidirectional(textField.textProperty(), boundProperty, new NumberStringConverter());
        textField.setPrefWidth(300);
        return textField;
    }

    private Node boundLabel(StringProperty boundProperty){
        Label label = (Label) styledLabel("", "bound-label");
        Bindings.bindBidirectional(label.textProperty(), boundProperty);
        return label;
    }

    private Node boundLabel(IntegerProperty boundProperty){
        Label label = (Label) styledLabel("", "bound-label");
        Bindings.bindBidirectional(label.textProperty(), boundProperty, new NumberStringConverter());
        return label;
    }

    private Node headerLabel(String contents){
        return styledLabel(contents, "header-label");
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
