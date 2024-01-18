package com.example.nadhir;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class DetailsViewer extends AppCompatActivity {
    private Chip houseId,location,room,price,tenantName,tenantNumber,endDate;
    private TextInputEditText tenantNam,tenantNumb,roomNumber,rentPrice,roomID,endDate2;
    private TextInputLayout endDatePicker;
    private Button button,update;
    private DatePickerDialog pickerDialog;
    private Date date;
    private DatabaseReference databaseReference;
    NadhirDBHelper helper;
    Dialog dialog;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_viewer);
        helper = new NadhirDBHelper(this);
        databaseReference = FirebaseDatabase.getInstance().getReference("Nadhir/"+getIntent().getStringExtra("cloud_house"));
        houseId = findViewById(R.id.chip_house_id);
        location = findViewById(R.id.chip_location);
        room = findViewById(R.id.chip_room_number);
        price = findViewById(R.id.chip_rent_price);
        tenantName = findViewById(R.id.chip_tenant_name);
        tenantNumber = findViewById(R.id.chip_tenant_number);
        endDate = findViewById(R.id.chip_end_date);
        button = findViewById(R.id.btnEdit);

        //extracting our extras
        String cloud_house = getIntent().getStringExtra("cloud_house");
        String cloud_location = getIntent().getStringExtra("cloud_location");
        String cloud_category = getIntent().getStringExtra("cloud_category");
        String cloud_tenant = getIntent().getStringExtra("cloud_tenant");
        String cloud_phone = getIntent().getStringExtra("cloud_phone");
        String cloud_id = getIntent().getStringExtra("cloud_id");
        Date cloud_end = (Date)getIntent().getSerializableExtra("cloud_end");
        double cloud_rent = getIntent().getDoubleExtra("cloud_rent",0.0);

        houseId.setText(cloud_house);
        location.setText(cloud_location);
        room.setText(cloud_id);
        price.setText(Double.toString(cloud_rent));
        tenantName.setText(cloud_tenant);
        tenantNumber.setText(cloud_phone);
        endDate.setText(dateFormat.format(cloud_end));

        button.setOnClickListener(e->{
            updateData();
        });

    }

    public void updateData(){
        //creating the bottom sheet
        dialog = new BottomSheetDialog(DetailsViewer.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.update_sheet);

        //initializing the sheet components
        tenantNam = dialog.findViewById(R.id.text_tenant_name_edit);
        tenantNumb = dialog.findViewById(R.id.text_tenant_number_edit);
        rentPrice = dialog.findViewById(R.id.text_rent_price_edit);
        roomID = dialog.findViewById(R.id.text_room_id_edit);
        endDate2 = dialog.findViewById(R.id.text_end_date_edit);
        endDatePicker = dialog.findViewById(R.id.layout_end_date);
        update = dialog.findViewById(R.id.btn_update_data);

        //packing data to items
        tenantNam.setText(getIntent().getStringExtra("cloud_tenant"));
        tenantNumb.setText(getIntent().getStringExtra("cloud_phone"));
        roomID.setText(getIntent().getStringExtra("cloud_id"));
        rentPrice.setText(Double.toString(getIntent().getDoubleExtra("cloud_rent",0.0)));
        endDate2.setText(getIntent().getSerializableExtra("cloud_end").toString());

        //lunching a datePickerDialogue
        endDatePicker.setEndIconOnClickListener(e->{
            dateMaker();
        });

        update.setOnClickListener(e->{
            updateDataToDBCloud();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    private void updateDataToDBCloud() {
        ProgressDialog progressDialog = new ProgressDialog(DetailsViewer.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("updating..,please wait...");
        progressDialog.show();
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(endDate2.getText().toString());
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }

        CloudData cloudData = new CloudData(getIntent().getStringExtra("cloud_house"),"OutsideTenants",
                tenantName.getText().toString(),tenantNumber.getText().toString(),getIntent().getStringExtra("cloud_id"),
                getIntent().getStringExtra("cloud_location"),Double.valueOf(rentPrice.getText().toString()),date);

        boolean status = helper.dataUpdateCloudDB(cloudData);
        if (status){
            tenantName.setText("");
            tenantNumber.setText("");
            roomID.setText("");
            endDate.setText("");
            Toast.makeText(DetailsViewer.this, "Data added successful", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            HashMap hashMap = new HashMap();
            hashMap.put("houseNumber",cloudData.getHouseNumber());
            hashMap.put("category",cloudData.getCategory());
            hashMap.put("tenantName",cloudData.getTenantName());
            hashMap.put("tenantPhone",cloudData.getTenantPhone());
            hashMap.put("roomNumber",cloudData.getRoomNumber());
            hashMap.put("location",cloudData.getLocation());
            hashMap.put("rentPrice",cloudData.getRentPrice());
            hashMap.put("dateOut",cloudData.getDateOut());

            String dataId = cloudData.getHouseNumber().concat("_").concat(cloudData.getRoomNumber());
            databaseReference.child(dataId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        helper.updateStat(cloudData.getHouseNumber(),cloudData.getRoomNumber(),true);
                        progressDialog.dismiss();
                        Toast.makeText(DetailsViewer.this, "cloud update successfully", Toast.LENGTH_SHORT).show();
                    }else {
                        helper.updateStat(cloudData.getHouseNumber(),cloudData.getRoomNumber(),false);
                        progressDialog.dismiss();
                        Toast.makeText(DetailsViewer.this, "cloud update failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }else {
            progressDialog.dismiss();
            Toast.makeText(DetailsViewer.this, "unknown error occurred,data addition failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void dateMaker(){
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                endDate2.setText(i+"-"+(i1+1)+"-"+i2);

            }
        };

        Calendar calendar = Calendar.getInstance();

        pickerDialog = new DatePickerDialog(DetailsViewer.this, android.app.AlertDialog.THEME_HOLO_LIGHT,dateSetListener,
                calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));

        pickerDialog.show();

    }

}