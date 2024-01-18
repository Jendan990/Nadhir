package com.example.nadhir;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class InsideOutDataAdapter extends RecyclerView.Adapter<InsideOutDataAdapter.InsideOutDataAdapterViewer> {

    Context context;
    ArrayList<CloudData> arrayList;
    private final RecyclerItem recyclerItem;
    public static final long CONSTANT = 86400000;

    public InsideOutDataAdapter(Context context, ArrayList<CloudData> arrayList, RecyclerItem recyclerItem) {
        this.context = context;
        this.arrayList = arrayList;
        this.recyclerItem = recyclerItem;
    }

    @NonNull
    @Override
    public InsideOutDataAdapterViewer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.inside_out_adapter,parent,false);
        return new InsideOutDataAdapterViewer(view,recyclerItem);
    }

    @Override
    public void onBindViewHolder(@NonNull InsideOutDataAdapterViewer holder, int position) {
        holder.roomID.setText(arrayList.get(position).getRoomNumber());
        //calculating days remained
        Date date = arrayList.get(position).getDateOut();
        long remainTime = (date.getTime() - Calendar.getInstance().getTime().getTime())/CONSTANT;
        holder.endTime.setText(String.valueOf(remainTime));

        int pos = position;
        holder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setMessage("This action will remove tenant details from your device and cloud")
                    .setPositiveButton("continue", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean stat = new NadhirDBHelper(context).deleteTenant(arrayList.get(pos).getHouseNumber(),arrayList.get(pos).getRoomNumber());
                            if (stat){
                                Toast.makeText(context, "tenant deleted on local", Toast.LENGTH_SHORT).show();
                                //for the cloud delete
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Nadhir/"+arrayList.get(pos).getHouseNumber());
                                String id = arrayList.get(pos).getHouseNumber().concat("_").concat(arrayList.get(pos).getRoomNumber());
                                reference.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(context, "tenant removed from database", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(context, "tenant removal failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }else {
                                Toast.makeText(context, "deletion failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton("cancel",null)
                    .create()
                    .show();

            return true;
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class InsideOutDataAdapterViewer extends RecyclerView.ViewHolder{
        TextView roomID;
        Chip endTime;
        public InsideOutDataAdapterViewer(@NonNull View itemView, RecyclerItem recyclerItem) {
            super(itemView);
            roomID = itemView.findViewById(R.id.text_room_id);
            endTime = itemView.findViewById(R.id.chip_days_remained);
            itemView.setOnClickListener(view->{
                if (recyclerItem != null){
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        recyclerItem.onClicked(pos);
                    }
                }
            });
        }
    }
}
