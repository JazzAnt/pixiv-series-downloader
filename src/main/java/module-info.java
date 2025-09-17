module org.jazzant.pixivseriesdownloader {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.seleniumhq.selenium.support;
    requires org.seleniumhq.selenium.firefox_driver;
    requires org.apache.pdfbox;
    requires java.sql;


    opens org.jazzant.pixivseriesdownloader to javafx.fxml;
    exports org.jazzant.pixivseriesdownloader;
    exports org.jazzant.pixivseriesdownloader.Parser;
    opens org.jazzant.pixivseriesdownloader.Parser to javafx.fxml;
    exports org.jazzant.pixivseriesdownloader.Downloader;
    opens org.jazzant.pixivseriesdownloader.Downloader to javafx.fxml;
    exports org.jazzant.pixivseriesdownloader.Database;
    opens org.jazzant.pixivseriesdownloader.Database to javafx.fxml;
    exports org.jazzant.pixivseriesdownloader.JavaFxAddSeries;
    opens org.jazzant.pixivseriesdownloader.JavaFxAddSeries to javafx.fxml;
    exports org.jazzant.pixivseriesdownloader.JavaFxConfig;
    opens org.jazzant.pixivseriesdownloader.JavaFxConfig to javafx.fxml;
    exports org.jazzant.pixivseriesdownloader.JavaFxDbViewer;
    opens org.jazzant.pixivseriesdownloader.JavaFxDbViewer to javafx.fxml;
    exports org.jazzant.pixivseriesdownloader.JavaFxDownload;
    opens org.jazzant.pixivseriesdownloader.JavaFxDownload to javafx.fxml;
    exports org.jazzant.pixivseriesdownloader.JavaFxLogin;
    opens org.jazzant.pixivseriesdownloader.JavaFxLogin to javafx.fxml;
    exports org.jazzant.pixivseriesdownloader.JavaFxMain;
    opens org.jazzant.pixivseriesdownloader.JavaFxMain to javafx.fxml;
}