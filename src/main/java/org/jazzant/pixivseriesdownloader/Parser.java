package org.jazzant.pixivseriesdownloader;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final String PIXIV_URL = "https://www.pixiv.net";
    private final WebDriver driver;
    //set by setters
    private int implicitWaitTime;
    private String seriesLink;
    private String chapterNameFormat;
    //set by parseSeriesDetails()
    private String seriesTitle;
    private String seriesArtist;
    //set by parseChapterDetails()
    private String currentLink;
    private String chapterTitle;
    private String chapterPixivID;
    private String chapterUploadDate;
    private int chapterNumber;
    private int chapterPageAmount;
    private ArrayList<String> chapterImageLinks;

    public Parser(){
        driver = new ChromeDriver();
        setImplicitWaitTime(15);
        chapterNameFormat = "";
        chapterImageLinks = new ArrayList<>();
    }

    public ArrayList<String> getChapterImageLinks(){
        return chapterImageLinks;
    }
    public String getSeriesArtist(){
        return seriesArtist;
    }
    public String getSeriesTitle(){
        return seriesTitle;
    }

    /**
     * Modifies the amount of wait time the driver will wait for the JS elements to appear before failing (Default is 15 seconds)
     * @param implicitWaitTime the amount of wait time in seconds
     */
    public void setImplicitWaitTime(int implicitWaitTime){
        this.implicitWaitTime = implicitWaitTime;
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(this.implicitWaitTime));
    }

    /**
     * Attempts to go to the series link given in the parameter, and sets the series link if successful.
     * @param seriesLink The series link, the one that's formatted (pixiv.net/user/___/series/___)
     */
    public void setSeries(String seriesLink){
        driver.get(seriesLink);
        this.seriesLink = seriesLink;
        currentLink = this.seriesLink;
    }

    /**
     * Sets the formatting of the chapter
     * @param chapterNameFormat the format of the chapter name
     */
    public void setChapterNameFormat(String chapterNameFormat){
        this.chapterNameFormat = chapterNameFormat;
    }

    /**
     * Check if the current series exists. Use to check if the artist had deleted this particular series.
     * Works by checking for the existence of the "page not found" element.
     * @return true if the series exists, false if it's not.
     */
    public boolean seriesExists(){
        return !driver.findElement(By.tagName("h1")).getText().equals("Page not found");
    }

    /**
     * Parses the series title and artist and sets their corresponding variables.
     */
    public void parseSeriesDetails(){
        parseSeriesTitle();
        parseSeriesArtist();
    }

    /**
     * Parses the series title and sets its variable.
     */
    private void parseSeriesTitle(){
        if(currentLink.equals(seriesLink))
            seriesTitle = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[5]/div[1]/div/div[1]/main/div[2]/section/div/div[1]/div[1]"))
                    .getText();
        else {
            String[] tempArray = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[1]/div/figcaption/div[2]/div/div[2]/a"))
                    .getText().split("#");
            tempArray[tempArray.length - 1] = "";
            seriesTitle = String.join("", tempArray).trim();
        }
    }

    /**
     * Parses the series artist name and sets its variable.
     */
    private void parseSeriesArtist(){
        if(currentLink.equals(seriesLink))
            seriesArtist = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[5]/div[1]/div/div[1]/aside/section[1]/h2/div/div/div/a/div"))
                    .getText();
        else
            seriesArtist = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[2]/div/div/div[1]/h2/div/div/a/div"))
                    .getText();
    }

    /**
     * Goes to the next chapter, or the first chapter if the current page is the series page.
     */
    public void goToNextChapter(){
        if(currentLink.equals(seriesLink))
            currentLink = PIXIV_URL + driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[5]/div[1]/div/div[1]/main/div[2]/section/div/div[1]/div[3]/div/a"))
                    .getDomAttribute("href");
        else
            currentLink = PIXIV_URL + driver
                    .findElement(By.className("gtm-series-next-work-button-in-illust-detail"))
                    .getDomAttribute("href");
        driver.get(currentLink);
    }

    /**
     * Parses the current chapter's title, chapter number, chapter pixiv ID, page amount, image links and sets
     * them to their respective variables.
     */
    public void parseChapterDetails(){
        parseChapterTitle();
        parseChapterID();
        parseChapterUploadDate();
        parseChapterNumber();
        parseChapterPageCount();
        parseImageLinks();
    }

    /**
     * Parses the current chapter title and sets its variable.
     */
    private void parseChapterTitle(){
        chapterTitle = driver.findElement(By.tagName("h1")).getText();
    }

    /**
     * Parses the current chapter ID and sets it's variable.
     */
    private void parseChapterID(){
        String[] tempArray = currentLink.split("/");
        chapterPixivID = tempArray[tempArray.length-1];
    }

    /**
     * Parses the current chapter upload date and sets it's variable
     */
    private void parseChapterUploadDate(){
        chapterUploadDate = driver.findElement(By.tagName("time"))
                .getDomAttribute("datetime").split("T")[0];
    }

    /**
     * Parses the current chapter number and sets it's variable
     */
    private void parseChapterNumber(){
        String[] tempArray = driver
                .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[1]/div/figcaption/div[2]/div/div[2]/a"))
                .getText().split("#");
        chapterNumber = Integer.parseInt(tempArray[tempArray.length - 1]);
    }

    /**
     * Parses the current chapter page count and sets it's variable
     */
    private void parseChapterPageCount(){
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        List<WebElement> tempList = driver.findElements(By.className("gtm-manga-viewer-open-preview"));
        if(tempList.isEmpty()){ //this code is done because the page counter doesn't appear on single-page chapters
            chapterPageAmount = 1;
        }
        else{
            String[] tempArray = tempList.getFirst()
                    .findElement(By.tagName("div"))
                    .findElement(By.tagName("span"))
                    .getText().split("/");
            chapterPageAmount = Integer.parseInt(tempArray[tempArray.length - 1]);
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitTime));
    }

    /**
     * Parses the current chapter image download links and sets it's variable
     */
    private void parseImageLinks(){
        chapterImageLinks.clear();
        //Get the image link and split it in two
        String temp = driver
                .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[1]/div/figure/div/div[2]/div/div/img"))
                .getDomAttribute("src");
        for(int page = 0; page< chapterPageAmount; page++){
            chapterImageLinks.add(temp
                    .replace("0_master1200", page+"")
                    .replace("master", "original"));
        }
    }

    /**
     * Checks if the parser is currently in the latest chapter.
     * @return true if it's the latest chapter, and false if not.
     */
    public boolean isLatestChapter(){
        if(currentLink.equals(seriesLink)) return false;
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        boolean isLatestChapter = driver.findElements(By.className("gtm-series-next-work-button-in-illust-detail")).isEmpty();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitTime));
        return isLatestChapter;
    }

    /**
     * Generates the chapter name for the file download, by default formatted as something like (Chapter123-title) but
     * can be given a formatter to make a custom filename. The formatter must contain either the chapter title, number,
     * id, or upload date or else it will use the default format (because if it doesn't contain at least one of the
     * aforementioned parameters, it might end up using the same name for every chapter).
     * @return formatted filename if it meets the formatting requirements, and the default filename otherwise
     */
    public String generateFileName(){
        if(chapterNameFormat.contains("{chapter_title}")
        || chapterNameFormat.contains("{chapter_number}")
        || chapterNameFormat.contains("{chapter_id}")
        || chapterNameFormat.contains("{upload_date}"))
            return chapterNameFormat
               .replace("{chapter_title}", chapterTitle)
               .replace("{chapter_number}", chapterNumber +"")
               .replace("{chapter_id}", chapterPixivID)
               .replace("{upload_date}", chapterUploadDate)
               .replace("{page_amount}", chapterPageAmount +"")
               .replace("{artist}", seriesArtist)
               .replace("{series_title}", seriesTitle);

        return "Chapter" + chapterNumber + "_" + chapterTitle;
    }
}
