package com.example.nadhir;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInActivity extends AppCompatActivity {
    public static final String TAG = "Google_Auth";
    private GoogleSignInOptions signInOptions;
     private GoogleSignInClient signInClient;
     private FirebaseAuth firebaseAuth;
     private Button signInBtn;
     private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        signInBtn = findViewById(R.id.sign_in_btn);
        firebaseAuth = FirebaseAuth.getInstance();

        //configuring the google sign in option
        signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this,signInOptions);

        //checking if an account is signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null){
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
            finish();
        }

        //setting a button trigger action
        signInBtn.setOnClickListener(e->{
            signingIn();
        });

    }

    private void signingIn() {
        if (NetworkConnectionChecker.isConnected(this)){
            Intent signIn = signInClient.getSignInIntent();
            startActivityForResult(signIn,100);
            dialog = new ProgressDialog(SignInActivity.this);
            dialog.setIndeterminate(true);
            dialog.setMessage("please wait");
        }else {
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //checking the results from the intent
        if (requestCode == 100){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //if the sign in is success then authenticate with firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG,"Firebase Auth with google"+account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Toast.makeText(this, "Executing", Toast.LENGTH_SHORT).show();
        dialog.show();
        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            // Sign in success, send user to main activity
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Intent main = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(main);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            dialog.dismiss();
                        }
                    }
                });
    }


}