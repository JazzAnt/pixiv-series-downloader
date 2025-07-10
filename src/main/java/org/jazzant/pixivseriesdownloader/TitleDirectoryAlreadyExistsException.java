package org.jazzant.pixivseriesdownloader;

public class TitleDirectoryAlreadyExistsException extends DatabaseException {
    public TitleDirectoryAlreadyExistsException(String titleDirectory) {
        super("Title Directory " + titleDirectory + "already exists in the database.");
    }
}
