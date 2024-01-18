package com.example.nadhir;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class NadhirDBHelper extends SQLiteOpenHelper {
    public static final String TABLE_HOUSES = "HOUSES";
    public static final String TABLE_APP_DETAILS = "APP_DETAILS";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_HOUSE_NAME = "HOUSE_NAME";
    public static final String COLUMN_LOCATION = "LOCATION";
    public static final String COLUMN_CATEGORY = "CATEGORY";
    public static final String COLUMN_TENANT_NAME = "TENANT_NAME";
    public static final String COLUMN_TENANT_PHONE = "TENANT_PHONE";
    public static final String COLUMN_ROOM_NUMBER = "ROOM_NUMBER";
    public static final String COLUMN_RENT_PRICE = "RENT_PRICE";
    public static final String COLUMN_END_TIME = "END_TIME";
    public static final String COLUMN_STATUS = "STATUS";
    SQLiteDatabase sqLiteDatabase;
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public NadhirDBHelper(@Nullable Context context) {
        super(context, "nadhirDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //table for house details in main activity
        String tableHouseID = "CREATE TABLE " + TABLE_HOUSES + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_HOUSE_NAME + " TEXT," + COLUMN_LOCATION + " TEXT)";
        //table for the cloud data and all house attributes
        String tableAllData = "CREATE TABLE " + TABLE_APP_DETAILS + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_HOUSE_NAME + " TEXT," + COLUMN_LOCATION + " TEXT," +
                COLUMN_CATEGORY + " TEXT," + COLUMN_TENANT_NAME + " TEXT," +
                COLUMN_TENANT_PHONE + " TEXT," + COLUMN_ROOM_NUMBER + " INTEGER," +
                COLUMN_RENT_PRICE + " CURRENCY," + COLUMN_END_TIME + " DATE," +
                COLUMN_STATUS + " BOOLEAN)";

        db.execSQL(tableHouseID);
        db.execSQL(tableAllData);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addMainHomeData(String name, String location){
        //initializing db
        sqLiteDatabase = this.getWritableDatabase();
        //content value creation
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_HOUSE_NAME,name);
        cv.put(COLUMN_LOCATION,location);
        //invoking insert method to add data to db
        long cursor = sqLiteDatabase.insert(TABLE_HOUSES,null,cv);
        if (cursor == -1){
            sqLiteDatabase.close();
            return false;
        }else {
            sqLiteDatabase.close();
            return true;
        }
    }

    public boolean updateMainHomeData(CloudData cloudData){
        //initializing db
        sqLiteDatabase = this.getWritableDatabase();
        //content value creation
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_HOUSE_NAME,cloudData.getHouseNumber());
        cv.put(COLUMN_LOCATION,cloudData.getLocation());
        //invoking insert method to add data to db
        long cursor = sqLiteDatabase.update(TABLE_HOUSES,cv,"HOUSE_NAME=?",new String[]{cloudData.getHouseNumber()});
        if (cursor == -1){
            sqLiteDatabase.close();
            return false;
        }else {
            sqLiteDatabase.close();
            return true;
        }
    }

    public boolean dataAddCloudDB(CloudData cloudData){
        //opening the database for access
        sqLiteDatabase = this.getWritableDatabase();
        //inputting dta to a content value
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_HOUSE_NAME,cloudData.getHouseNumber());
        contentValues.put(COLUMN_LOCATION,cloudData.getLocation());
        contentValues.put(COLUMN_CATEGORY,cloudData.getCategory());
        contentValues.put(COLUMN_TENANT_NAME,cloudData.getTenantName());
        contentValues.put(COLUMN_TENANT_PHONE,cloudData.getTenantPhone());
        contentValues.put(COLUMN_ROOM_NUMBER,cloudData.getRoomNumber());
        contentValues.put(COLUMN_RENT_PRICE,cloudData.getRentPrice());
        contentValues.put(COLUMN_END_TIME, dateFormat.format(cloudData.getDateOut()));
        //invoking insert method to add data to db
        long cursor = sqLiteDatabase.insert(TABLE_APP_DETAILS,null,contentValues);
        if (cursor == -1){
            sqLiteDatabase.close();
            return false;
        }else {
            sqLiteDatabase.close();
            return true;
        }

    }

    public boolean dataUpdateCloudDB(CloudData cloudData){
        //opening the database for access
        sqLiteDatabase = this.getWritableDatabase();
        //inputting dta to a content value
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_HOUSE_NAME,cloudData.getHouseNumber());
        contentValues.put(COLUMN_LOCATION,cloudData.getLocation());
        contentValues.put(COLUMN_CATEGORY,cloudData.getCategory());
        contentValues.put(COLUMN_TENANT_NAME,cloudData.getTenantName());
        contentValues.put(COLUMN_TENANT_PHONE,cloudData.getTenantPhone());
        contentValues.put(COLUMN_ROOM_NUMBER,cloudData.getRoomNumber());
        contentValues.put(COLUMN_RENT_PRICE,cloudData.getRentPrice());
        contentValues.put(COLUMN_END_TIME,dateFormat.format(cloudData.getDateOut()));
        //invoking update method to update data present in db
        long cursor = sqLiteDatabase.update(TABLE_APP_DETAILS,contentValues,"HOUSE_NAME=? AND ROOM_NUMBER=?",new String[]{cloudData.getHouseNumber(),cloudData.getRoomNumber()});
        if (cursor == -1){
            sqLiteDatabase.close();
            return false;
        }else {
            sqLiteDatabase.close();
            return true;
        }

    }

    public boolean updateStat(String house,String room,boolean status){
        sqLiteDatabase = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_STATUS,status);
        long result = sqLiteDatabase.update(TABLE_APP_DETAILS,cv,"HOUSE_NAME=? AND ROOM_NUMBER=?",new String[]{house,room});
        sqLiteDatabase.close();
        if (result ==-1){
            return false;
        }else {
            return true;
        }
    }

    public boolean deleteTenant(String house,String room){
        //invoking the database
        sqLiteDatabase = this.getWritableDatabase();
        //invoking delete method of our db
        long result = sqLiteDatabase.delete(TABLE_APP_DETAILS,"HOUSE_NAME=? AND ROOM_NUMBER=?",new String[]{house,room});
        if (result == -1){
            sqLiteDatabase.close();
            return false;
        }else {
            sqLiteDatabase.close();
            return true;
        }

    }

    public void deleteHouseData(Context context,String house){
        //query strings
        String deleteQuery1 = "SELECT * FROM " + TABLE_HOUSES;
        String deleteQuery2 = "SELECT * FROM " + TABLE_APP_DETAILS;
        //instantiating the database
        sqLiteDatabase = this.getWritableDatabase();
        //cursor
        Cursor cursor = sqLiteDatabase.rawQuery(deleteQuery2,null);
        if (cursor.moveToFirst()){
            do {
                long result = sqLiteDatabase.delete(TABLE_APP_DETAILS,"HOUSE_NAME=?",new String[]{house});
                if (result == -1){
                    System.out.println("an unknown error occurred");
                }else {
                    System.out.println("item deleted");
                }
            } while (cursor.moveToNext());

            Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show();
            cursor.close();

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Nadhir");
            reference.child(house).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(context, "house data deleted", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "deletion failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }else {
            cursor.close();
            Toast.makeText(context, "an unknown error occurred", Toast.LENGTH_SHORT).show();
        }

        long result = sqLiteDatabase.delete(TABLE_HOUSES,"HOUSE_NAME=?",new String[]{house});
        if (result == -1){
            System.out.println("an error occurred");
            Toast.makeText(context, "an unknown error occurred", Toast.LENGTH_SHORT).show();
        }else {
            System.out.println("item deleted");
            Toast.makeText(context, "item deleted", Toast.LENGTH_SHORT).show();
        }

        sqLiteDatabase.close();

    }

    public ArrayList<CloudData> getAllDetails(Context context,String name){
        //composing query statement
        String sql = "SELECT * FROM " + TABLE_APP_DETAILS;
        //initializing the arraylist
        ArrayList<CloudData> composer = new ArrayList<>();
        //instantiating the db query
        sqLiteDatabase = this.getReadableDatabase();
        //cursor
        Cursor cursor = sqLiteDatabase.rawQuery(sql,null);
        if (cursor.moveToFirst()){
            do {
                if (cursor.getString(3).equalsIgnoreCase("InsideTenants") && cursor.getString(1).equalsIgnoreCase(name)){
                    composer.add(new CloudData(cursor.getString(1), cursor.getString(3),
                            cursor.getString(4), cursor.getString(5), cursor.getString(6),
                            cursor.getString(2), cursor.getDouble(7),Date.valueOf(cursor.getString(8))));
                }
            }while (cursor.moveToNext());
        }else {
            Toast.makeText(context, "No Data Available", Toast.LENGTH_SHORT).show();
        }
        //closing the db and cursor
        cursor.close();
        sqLiteDatabase.close();

        return composer;
    }

    public ArrayList<CloudData> getAllDetails2(Context context,String name){
        //composing query statement
        String sql = "SELECT * FROM " + TABLE_APP_DETAILS;
        //initializing the arraylist
        ArrayList<CloudData> composer = new ArrayList<>();
        //instantiating the db query
        sqLiteDatabase = this.getReadableDatabase();
        //cursor
        Cursor cursor = sqLiteDatabase.rawQuery(sql,null);
        if (cursor.moveToFirst()){
            do {
                if (cursor.getString(3).equalsIgnoreCase("OutsideTenants") && cursor.getString(1).equalsIgnoreCase(name)){
                    composer.add(new CloudData(cursor.getString(1), cursor.getString(3),
                            cursor.getString(4), cursor.getString(5), cursor.getString(6),
                            cursor.getString(2), cursor.getDouble(7), Date.valueOf(cursor.getString(8))));
                }
            }while (cursor.moveToNext());
        }else {
            Toast.makeText(context, "No Data Available", Toast.LENGTH_SHORT).show();
        }
        //closing the db and cursor
        cursor.close();
        sqLiteDatabase.close();

        return composer;
    }

    public ArrayList<MainData> getMainHomeDetails(Context context){
        //composing query statement
        String sql = "SELECT * FROM " + TABLE_HOUSES;
        //initializing the arraylist
        ArrayList<MainData> composer = new ArrayList<>();
        //instantiating the database query
        sqLiteDatabase = this.getReadableDatabase();
        //cursor
        Cursor cursor = sqLiteDatabase.rawQuery(sql,null);
        if (cursor.moveToFirst()){
            do {
                composer.add(new MainData(cursor.getString(1), cursor.getString(2)));
            }while (cursor.moveToNext());
        }else {
            Toast.makeText(context, "No Data Available", Toast.LENGTH_SHORT).show();
        }
        //closing the cursor and database connection to avoid data leakage.
        cursor.close();
        sqLiteDatabase.close();

        return composer;
    }


}


