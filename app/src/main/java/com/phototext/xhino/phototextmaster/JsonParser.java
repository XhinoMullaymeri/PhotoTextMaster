package com.phototext.xhino.phototextmaster;

import android.util.Log;

import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {

    private static final String TAG = "JsonParser";
    private String Text = "";
    private BatchAnnotateImagesResponse response=null;

    public JsonParser(BatchAnnotateImagesResponse response) {
        this.response=response;
    }


    //Parsing Json actually we only take the text (photo's text)
    public void ParseJson(){

        try {
            JSONObject obj=new JSONObject(response.toString());
            JSONArray responseJson = (JSONArray) obj.get("responses");
            obj= responseJson.getJSONObject(0);
            Text=obj.getJSONObject("fullTextAnnotation").getString("text");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "ParseJson: Fail");
        }

    }

    public String getText() {
        return Text;
    }
}
