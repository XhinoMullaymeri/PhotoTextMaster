package com.phototext.xhino.phototextmaster;

import android.content.ClipboardManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
// Imports the Google Cloud client library
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translate.TranslateOption;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;


import static android.content.Context.CLIPBOARD_SERVICE;
import static com.phototext.xhino.phototextmaster.MainFragment.rootview;

/*
This fragment takes the text from the previous one and allows us
to translate it in many languages (google cloud translate api request).
It also gives us some options on the translated text (copy save)
 */


public class GoogleTranslateFragment extends Fragment implements View.OnClickListener {
    private static final String GOOGLE_API_KEY = BuildConfig.API_KEY;
    private static final String TAG = "TranslateFragment";
    private TextView translateText ;
    private ImageButton translatebtn;
    private ArrayList<CountryItem> mCountryList;

    private CountryAdapter mAdapter;
    private  String from="";
    private  String to="";

    private static ImageButton savebtntranslate;
    private static ImageButton uploadbtntranslate;
    private static ImageButton  backtranslate;
    private static ImageButton  copybtntranslate;
    private CreateSaveTxtFile mCreateSaveTxtTranslatedFile;
    private String imageFileName;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View rootview  = inflater.inflate(R.layout.fragment2_translate_layout, container, false);
        Log.d(TAG, "onCreateView: in");


        savebtntranslate  = rootview.findViewById(R.id.savebtntranslate);
        uploadbtntranslate = rootview.findViewById(R.id.uploadbtntranslate);
        backtranslate = rootview.findViewById(R.id.backToLogInTranslate);
        copybtntranslate = rootview.findViewById(R.id.copytextbtntranslate);
        translatebtn =  (ImageButton) rootview.findViewById(R.id.translatebtn);


        savebtntranslate.setOnClickListener(this);
                uploadbtntranslate.setOnClickListener(this);
        backtranslate.setOnClickListener(this);
                copybtntranslate.setOnClickListener(this);
        translatebtn.setOnClickListener(this);


        mCreateSaveTxtTranslatedFile = new CreateSaveTxtFile(getActivity().getApplicationContext());

        translateText = rootview.findViewById(R.id.TranslateTextView);

        translatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: .");
                startAsyncTask(rootview);
            }
        });

        //initializing our country list
        initList();
        //spinnners adapter
        mAdapter = new CountryAdapter(rootview.getContext(), mCountryList);
        //spinners
        Spinner fromSpinnerCountries = rootview.findViewById(R.id.FromspinnerCountries);
        Spinner tospinnerCountries = rootview.findViewById(R.id.tospinnerCountries);
        tospinnerCountries.setAdapter(mAdapter);
        fromSpinnerCountries.setAdapter(mAdapter);
        fromSpinnerCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CountryItem clickedItem = (CountryItem) parent.getItemAtPosition(position);
                from= clickedItem.getCountryCode();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        tospinnerCountries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CountryItem clickedItem = (CountryItem) parent.getItemAtPosition(position);
                to= clickedItem.getCountryCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

            return rootview;

        }


//buttons functionality
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.savebtntranslate:
                imageFileName=((MainActivity)getActivity()).GetFileName();
                mCreateSaveTxtTranslatedFile.save("translated"+imageFileName,translateText.getText().toString());
                Log.d("pleasework", imageFileName + "   " + translateText.getText().toString());
                Toast.makeText(getActivity(), "file saved", Toast.LENGTH_SHORT).show();
                break;



            case R.id.uploadbtntranslate:
                Toast.makeText(getActivity(), "upload", Toast.LENGTH_SHORT).show();
                break;

            case R.id.backToLogInTranslate:
                LogInScreen.SignOutAcc();
                Intent LogInScreenIntent = new Intent(getContext(), LogInScreen.class);
                LogInScreenIntent.putExtra("SignOUt", "1");
                startActivity(LogInScreenIntent);
                getActivity().finish();
                break;

            case R.id.copytextbtntranslate:
                String text = translateText.getText().toString();
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                clipboardManager.setText(text);
                Toast.makeText(getActivity(), "Text copied", Toast.LENGTH_SHORT).show();
                break;

            case R.id.translatebtn:
                startAsyncTask(rootview);
                break;

            default:
                break;
        }

    }




//initializing our list
    private void initList(){
        mCountryList = new ArrayList<>();
        mCountryList.add(new CountryItem("AL" ,"sq", R.drawable.albania));
        mCountryList.add(new CountryItem("DE" ,"de", R.drawable.germany));
        mCountryList.add(new CountryItem("EL" ,"el", R.drawable.greece));
        mCountryList.add(new CountryItem("ES" ,"es", R.drawable.spain));
        mCountryList.add(new CountryItem("FR" ,"fr", R.drawable.france));
        mCountryList.add(new CountryItem("IT" ,"it" ,R.drawable.italy));
        mCountryList.add(new CountryItem("RU" , "ru",R.drawable.russia));
        mCountryList.add(new CountryItem("UK" ,"en", R.drawable.uk));
        mCountryList.add(new CountryItem("any" , "",R.drawable.world));
    }


    //translating our text
    public void startAsyncTask(View v) {

        savebtntranslate.setVisibility(rootview.INVISIBLE);
        copybtntranslate.setVisibility(rootview.INVISIBLE);
        uploadbtntranslate.setVisibility(rootview.INVISIBLE);
        ExampleAsyncTask task = new ExampleAsyncTask((MainActivity) getActivity());
        task.execute(from,to);
    }

    private static class ExampleAsyncTask extends AsyncTask<String, Void, String> {
        private WeakReference<MainActivity> activityWeakReference;

        ExampleAsyncTask(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        protected String doInBackground(String... inputs) {

            MainActivity activity = activityWeakReference.get();
            String text = activity.GetText();



            if(inputs[0]!=inputs[1]) {
            // Instantiates a client
                Translate translate = TranslateOptions.newBuilder().setApiKey(GOOGLE_API_KEY).build().getService();
                Translation translation=null;

                //Google translate return a string without the \n so i replace \n with a sequence and when google translate returns the answer i replace again the
                //sequence with \n so that it looks like the initial text ;)
                String lines[] = text.split("\\r?\\n");
                text="";
                for(String line :  lines) {
                    text+= line + "%%%";
                }

                    try {
                        translation = translate.translate(
                                text,
                                TranslateOption.sourceLanguage(inputs[0].toLowerCase()),
                                TranslateOption.targetLanguage(inputs[1].toLowerCase())
                        );

                        text = translation.getTranslatedText();

                        text = text.replace("%%%", "\n");
                        text = text.replace("&#39;", "'");
                        return text;
                    }catch (Exception e){
                    return "ERROR maybe invalid key";
                    }

        }
        else {
            return "Select a Language \n\n";
        }


        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: " + s);


            MainActivity activity = activityWeakReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            savebtntranslate.setVisibility(rootview.VISIBLE);
            copybtntranslate.setVisibility(rootview.VISIBLE);
            uploadbtntranslate.setVisibility(rootview.VISIBLE);


            TextView translatedTextView = activity.findViewById(R.id.TranslateTextView);
            translatedTextView.setHeight(1150);
            translatedTextView.setWidth(1000);
            translatedTextView.setText(s);
            Log.d(TAG, "TranslateText: "+s);

        }
    }


}
