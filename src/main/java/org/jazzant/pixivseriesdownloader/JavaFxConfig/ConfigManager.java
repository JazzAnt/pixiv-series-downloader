package org.jazzant.pixivseriesdownloader.JavaFxConfig;

import org.jazzant.pixivseriesdownloader.Downloader.SaveAs;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigManager {
    private final String CONFIG_FILENAME = "app.config";
    public final String KEY_LIBRARY = "LIBRARY";
    public final String KEY_SAVEAS = "SAVEAS";
    public final String KEY_FILENAME_FORMAT = "FILENAMEFORMAT";

    public String getProperty(String key) {
        Properties properties = new Properties();
        try(FileInputStream inputStream = new FileInputStream(CONFIG_FILENAME)){
            properties.load(inputStream);
            return properties.getProperty(key);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void setProperty(String key, String value){
        Properties properties = new Properties();
        try(FileInputStream inputStream = new FileInputStream(CONFIG_FILENAME)){
            properties.load(inputStream);
            properties.setProperty(key,value);
            properties.store(new FileOutputStream(CONFIG_FILENAME), null);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void removeProperty(String key){
        Properties properties = new Properties();
        try(FileInputStream inputStream = new FileInputStream(CONFIG_FILENAME)){
            properties.load(inputStream);
            properties.remove(key);
            properties.store(new FileOutputStream(CONFIG_FILENAME), null);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void createConfigFile() throws IOException {
        File file = new File(CONFIG_FILENAME);
        file.createNewFile();
    }

    public boolean configIsValid(){
        if(!configExists()) return false;
        if(!libraryValueIsValid()) return false;
        if(!saveAsValueIsValid()) return false;
        if(!filenameFormatValueIsValid()) return false;

        return true;
    }

    private boolean configExists(){
        File file = new File(CONFIG_FILENAME);
        return file.exists();
    }

    private boolean libraryValueIsValid(){
        String libraryDirectory = getProperty(KEY_LIBRARY);
        if(libraryDirectory == null) return false;

        Path libraryPath = Paths.get(libraryDirectory);
        if(!Files.exists(libraryPath) || !Files.isDirectory(libraryPath)) return false;

        return true;
    }

    private boolean saveAsValueIsValid(){
        String saveAs = getProperty(KEY_SAVEAS);
        if(saveAs == null) return false;

        try {
            SaveAs.valueOf(saveAs);}
        catch (IllegalArgumentException _) {
            return false;
        }
        return true;
    }

    private boolean filenameFormatValueIsValid(){
        String filenameFormat = getProperty(KEY_FILENAME_FORMAT);
        if(filenameFormat == null) return false;

        return filenameFormatIsValid(filenameFormat);
    }

    public boolean filenameFormatIsValid(String filenameFormat){
        return filenameFormat.contains("{chapter_id}") || filenameFormat.contains("{chapter_number}");
    }
}
