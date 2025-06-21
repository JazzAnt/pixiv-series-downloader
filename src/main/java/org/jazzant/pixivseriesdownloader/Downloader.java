package org.jazzant.pixivseriesdownloader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.*;
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
    public static final int SAVE_AS_PDF = 3;
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
        if(saveAs == Downloader.SAVE_AS_PDF){
            chapterDirectory = seriesDirectory + "\\" + chapterName + ".pdf";
            return downloadChapterAsPdf();
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

    public boolean downloadChapterAsPdf(){
        try(PDDocument document = new PDDocument()) {
            for(String link : downloadLinks){
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                URLConnection connection = new URI(link).toURL().openConnection();
                connection.setRequestProperty("Referer", "https://www.pixiv.net");
                InputStream inputStream = connection.getInputStream();
                int length;
                byte[] buffer = new byte[1024];

                while((length = inputStream.read(buffer)) != -1){
                    byteArrayOutputStream.write(buffer, 0, length);
                }

                byte[] byteArray = byteArrayOutputStream.toByteArray();

                String[] pathArray = connection.getURL().getPath().split("/");
                String filename = pathArray[pathArray.length - 1];

                PDImageXObject image = PDImageXObject.createFromByteArray(document, byteArray, filename);
                PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                document.addPage(page);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.drawImage(image, 0, 0);
                contentStream.close();
            }
            document.save(chapterDirectory);
            return true;
        } catch (IOException | URISyntaxException e) {
            return false;
        }


    }
}
