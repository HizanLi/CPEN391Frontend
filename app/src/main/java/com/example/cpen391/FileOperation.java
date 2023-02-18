package com.example.cpen391;

import android.content.Context;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

public class FileOperation {

    private final String TAG = "FileOperation";
    private final String login = "login.json";
    private final String setting = "setting.json";
    private final String history = "history.json";

    public int writeToJson(Context applicationContext, JSONObject response, String fileName) {
        try {
            File file = new File(applicationContext.getFilesDir(), fileName);
            FileOutputStream writer = new FileOutputStream(file);
            writer.write(response.toString().getBytes());
            writer.close();
//            Log.d(TAG, "write to file" + fileName + " path is: " + file.getCanonicalPath());
        } catch (FileAlreadyExistsException e) {
            e.printStackTrace();
            return 1;
        } catch (IOException e) {
            e.printStackTrace();
            return 2;
        }
        return 0;
    }

    public String readFromJson(Context applicationContext, String fileName) {
        try {
            File file = new File(applicationContext.getFilesDir(), fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
            // This response will have Json Format String
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void cleanSearchCaches(Context applicationContext){
        if( applicationContext == null){
//            Log.d(TAG, "applicationContext Null");
            return;
        }

        File targetDir = applicationContext.getFilesDir();
        File[] files = targetDir.listFiles();

        if(files == null){
//            Log.d(TAG, "dir empty, nothing to clean");
            return;
        }

        for(File f : files){
            String filename = f.getName();
            if(!filename.contains("Search.json") || f.isDirectory()){
                continue;
            }
//            Log.d(TAG, "delete filename is: " + filename);
            f.delete();
        }
    }
}
