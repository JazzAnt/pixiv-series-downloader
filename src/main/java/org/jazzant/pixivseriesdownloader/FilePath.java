package org.jazzant.pixivseriesdownloader;

public enum FilePath {
    DATA_FOLDER("PixivSeriesDownloader_Data"),
    DATABASE_FILE(DATA_FOLDER + "/PixivSeriesDownloader.db"),
    CONFIG_FILE(DATA_FOLDER + "/app.config"),
    COOKIE_FILE(DATA_FOLDER + "/loginCookie.ser"),
    RESOURCE_FOLDER("/org/jazzant/pixivseriesdownloader/");

    private final String path;

    FilePath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return path;

    }
}
