package com.phototext.xhino.phototextmaster;

public class CountryItem {
    private String mCountryName;
    private String mCountryCode;
    private int mFlagImage;

    public CountryItem(String countryname,String countrycode,int flagImage){
            mCountryName = countryname;
            mFlagImage =flagImage;
            mCountryCode=countrycode;
    }


    public String getCountryName(){
        return mCountryName;
    }

    public int getmFlagImage(){
        return mFlagImage;
    }

    public String getCountryCode(){
        return mCountryCode;
    }

}
