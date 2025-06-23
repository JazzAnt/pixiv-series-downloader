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

    private InputStream getInputStreamFromImageLink(String imageLink) throws URISyntaxException, IOException {
        URLConnection connection = new URI(imageLink).toURL().openConnection();
        connection.setRequestProperty("Referer", "https://www.pixiv.net");
        return connection.getInputStream();
    }

    private String getImageIDFromImageLink(String imageLink){
        String[] pathArray = imageLink.split("/");
        return pathArray[pathArray.length - 1];
    }

    private boolean downloadChapterAsZip(){
        try (FileOutputStream fos = new FileOutputStream(chapterDirectory);
             ZipOutputStream zipOut = new ZipOutputStream(fos);)
        {
            for(String link : downloadLinks){
                try(InputStream inputStream = getInputStreamFromImageLink(link)){
                    ZipEntry zipEntry = new ZipEntry(getImageIDFromImageLink(link));
                    zipOut.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while((length = inputStream.read(buffer)) != -1){
                        zipOut.write(buffer, 0, length);
                    }
                }
                catch (URISyntaxException e){
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean downloadChapterAsFolder(){
        new File(chapterDirectory).mkdirs();
        for(String link : downloadLinks){
            try(ReadableByteChannel rbc = Channels.newChannel(getInputStreamFromImageLink(link));
                FileOutputStream fos = new FileOutputStream(chapterDirectory + "\\" + getImageIDFromImageLink(link));
                FileChannel fileChannel = fos.getChannel();)
            {
                fileChannel.transferFrom(rbc,0, Long.MAX_VALUE);
            }
            catch (IOException | URISyntaxException e){
               return false;
            }
        }
        return true;
    }

    private byte[] getByteArrayFromImageLink(String imageLink) throws URISyntaxException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = getInputStreamFromImageLink(imageLink);
        int length;
        byte[] buffer = new byte[1024];

        while((length = inputStream.read(buffer)) != -1){
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public boolean downloadChapterAsPdf(){
        PDDocument document = new PDDocument();
        for(String link : downloadLinks){
            try {
                PDImageXObject image = PDImageXObject.createFromByteArray(document, getByteArrayFromImageLink(link), getImageIDFromImageLink(link));
                PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                document.addPage(page);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.drawImage(image, 0, 0);
                contentStream.close();
            }
            catch (URISyntaxException | IOException e){
                return false;
            }
        }
        try {
            document.save(chapterDirectory);
        }
        catch (IOException e){
            return false;
        }
        return true;
    }
}
