package com.example.nadhir;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainAdapterViewer> {
    Context context;
    ArrayList<String> arrayList;
    private final RecyclerItem recyclerItem;
    DatabaseReference reference;

    public MainAdapter(Context context, ArrayList<String> arrayList, RecyclerItem recyclerItem) {
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
        holder.name.setText(arrayList.get(position));
        //
        int pos = position;
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(context)
                        .setMessage("This action will delete the data selected on the cloud")
                        .setPositiveButton("continue", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                reference = FirebaseDatabase.getInstance().getReference("Nadhir");
                                reference.child(arrayList.get(pos)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            deleteItem(pos);
                                            Toast.makeText(context, "Data Removed", Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(context, "An error occurred ,please check your internet connection", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
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
        TextView name;
        ImageView imageView;
        public MainAdapterViewer(@NonNull View itemView,RecyclerItem recyclerItem1) {
            super(itemView);
            name = itemView.findViewById(R.id.text_name_viewer);
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

    void deleteItem(int pos){
        //removing the deleted item from the arraylist
        arrayList.remove(pos);
        //notify the item removal
        notifyItemRemoved(pos);
        //checking on the listener of the event and notify it
        if (recyclerItem != null){
            recyclerItem.onItemDeleted();
        }

    }
}
