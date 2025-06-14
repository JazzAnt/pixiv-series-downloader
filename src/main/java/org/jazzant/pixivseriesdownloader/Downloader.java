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

public class Downloader {
    private static final int SAVE_AS_CBZ = 0;
    private static final int SAVE_AS_ZIP = 1;
    private static final int SAVE_AS_FOLDER = 2;
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

    public Downloader(int saveAs, String libraryDir){
        this.saveAs = saveAs;
        this.libraryDir = libraryDir;
    }

    public void setSaveAs(int saveAs){this.saveAs = saveAs;}
    public void setGroupDir(String groupDir){this.groupDir = groupDir;}
    public void setSeriesDir(String seriesDir){this.seriesDir = seriesDir;}
    public void setChapterName(String chapterName){this.chapterName = chapterName;}
    public void setDownloadLinks(ArrayList<String> downloadLinks){this.downloadLinks = downloadLinks;}

    public void downloadChapter(){
        if(saveAs == Downloader.SAVE_AS_CBZ || saveAs == Downloader.SAVE_AS_ZIP){
            downloadChapterAsZip();
        }
        if(saveAs == Downloader.SAVE_AS_FOLDER){
            downloadChapterAsFolder();
        };
    }

    public void downloadChapterAsZip(){
        String directory = libraryDir + "\\" + groupDir + "\\" + seriesDir;
        String format;
        if(saveAs == Downloader.SAVE_AS_CBZ) {
            format = ".cbz";
        }
        else {
            format = ".zip";
        }
        new File(directory).mkdirs();
        String chapterDirectory = directory + "\\" + chapterName + format;
        try {
            FileOutputStream fos = new FileOutputStream(chapterDirectory);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            for(String link : downloadLinks){
                URLConnection connection = new URI(link).toURL().openConnection();
                connection.addRequestProperty("Referer", "https://www.pixiv.net");
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
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void downloadChapterAsFolder(){
        String directory = libraryDir + "\\" + groupDir + "\\" + seriesDir;

        new File(directory).mkdirs();
        String chapterDirectory = directory + "\\" + chapterName;
        new File(chapterDirectory).mkdirs();
        try {
            for(String link : downloadLinks){
                URLConnection connection = new URI(link).toURL().openConnection();
                connection.addRequestProperty("Referer", "https://www.pixiv.net");
                String[] pathArray = connection.getURL().getPath().split("/");
                String filename = pathArray[pathArray.length - 1];
                String fileDirectory = chapterDirectory + "\\" + filename;
//              (Old buffer reliant downloader code, kept in case the new one fails)
//                FileOutputStream fos = new FileOutputStream(fileDirectory);
//                InputStream is = connection.getInputStream();
//
//                byte[] buffer = new byte[1024];
//                int length;
//                while((length = is.read(buffer)) != -1){
//                    fos.write(buffer, 0, length);
//                }
//                is.close();
//                fos.close();
                ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
                FileOutputStream fos = new FileOutputStream(fileDirectory);
                FileChannel fileChannel = fos.getChannel();
                fileChannel.transferFrom(rbc,0, Long.MAX_VALUE);
                fileChannel.close();
                fos.close();
                rbc.close();
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
