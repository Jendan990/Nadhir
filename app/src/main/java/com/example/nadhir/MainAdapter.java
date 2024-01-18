package com.example.nadhir;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainAdapterViewer> {
    Context context;
    ArrayList<MainData> arrayList;
    private final RecyclerItem recyclerItem;

    public MainAdapter(Context context, ArrayList<MainData> arrayList, RecyclerItem recyclerItem) {
        this.context = context;
        this.arrayList = arrayList;
        this.recyclerItem = recyclerItem;
    }

    @NonNull
    @Override
    public MainAdapterViewer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_adapter,parent,false);
        return new MainAdapterViewer(view,recyclerItem);
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapterViewer holder, int position) {
        MainData data = arrayList.get(position);
        holder.name.setText(data.getName());
        holder.location.setText(data.getLocation());
        //
        int pos = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setMessage("This action will delete the data selected from device and cloud")
                        .setPositiveButton("continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new NadhirDBHelper(context).deleteHouseData(context,arrayList.get(pos).getName());
                            }
                        })
                        .setNegativeButton("cancel",null)
                        .create()
                        .show();
                return true;
            }
        });


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class MainAdapterViewer extends RecyclerView.ViewHolder {
        TextView name,location;
        ImageView imageView;
        public MainAdapterViewer(@NonNull View itemView,RecyclerItem recyclerItem1) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name_viewer);
            location = itemView.findViewById(R.id.text_location_viewer);
            imageView = itemView.findViewById(R.id.image_house_icon);

            itemView.setOnClickListener(view->{
                if (recyclerItem1 != null){
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION){
                        recyclerItem1.onClicked(pos);
                    }
                }
            });
        }
    }
}
