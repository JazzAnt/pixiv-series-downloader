package org.jazzant.pixivseriesdownloader;

public enum FilePath {
    RESOURCE("/org/jazzant/pixivseriesdownloader/");

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
