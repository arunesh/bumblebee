package com.chaibytes.bumblebee.backend;

import android.content.Context;

import com.chaibytes.bumblebee.data.MotionData;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by arunesh on 11/4/17.
 */

public class MotionDataLoader {
    private static final String OFFLINE_PATH = "offline";
    private Context context;
    private File myExternalFile;

    public MotionDataLoader(Context context, String absolutePath) {
        this.context = context;
        getExternalFile(absolutePath);
    }

    public void getExternalFile(String filepath) {
        // myExternalFile = new File(context.getExternalFilesDir(CloudFirestoreDatabase.FILE_PATH), filename);
        myExternalFile = new File(filepath);
    }

    public ArrayList<MotionData> readAll() {
        ArrayList<MotionData> dataList = new ArrayList<>();
        try {
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                MotionData data = MotionData.fromString(strLine);
                dataList.add(data);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public static ArrayList<String> listFiles(Context context) {
        File dir = context.getExternalFilesDir(OFFLINE_PATH);
        File listFile[] = dir.listFiles();
        ArrayList<String> fileList = new ArrayList<>();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                if (!listFile[i].isDirectory()) {// if its a directory need to get the files under that directory
                    //Do what ever u want
                    fileList.add(listFile[i].getAbsolutePath());
                }
            }
        }
        return fileList;
    }
}
