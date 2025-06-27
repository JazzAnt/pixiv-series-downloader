package org.jazzant.pixivseriesdownloader;

import java.util.ArrayList;

public class Chapter {
    private String title;
    private String uploadDate;
    private int pixivID;
    private int chapterNumber;
    private int pageAmount;
    private ArrayList<String> imageLinks;

    public boolean isValid(){
        if(title == null || title.isBlank() ||
                uploadDate == null || uploadDate.isBlank() ||
                pixivID < 1 ||
                chapterNumber < 1 ||
                pageAmount < 1 ||
                imageLinks == null || imageLinks.isEmpty()
        ) return false;
        return true;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public int getPixivID() {
        return pixivID;
    }

    public void setPixivID(int pixivID) {
        this.pixivID = pixivID;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public int getPageAmount() {
        return pageAmount;
    }

    public void setPageAmount(int pageAmount) {
        this.pageAmount = pageAmount;
    }

    public ArrayList<String> getImageLinks() {
        return imageLinks;
    }

    public void setImageLinks(ArrayList<String> imageLinks) {
        this.imageLinks = imageLinks;
    }
}
