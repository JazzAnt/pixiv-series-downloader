package org.jazzant.pixivseriesdownloader;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class AddSeriesController {
    @FXML private TextField seriesLink;
    @FXML private Text testText;

    @FXML
    protected void handleParseButtonAction(ActionEvent actionEvent) {
        if(!Series.checkSeriesLinkFormat(seriesLink.getText())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("The link format is incorrect!\n" +
                    "Should be something like '.../user/000000/series/000000'");
            alert.show();
            seriesLink.setDisable(false);
            return;
        }
        seriesLink.setDisable(true);
        Series series = new Series();
        series.setSeriesLink(seriesLink.getText());
        Parser.setSeries(series);
        Parser.goToSeries();
        if(!Parser.seriesExists()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("This series does not exist!\n" +
                    "Either the link is incorrect or the series has been deleted");
            alert.show();
            seriesLink.setDisable(false);
            return;
        }
        Parser.parseSeriesDetails();
        testText.setText(series.getTitle() + "Parsed");
        //TODO: open add-series-parsed-fxml and pass the series
    }
}
