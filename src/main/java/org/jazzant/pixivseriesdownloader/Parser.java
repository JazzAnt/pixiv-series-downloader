package org.jazzant.pixivseriesdownloader;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private final String PIXIV_URL = "https://www.pixiv.net";
    private final String SERIES_LINK_REGEX = "/user/(\\d+)/series/(\\d+)$";
    private final WebDriver driver;
    private final WebDriver.Window window;
    private final WebDriverWait driverWait;
    private final WebDriverWait driverLongWait;
    private boolean isLoggedIn;
    //set by setters
    private String pixivUsername;
    private String pixivPassword;
    private int waitTime;
    private String artistID;
    private String seriesID;
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
    private final ArrayList<String> chapterImageLinks;

    public Parser(){
        FirefoxOptions options = new FirefoxOptions();
//        options.addArguments("--headless=new");
        options.addArguments("--width=400");
        options.addArguments("--height=500");
        driver = new FirefoxDriver(options);
        window = driver.manage().window();
        driverWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driverLongWait = new WebDriverWait(driver, Duration.ofSeconds(99));
        setWaitTime(10);
        isLoggedIn = false;
        chapterNameFormat = "";
        pixivUsername = "";
        pixivPassword = "";
        chapterImageLinks = new ArrayList<>();
    }

    public void close(){
        driver.close();
    }

    public void setPixivUsername(String pixivUsername){this.pixivUsername = pixivUsername;}
    public void setPixivPassword(String pixivPassword){this.pixivPassword = pixivPassword;}

    public boolean loginPixiv() {
        if(isLoggedIn) return true;
        goToLoginPage();
        if(enterUsernameField() && enterPasswordField()) {
            clickLoginButton();
            isLoggedIn = waitForLoginShort();
            if(isLoggedIn) return true;

            if(detectReCAPTCHA()){
                windowToFront();
                isLoggedIn = waitForLoginLong();
            }
        }
        else {
            windowToFront();
            isLoggedIn = waitForLoginLong();
        }
        return isLoggedIn;
    }

    private void goToLoginPage(){
        driver.get("https://accounts.pixiv.net/login");
    }
    private boolean enterUsernameField(){
        if(!driver.getCurrentUrl().contains("accounts.pixiv.net/login") || pixivUsername.isBlank())
            return false;

        driver.findElements(By.tagName("fieldset")).get(0)
                .findElement(By.tagName("input"))
                .sendKeys(pixivUsername);
        return true;
    }
    private boolean enterPasswordField(){
        if(!driver.getCurrentUrl().contains("accounts.pixiv.net/login") || pixivPassword.isBlank())
            return false;

        driver.findElements(By.tagName("fieldset")).get(1)
                .findElement(By.tagName("input"))
                .sendKeys(pixivPassword);
        return true;
    }
    private void clickLoginButton(){
        driver.findElement(By.id("app-mount-point"))
                .findElements(By.tagName("button")).get(5)
                .click();
    }
    private boolean waitForLoginShort(){
        try {
            driverWait.until(d -> driver.getCurrentUrl().contains("www.pixiv.net"));
            return true;
        }
        catch (TimeoutException _){
            return false;
        }
    }
    private boolean waitForLoginLong(){
        try {
            driverLongWait.until(d -> driver.getCurrentUrl().contains("www.pixiv.net"));
            return true;
        }
        catch (TimeoutException _){
            return false;
        }
    }
    private boolean detectReCAPTCHA(){
        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
        for(WebElement iframe : iframes){
            if(iframe.getDomAttribute("title") != null && iframe.getDomAttribute("title").equals("reCAPTCHA")){
                new Actions(driver).moveToElement(iframe).perform();
                return true;
            }
        }
        return false;
    }
    private void windowToFront(){
        Point position = window.getPosition();
        window.minimize();
        window.setPosition(position);
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
     * Modifies the amount of wait time the driver will wait for various operation attempts (such as waiting for
     * a webpage or web element to load) before giving up
     * @param waitTime the amount of wait time in seconds (Default is 10 seconds)
     */
    public void setWaitTime(int waitTime){
        this.waitTime = waitTime;
        driverWait.withTimeout(Duration.ofSeconds(this.waitTime));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(this.waitTime));
    }

    public boolean setSeries(String seriesLink){
        Pattern pattern = Pattern.compile(SERIES_LINK_REGEX);
        Matcher matcher = pattern.matcher(seriesLink.trim());
        if(matcher.find()){
            artistID = matcher.group(1);
            seriesID = matcher.group(2);
            return true;
        }
        else return false;
    }

    public String generateSeriesLink(){
        return PIXIV_URL + "/user/" + artistID + "/series/" + seriesID;
    }

    public void goToSeries(){
        driver.get(generateSeriesLink());
        currentLink = generateSeriesLink();
    }
    public boolean checkSeriesLink(String seriesLink){
        return seriesLink.trim().matches(SERIES_LINK_REGEX);
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
        if(currentLink.equals(generateSeriesLink()))
            seriesTitle = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[5]/div[1]/div/div[1]/main/div[2]/section/div/div[1]/div[1]"))
                    .getText();
        else {
            String[] tempArray;
            if(isLoggedIn)
                tempArray = driver
                        .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[5]/div[1]/div/div[1]/main/section/div[1]/div/figcaption/div[2]/div/div[2]/a"))
                        .getText().split("#");
            else
                tempArray = driver
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
        if(currentLink.equals(generateSeriesLink()))
            seriesArtist = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[5]/div[1]/div/div[1]/aside/section[1]/h2/div/div/div/a/div"))
                    .getText();
        else if(isLoggedIn){
            seriesArtist = driver
                    .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[5]/div[1]/div/div[1]/main/section/div[2]/div[1]/div/div[1]/h2/div/div/a/div"))
                    .getText();
        }
        else{
            seriesArtist = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[2]/div/div/div[1]/h2/div/div/a/div"))
                    .getText();
        }


    }

    /**
     * Goes to the next chapter, or the first chapter if the current page is the series page.
     */
    public void goToNextChapter(){
        if(!currentLink.equals(generateSeriesLink())){
            currentLink = PIXIV_URL + driver
                    .findElement(By.className("gtm-series-next-work-button-in-illust-detail"))
                    .getDomAttribute("href");
        }
        else if(isLoggedIn){
            currentLink = PIXIV_URL + driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[5]/div[1]/div/div[1]/main/div[2]/section/div/div[1]/div[4]/div/a"))
                    .getDomAttribute("href");
        }
        else {
            currentLink = PIXIV_URL + driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[5]/div[1]/div/div[1]/main/div[2]/section/div/div[1]/div[3]/div/a"))
                    .getDomAttribute("href");
        }


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
        String[] tempArray;
        if(isLoggedIn){
            tempArray = driver
                    .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[5]/div[1]/div/div[1]/main/section/div[1]/div/figcaption/div[2]/div/div[2]/a"))
                    .getText().split("#");
        }
        else {
            tempArray = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[1]/div/figcaption/div[2]/div/div[2]/a"))
                    .getText().split("#");
        }

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
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitTime));
    }

    /**
     * Parses the current chapter image download links and sets it's variable
     */
    private void parseImageLinks(){
        chapterImageLinks.clear();
        String temp;
        if(chapterPageAmount == 1 && isLoggedIn){
            temp = driver
                    .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[5]/div[1]/div/div[1]/main/section/div[1]/div/figure/div[1]/div[1]/div/a/img"))
                    .getDomAttribute("src");
        }
        else if(chapterPageAmount > 1 && isLoggedIn){
            temp = driver
                    .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[5]/div[1]/div/div[1]/main/section/div[1]/div/figure/div/div[2]/div/a/img"))
                    .getDomAttribute("src");
        }
        else if(chapterPageAmount == 1 && !isLoggedIn){
            temp = driver
                    .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[1]/div/figure/div[1]/div[1]/div/div/img"))
                    .getDomAttribute("src");
        }
        else{ //if(chapterPageAmount > 1 && !isLoggedIn)
            temp = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[1]/div/figure/div/div[2]/div/div/img"))
                    .getDomAttribute("src");
        }

        for(int page = 0; page< chapterPageAmount; page++){
            chapterImageLinks.add(temp
                    .replace("0_master1200", page+"")
                    .replace("master", "original")
                    .replace(".jpg", ".png"));
        }
    }

    /**
     * Checks if the parser is currently in the latest chapter.
     * @return true if it's the latest chapter, and false if not.
     */
    public boolean isLatestChapter(){
        if(currentLink.equals(generateSeriesLink())) return false;
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        boolean isLatestChapter = driver.findElements(By.className("gtm-series-next-work-button-in-illust-detail")).isEmpty();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitTime));
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
