module org.jazzant.pixivseriesdownloader {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.seleniumhq.selenium.chrome_driver;


    opens org.jazzant.pixivseriesdownloader to javafx.fxml;
    exports org.jazzant.pixivseriesdownloader;
}