package com.example.nadhir;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements RecyclerItem {
    private static final int PERMISSION_REQUEST = 100;
    private GoogleSignInClient signInClient;
    private GoogleSignInOptions signInOptions;
    MainAdapter mainAdapter;
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    ActivityResultLauncher<Intent> resultLauncher;
    ArrayList<String> mainDataArrayList;
    ArrayList<CloudData> cloudDataArrayList;
    TextInputEditText house_name;
    Dialog bottom;
    private DatabaseReference reference,reference1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar bar = getSupportActionBar();
        bar.setTitle("Luvanda Enterprises");
        bar.setElevation(0);
        swipeRefreshLayout = findViewById(R.id.swiper);
        recyclerView = findViewById(R.id.recycler);
        cloudDataArrayList = new ArrayList<>();


        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode()== Activity.RESULT_OK){
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
                        if (Environment.isExternalStorageManager()){
                            Toast.makeText(MainActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this, "permission denied", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(MainActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        //configuring the google sign in option
        signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(this,signInOptions);

        // calling 'checkPermission()' to see if permission for file access on the device
        checkPermission();
        //obtaining data to view
        obtainData();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            obtainData();
            swipeRefreshLayout.setRefreshing(false);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        MenuItem logout = menu.findItem(R.id.logout);
        logout.setOnMenuItemClickListener(menuItem ->{
            signInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    finish();
                    startActivity(new Intent(MainActivity.this,SignInActivity.class));
                }
            });
            return true;
        });

        MenuItem add_new = menu.findItem(R.id.add_new);
        add_new.setOnMenuItemClickListener(menuItem -> {
            addingNewHouse();
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void requestPermission(){
        //asking for permission from the device that an app requires
        //Permissions required : internet permission (done in manifest file) and storage access permission.
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            //requesting permission for android 9 and higher
            try{
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",new Object[]{getApplicationContext().getPackageName()})));
                resultLauncher.launch(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                resultLauncher.launch(intent);
            }

        }else {
            //below android version 9
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST);
        }



    }

    public void checkPermission(){
        //check if permission are granted if not call 'requestPermission()'
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            if(Environment.isExternalStorageManager()){
                //do nothing
            }else {
                requestPermission();
            }
        }else {
            int CHECK_READ = ContextCompat.checkSelfPermission(getApplicationContext(),READ_EXTERNAL_STORAGE);
            if(CHECK_READ == PackageManager.PERMISSION_GRANTED){
                //do nothing
            }else {
                requestPermission();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case PERMISSION_REQUEST:
                if (grantResults.length > 0){
                    boolean resultPackage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(resultPackage){
                        Toast.makeText(MainActivity.this, "Permission granted", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MainActivity.this, "Permission denied by user", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void addingNewHouse(){
         //creating a bottom navigator for adding the house variables
        bottom = new BottomSheetDialog(MainActivity.this);
        bottom.requestWindowFeature(Window.FEATURE_NO_TITLE);
        bottom.setContentView(R.layout.house_variables);

        //initializing components of the bottom sheet
        house_name = bottom.findViewById(R.id.text_name_or_number);
        Button confirm  = bottom.findViewById(R.id.btn_confirm);
        ImageView cancel =  bottom.findViewById(R.id.btn_cancel);

        Objects.requireNonNull(confirm).setOnClickListener(e->{
                onConfirmAddition(house_name.getText().toString());
        });

        Objects.requireNonNull(cancel).setOnClickListener(e->{
            bottom.dismiss();
        });

        bottom.show();
        bottom.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        bottom.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void onConfirmAddition(String name){
        //adding main house data to cloud.
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if (name != null){
            reference1 = database.getReference("Nadhir");
            reference1.child(name).setValue("").addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Toast.makeText(MainActivity.this, "Data Added Successful", Toast.LENGTH_SHORT).show();
                    house_name.setText("");
                    bottom.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Data Addition failed,please retry or check internet connection", Toast.LENGTH_SHORT).show();
                }
            });

        }else {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage("one or both of the fields are empty")
                    .setPositiveButton("Ok",null)
                    .create()
                    .show();
        }

    }

    public void obtainData(){
        reference = FirebaseDatabase.getInstance().getReference("Nadhir");
        mainDataArrayList = new ArrayList<>();
        mainAdapter = new MainAdapter(MainActivity.this,mainDataArrayList,this);
        recyclerView.setAdapter(mainAdapter);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        
        //loading data from firebase database.
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //for this section will obtain keys only (house numbers or names)
                for (DataSnapshot snap :
                        dataSnapshot.getChildren()) {
                    String _names = snap.getKey();
                    mainDataArrayList.add(_names);
                }
                mainAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //on load failing
                Toast.makeText(MainActivity.this, "Data loading failed", Toast.LENGTH_SHORT).show();
            }
        });
        
    }

    @Override
    public void onClicked(int pos) {
        Intent intent = new Intent(MainActivity.this, InsideOut.class);
        intent.putExtra("house_name",mainDataArrayList.get(pos));
        startActivity(intent);
    }

    @Override
    public void onItemDeleted() {
        swipeRefreshLayout.setRefreshing(true);
        //obtaining new data set
        obtainData();
        swipeRefreshLayout.setRefreshing(false);
    }
}