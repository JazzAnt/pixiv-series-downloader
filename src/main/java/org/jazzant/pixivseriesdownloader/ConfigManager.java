package org.jazzant.pixivseriesdownloader;

import java.io.*;
import java.util.Properties;

public class ConfigManager {
    private final String FILENAME = "app.config";
    public final String KEY_LIBRARY = "LIBRARY";
    public final String KEY_SAVEAS = "SAVEAS";

    public String getProperty(String key) {
        Properties properties = new Properties();
        try(FileInputStream inputStream = new FileInputStream(FILENAME)){
            properties.load(inputStream);
            return properties.getProperty(key);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void setProperty(String key, String value){
        Properties properties = new Properties();
        try(FileInputStream inputStream = new FileInputStream(FILENAME)){
            properties.load(inputStream);
            properties.setProperty(key,value);
            properties.store(new FileOutputStream(FILENAME), null);
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public boolean configExists(){
        File file = new File(FILENAME);
        if(!file.exists()) return false;
        if(getProperty(KEY_LIBRARY) == null || getProperty(KEY_SAVEAS) == null) return false;
        return true;
    }

    public void createConfigFile() throws IOException {
        File file = new File(FILENAME);
        file.createNewFile();
    }
}
