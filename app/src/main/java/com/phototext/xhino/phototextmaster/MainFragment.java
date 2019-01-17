package com.phototext.xhino.phototextmaster;

import android.Manifest;


import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
//import org.apache.commons.codec.binary.Base64;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.CLIPBOARD_SERVICE;

/*
This fragments takes a photo from camera or our gallery
makes a request to the cloud vision api
returns image's text
gives you some option (save- copy text , exit)
by swapping you "visit" the other fragment

 */





public class MainFragment extends Fragment implements View.OnClickListener{

    private static final String CLOUD_VISION_API_KEY = BuildConfig.API_KEY;
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_DIMENSION = 1200;

    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private static final String TAG = "MainFragment";
    private TextView mImageDetails;
    private ImageView mMainImage;
    public Uri photoUri;
    private static ImageButton savebtn;
    private static ImageButton uploadbtn;
    private static ImageButton  back;
    private static ImageButton  copybtn;
    static View rootview;
    private String imageFileName="";
    private CreateSaveTxtFile mCreateSaveTxtFile;
    private  ProgressBar spinner;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View rootview  = inflater.inflate(R.layout.fragment1_upload_to_drive_layout, container, false);
        Log.d(TAG, "onCreateView: mpika");

        mImageDetails = rootview.findViewById(R.id.image_details);
        mMainImage = rootview.findViewById(R.id.main_image);
        savebtn = (ImageButton) rootview.findViewById(R.id.savebtn);
        uploadbtn = (ImageButton) rootview.findViewById(R.id.uploadbtn);
        copybtn = (ImageButton) rootview.findViewById(R.id.copytextbtn);
        back = (ImageButton) rootview.findViewById(R.id.backToLogIn);
        mCreateSaveTxtFile = new CreateSaveTxtFile(getActivity().getApplicationContext());
        spinner = (ProgressBar) rootview.findViewById(R.id.progressBar1);


        FloatingActionButton fab = rootview.findViewById(R.id.fab);
       fab.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setMessage(R.string.dialog_select_prompt)
                  .setPositiveButton(R.string.dialog_select_gallery, (dialog, which) -> startGalleryChooser())
                    .setNegativeButton(R.string.dialog_select_camera, (dialog, which) -> startCamera());
          builder.create().show();
        });


        back.setOnClickListener(this);
        copybtn.setOnClickListener(this);
        savebtn.setOnClickListener(this);
        uploadbtn.setOnClickListener(this);


        Log.d(TAG, "onCreateView: started.");


        return rootview;
    }



    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.savebtn:
                mCreateSaveTxtFile.save(imageFileName,mImageDetails.getText().toString());
                Log.d("pleasework", imageFileName + "   " + mImageDetails.getText().toString());
                Toast.makeText(getActivity(), "file saved", Toast.LENGTH_SHORT).show();
                break;


            case R.id.uploadbtn:
                Toast.makeText(getActivity(), "upload", Toast.LENGTH_SHORT).show();
                break;

            case R.id.backToLogIn:
                LogInScreen.SignOutAcc();
                Intent LogInScreenIntent = new Intent(getContext(), LogInScreen.class);
                LogInScreenIntent.putExtra("SignOUt", "1");
                startActivity(LogInScreenIntent);
                getActivity().finish();
                break;

            case R.id.copytextbtn:
                String text = mImageDetails.getText().toString();
                ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(CLIPBOARD_SERVICE);
                clipboardManager.setText(text);
                Toast.makeText(getActivity(), "Text copied", Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }

    }



    public void startGalleryChooser() {
        Log.d("xhinooo", "startGalleryChooser:  1 ");
       if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
           requestPermissions( //Method of Fragment
                   new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                   GALLERY_PERMISSIONS_REQUEST
           );
       }

       else{
            Log.d("xhinooo", "startGalleryChooser: 2 ");
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }


    public void startCamera() {

        if((ActivityCompat.checkSelfPermission(getContext(),  Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(getContext(),  Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)||
                (ActivityCompat.checkSelfPermission(getContext(),  Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)){
            requestPermissions( //Method of Fragment
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA},
                    CAMERA_PERMISSIONS_REQUEST
            );
        }
        else
            {
                Log.d(TAG, "startCamera: 1");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoUri = FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getApplicationContext().getPackageName() + ".provider", CreateCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    @Override
    //Handling permissions
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if ( (grantResults[0] == PackageManager.PERMISSION_GRANTED) &&(grantResults[1] == PackageManager.PERMISSION_GRANTED )&& (grantResults[2] == PackageManager.PERMISSION_GRANTED) ) {
                    startCamera();
                }
                else
                {}
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGalleryChooser();
                }
                break;
        }
    }


    //Creating an image file
    //saving it at android/data/app_name/file/pictures
    public File CreateCameraFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         imageFileName = "PhotoText_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            Log.d(TAG, "CreateCameraFile: in ");
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            if(image!=null){
                Log.d(TAG, "CreateCameraFile: created ");
            }
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            //Getting the name of this image.
            //Will need it if we save the file
            File f = new File(data.getData().getPath());
            imageFileName = f.getName();

            //uploading image to get stuff back
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            uploadImage(photoUri);
        }
    }


    //Setting image on our fragment
    //and calling a method that uploads it to the cloud
    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri),
                                MAX_DIMENSION);

                callCloudVision(bitmap);
                mMainImage.setImageBitmap(bitmap);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(getActivity(), R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(getActivity(), R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }


    //Get the bitmap (image) and starts the async task which annotates the request and  does (request,response,parse)
    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        mImageDetails.setX(200);
        mImageDetails.setWidth(700);
        mImageDetails.setHeight(50);
        mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            spinner.setVisibility(rootview.VISIBLE);
            //setting visibility off
            copybtn.setVisibility(rootview.INVISIBLE);
            uploadbtn.setVisibility(rootview.INVISIBLE);
            savebtn.setVisibility(rootview.INVISIBLE);
            AsyncTask<Object, Void, String> textDetectionTask = new   TextDetectionTask((MainActivity) getActivity(), prepareAnnotationRequest(bitmap),spinner,imageFileName);  //??????????
            textDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    //Here is the magic!
    //Here we create our http request
    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getActivity().getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getActivity().getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        //forming our request
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();

            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but not Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            // Base64 encode the JPEG
            String imgBytes = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            base64EncodedImage.setContent(imgBytes);

            //adding the encoded image to our Annotated image request

            annotateImageRequest.setImage(base64EncodedImage);
            // add the features we want
            //Our json response will be based on what "features" we add/set
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature textDetection = new Feature();
                textDetection.setType("TEXT_DETECTION");
                add(textDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});


        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");


        return annotateRequest;
    }



    //Making our request to the cloud vision
    //Parsing the response
    public static class TextDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<MainActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;
        private ProgressBar spinner;
        private String filename;
        TextDetectionTask(MainActivity activity, Vision.Images.Annotate annotate,ProgressBar mspinner,String Filename ) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
            spinner = mspinner;
            filename=Filename;
        }

        @Override
        //Sending req and parsing resp
        protected String doInBackground(Object... params) {
            try {


                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                //Parsing our response (Json)
                JsonParser jsonParser = new JsonParser(response);
                jsonParser.ParseJson();
                return jsonParser.getText();

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute: in");
            spinner.setVisibility(View.GONE);
            MainActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                TextView imageDetail = activity.findViewById(R.id.image_details);
                int i = (int) imageDetail.getX();
                imageDetail.setX(40);

                activity.SetPhotoText(result);
                activity.SetFileName(filename);
                imageDetail.setHeight(800);
                imageDetail.setWidth(1000);
                imageDetail.setText(result);

                //setting visibility on
                copybtn.setVisibility(rootview.VISIBLE);
                uploadbtn.setVisibility(rootview.VISIBLE);
                savebtn.setVisibility(rootview.VISIBLE);

                Log.d(TAG, "onPostExecute: " +result );
            }
            else{
                Log.d(TAG, "onPostExecute: nulltext");
            }
        }
    }

    //Scales our bitmap so that we will "save" on bandwidth
    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.mymenu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.SignOut:
                LogInScreen.SignOutAcc();
                openLogInActivity();

                break;
        }

        return true;
    }

    public void openLogInActivity() {

        Intent LogInScreenIntent = new Intent(getActivity(), LogInScreen.class);
        LogInScreenIntent.putExtra("SignOUt", "1");
        startActivity(LogInScreenIntent, ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        getActivity().finish();

    }




}