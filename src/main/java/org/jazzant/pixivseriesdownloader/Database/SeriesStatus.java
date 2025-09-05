package org.jazzant.pixivseriesdownloader;

import org.openqa.selenium.InvalidArgumentException;

public enum SeriesStatus {
    ONGOING(0),
    COMPLETED(1),
    HIATUS(2),
    PAUSED(3),
    DELETED(4);

    private final int code;
    SeriesStatus(int code){this.code = code;}
    public int getCode() {
        return code;
    }
    public static SeriesStatus getStatusFromCode(int code){
        return switch (code) {
            case 0 -> ONGOING;
            case 1 -> COMPLETED;
            case 2 -> HIATUS;
            case 3 -> PAUSED;
            case 4 -> DELETED;
            default -> throw new InvalidArgumentException("Code does not correspond to any SeriesStatus");
        };
    }
}
