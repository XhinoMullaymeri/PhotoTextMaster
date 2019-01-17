/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.phototext.xhino.phototextmaster;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();

    //for fragment
    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;
    //

    private String textotpass="";
    private String filename="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: MainActivityLoaded");


        //fragment stuff agagin
        mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager( mViewPager);
        setViewPager(0);
        Log.d(TAG, "onCreate: MainActivityFragment loaded");
    }


    //Adding fragments to our adapter
    private void setupViewPager(ViewPager viewPager){
        SectionsStatePagerAdapter adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MainFragment(), "Fragment1");
        adapter.addFragment(new GoogleTranslateFragment(), "Fragment2");
        viewPager.setAdapter(adapter);
    }

    //Swapping fragments
    public void setViewPager(int fragmentNumber){
        Log.d(TAG, "setViewPager: ");
        mViewPager.setCurrentItem(fragmentNumber);
    }



    //We are using these methods in order our fragments to communicate with each other
    // (they send data on MainActivity and then the other fragment gets the data )
    public void SetFileName(String text){
        filename = text;
        return;
    }


    public String GetFileName(){
        return filename;
    }


    public void SetPhotoText(String text){
        textotpass = text;
        return;
    }


    public String GetText(){
        return textotpass;
    }



}
