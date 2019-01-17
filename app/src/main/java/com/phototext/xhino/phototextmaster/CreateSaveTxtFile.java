package com.phototext.xhino.phototextmaster;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateSaveTxtFile {

    private Context mContext;

    public CreateSaveTxtFile(Context context){
        mContext=context;



    }


    public void save(String textFileName , String text ){
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(storageDir, textFileName+".txt");

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            try {
                stream.write(text.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }







}
