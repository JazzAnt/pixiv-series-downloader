module org.jazzant.pixivseriesdownloader {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.seleniumhq.selenium.support;
    requires org.seleniumhq.selenium.firefox_driver;
    requires org.apache.pdfbox;
    requires java.sql;


    opens org.jazzant.pixivseriesdownloader to javafx.fxml;
    exports org.jazzant.pixivseriesdownloader;
    exports org.jazzant.pixivseriesdownloader.Deprecated;
    opens org.jazzant.pixivseriesdownloader.Deprecated to javafx.fxml;
}