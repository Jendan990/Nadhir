package com.example.nadhir;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class OutsideFragment extends Fragment implements RecyclerItem{
    private RecyclerView recyclerView;
    private ArrayList<CloudData> arrayList;
    private FloatingActionButton floatingActionButton;
    private TextInputEditText tenantName,tenantNumber,rentPrice,roomID,endDate;
    private TextInputLayout endDatePicker;
    private DatePickerDialog pickerDialog;
    private Button btnAdd;
    private Date date;
    private Dialog dialog;
    private DatabaseReference databaseReference;
    InsideOutDataAdapter insideOutDataAdapter;
    NadhirDBHelper helper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_outside, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.outside_recycler);
        floatingActionButton = view.findViewById(R.id.fab_outside);
        databaseReference = FirebaseDatabase.getInstance().getReference("Nadhir/"+getActivity().getIntent().getStringExtra("house_name"));
        helper = new NadhirDBHelper(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        arrayList = new ArrayList<>();
        //obtaining data
        obtainData();
        //adding new data to the system
        floatingActionButton.setOnClickListener(e->{
            addData();
        });
    }

    public void obtainData(){
        arrayList = helper.getAllDetails2(getContext(),getActivity().getIntent().getStringExtra("house_name"));
        insideOutDataAdapter = new InsideOutDataAdapter(getContext(),arrayList,this);
        recyclerView.setAdapter(insideOutDataAdapter);
        insideOutDataAdapter.notifyDataSetChanged();
    }

    public void addData(){
        //creating the bottom sheet
        dialog = new BottomSheetDialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_data_register);

        //initializing the sheet components
        tenantName = dialog.findViewById(R.id.text_tenant_name_reg);
        tenantNumber = dialog.findViewById(R.id.text_tenant_number_reg);
        rentPrice = dialog.findViewById(R.id.text_rent_price_reg);
        roomID = dialog.findViewById(R.id.text_room_id_reg);
        endDate = dialog.findViewById(R.id.text_end_date_reg);
        endDatePicker = dialog.findViewById(R.id.layout_end_date);
        btnAdd = dialog.findViewById(R.id.btn_add_data);

        //lunching a datePickerDialogue
        endDatePicker.setEndIconOnClickListener(e->{
            dateMaker();
        });

        btnAdd.setOnClickListener(e->{
            dataToDBCloud();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    }

    public void dataToDBCloud(){
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(endDate.getText().toString());
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }

        CloudData cloudData = new CloudData(getActivity().getIntent().getStringExtra("house_name"),"OutsideTenants",
                tenantName.getText().toString(),tenantNumber.getText().toString(),roomID.getText().toString(),
                getActivity().getIntent().getStringExtra("loc"),Double.valueOf(rentPrice.getText().toString()),date);

        boolean status = helper.dataAddCloudDB(cloudData);
        if (status){
            tenantName.setText("");
            tenantNumber.setText("");
            roomID.setText("");
            endDate.setText("");
            Toast.makeText(getContext(), "Data added successful", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        }else {
            Toast.makeText(getContext(), "unknown error occurred,data addition failed", Toast.LENGTH_SHORT).show();
        }

        //initiating cloud upload and status update on the local db
        if (cloudData.getRoomNumber() != null){
            String dataId = cloudData.getHouseNumber().concat("_").concat(cloudData.getRoomNumber());
            databaseReference.child(dataId).setValue(cloudData,((error, ref)->{
                if (error != null){
                    error.toException();
                    helper.updateStat(cloudData.getHouseNumber(),cloudData.getRoomNumber(),false);
                    Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                }else {
                    helper.updateStat(cloudData.getHouseNumber(),cloudData.getRoomNumber(),true);
                    Toast.makeText(getContext(), "Upload success", Toast.LENGTH_SHORT).show();
                }
            }));
        }

    }

    public void dateMaker(){
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                endDate.setText(i+"-"+(i1+1)+"-"+i2);

            }
        };

        Calendar calendar = Calendar.getInstance();

        pickerDialog = new DatePickerDialog(getContext(), android.app.AlertDialog.THEME_HOLO_LIGHT,dateSetListener,
                calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));

        pickerDialog.show();

    }

    @Override
    public void onClicked(int pos) {
        Intent intent = new Intent(getContext(), DetailsViewer.class);
        //ArrayList<CloudData> cloudData = helper.getAllDetails(getContext());
        intent.putExtra("cloud_house", arrayList.get(pos).getHouseNumber());
        intent.putExtra("cloud_location", arrayList.get(pos).getLocation());
        intent.putExtra("cloud_category", arrayList.get(pos).getCategory());
        intent.putExtra("cloud_tenant", arrayList.get(pos).getTenantName());
        intent.putExtra("cloud_phone", arrayList.get(pos).getTenantPhone());
        intent.putExtra("cloud_rent", arrayList.get(pos).getRentPrice());
        intent.putExtra("cloud_id", arrayList.get(pos).getRoomNumber());
        intent.putExtra("cloud_end", arrayList.get(pos).getDateOut());

        startActivity(intent);
    }
}