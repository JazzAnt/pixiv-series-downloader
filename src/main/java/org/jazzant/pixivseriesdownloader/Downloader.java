package org.jazzant.pixivseriesdownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Downloader{
    public static final int SAVE_AS_CBZ = 0;
    public static final int SAVE_AS_ZIP = 1;
    public static final int SAVE_AS_FOLDER = 2;
    private int saveAs;

    private ArrayList<String> downloadLinks;
    private String chapterName;

    /**
     * Directory Format
     * ****************
     * >>Library
     * >>>>Group1
     * >>>>>>Series1
     * >>>>>>>>Chapter1
     * >>>>>>>>Chapter2
     * >>>>>>Series2
     * >>>>>>>>>Chapter1
     * >>>>>>>>>Chapter2
     * >>>>Group2
     * etc
     */
    private String libraryDir;
    private String groupDir;
    private String seriesDir;
    private String seriesDirectory;
    private String chapterDirectory;

    public Downloader(int saveAs, String libraryDir){
        this.saveAs = saveAs;
        this.libraryDir = libraryDir;
        groupDir = "";
        seriesDir = "";
        chapterName = "";
    }

    public void setSaveAs(int saveAs){this.saveAs = saveAs;}
    public void setGroupDir(String groupDir){this.groupDir = groupDir;}
    public void setSeriesDir(String seriesDir){this.seriesDir = seriesDir;}
    public void setChapterName(String chapterName){this.chapterName = chapterName;}
    public void setDownloadLinks(ArrayList<String> downloadLinks){this.downloadLinks = downloadLinks;}

    public boolean downloadChapter(){
        if(libraryDir.isBlank() || seriesDir.isBlank() || chapterName.isBlank())
            throw new IllegalArgumentException("Directories are not properly set");

        seriesDirectory = generateDirectory();
        if(saveAs == Downloader.SAVE_AS_CBZ) {
            chapterDirectory = seriesDirectory + "\\" + chapterName + ".cbz";
            return downloadChapterAsZip();
        }
        if(saveAs == Downloader.SAVE_AS_ZIP) {
            chapterDirectory = seriesDirectory + "\\" + chapterName + ".zip";
            return downloadChapterAsZip();
        }
        if(saveAs == Downloader.SAVE_AS_FOLDER) {
            chapterDirectory = seriesDirectory + "\\" + chapterName;
            return downloadChapterAsFolder();
        }
        return false;
    }

    private String generateDirectory(){
        String directory = libraryDir + "\\" + groupDir + "\\" + seriesDir;
        new File(directory).mkdirs();
        return directory;
    }

    private boolean downloadChapterAsZip(){
        try {
            FileOutputStream fos = new FileOutputStream(chapterDirectory);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            for(String link : downloadLinks){
                URLConnection connection = new URI(link).toURL().openConnection();
                connection.setRequestProperty("Referer", "https://www.pixiv.net");

                String[] pathArray = connection.getURL().getPath().split("/");
                String filename = pathArray[pathArray.length - 1];

                ZipEntry zipEntry = new ZipEntry(filename);
                zipOut.putNextEntry(zipEntry);

                InputStream inputStream = connection.getInputStream();
                byte[] buffer = new byte[1024];
                int length;
                while((length = inputStream.read(buffer)) != -1){
                    zipOut.write(buffer, 0, length);
                }
                inputStream.close();
            }
            zipOut.close();
            fos.close();
            return true;
        } catch (IOException | URISyntaxException e) {
            return false;
        }
    }

    public boolean downloadChapterAsFolder(){
        new File(chapterDirectory).mkdirs();
        try {
            for(String link : downloadLinks){
                URLConnection connection = new URI(link).toURL().openConnection();
                connection.setRequestProperty("Referer", "https://www.pixiv.net");

                String[] pathArray = connection.getURL().getPath().split("/");
                String filename = pathArray[pathArray.length - 1];
                String fileDirectory = chapterDirectory + "\\" + filename;

                ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
                FileOutputStream fos = new FileOutputStream(fileDirectory);
                FileChannel fileChannel = fos.getChannel();
                fileChannel.transferFrom(rbc,0, Long.MAX_VALUE);

                fileChannel.close();
                fos.close();
                rbc.close();
            }
            return true;
        } catch (IOException | URISyntaxException e) {
            return false;
        }
    }
}
