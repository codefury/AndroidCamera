package com.codefury16.androidcamera;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ImageConstant {


    public ImageConstant() {
    }

    public static ArrayList<String> getImages() {
        String ExternalStorageDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        String targetPath = ExternalStorageDirectoryPath + "/AndroidCamera/";
        ArrayList<String> itemList = new ArrayList<String>();
        File targetDirector = new File(targetPath);
        File[] files = targetDirector.listFiles();
        for (File file : files) {
            itemList.add("file:///" + file.getAbsolutePath());
        }
        Collections.sort(itemList);
        Collections.reverse(itemList);
        return itemList;
    }

    public static class Extra {
        public static final String FRAGMENT_INDEX = "com.codefury16.androidcamera.FRAGMENT_INDEX";
        public static String IMAGE_POSITION = "com.codefury16.androidcamera.IMAGE_POSITION";
    }
}
