package com.phototext.xhino.phototextmaster;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.api.gax.rpc.ApiException;


public class LogInScreen extends AppCompatActivity {

    static GoogleSignInClient mGoogleSignInClient;
    public String SignOUt = "0";
    Handler handler;
    private  ImageButton guestbtn;

    //checking if already signed in
    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
       if(account!=null) {

        GotoMainActivity(account);

       }
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_screen);

        guestbtn = (ImageButton) findViewById(R.id.guestbtn);


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);


        //String SignOUt = getIntent().getStringExtra("SignOUt");

        if (SignOUt == null) {
            SignOUt = "0";
        }

        if (SignOUt.equals("1")) {

            handler = new Handler(getApplicationContext().getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mGoogleSignInClient.signOut();
                }
            });

        }

        Log.d("SignOUt", "Value: " + SignOUt);


        this.findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                signIn();
            }
        });


        guestbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GotoMainActivity(null);
            }
        });

        Log.d("checkthis", "onCreate:4 ");
    }



    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,3600);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
           GotoMainActivity(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("xhino","InResult:failed code=" + e.getStatusCode());

        }
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==0){
            return;
        }
        if(requestCode == 3600) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            }

        }


        private  void GotoMainActivity(GoogleSignInAccount acc){
            GoogleSignInAccount account=acc;
            if(acc!=null){
                Toast.makeText(this, "email:" + account.getEmail() + " authenticated!", Toast.LENGTH_SHORT).show();
            }
            Intent MainActivityIntent = new Intent(this, MainActivity.class);

            startActivity(MainActivityIntent);

            LogInScreen.this.finishAfterTransition();
        }

    public  static void SignOutAcc(){
        mGoogleSignInClient.signOut();
    }


}
