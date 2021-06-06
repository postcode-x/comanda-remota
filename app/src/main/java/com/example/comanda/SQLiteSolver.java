package com.example.comanda;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteSolver extends SQLiteOpenHelper{

    private static final String database_name ="DBASE.db";
    public String table_name1 = "TABLAPEND";
    public String table_name2 = "TABLAOK";
    private static final int DATABASE_VERSION = 1;
    public static final String COL2 = "NUMBER";
    public static final String COL3 = "NAME";
    public static final String COL4 = "BOLETA";
    public static final String COL5 = "ITEMS";

    public SQLiteSolver(Context context) {
        super(context, database_name, null, DATABASE_VERSION);


    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable1 = "CREATE TABLE IF NOT EXISTS " + table_name1 + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 + " TEXT, " +
                COL3 + " TEXT, " +
                COL4 + " TEXT, " +
                COL5 + " TEXT);";

        String createTable2 = "CREATE TABLE IF NOT EXISTS " + table_name2 + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL2 + " TEXT, " +
                COL3 + " TEXT, " +
                COL4 + " TEXT, " +
                COL5 + " TEXT);";

        db.execSQL(createTable1);
        db.execSQL(createTable2);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + table_name1);
        db.execSQL("DROP TABLE IF EXISTS " + table_name2);
        onCreate(db);

    }

    public boolean addData(String numero, String nombre, String boleta, String items, String table){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, numero);
        contentValues.put(COL3, nombre);
        contentValues.put(COL4, boleta);
        contentValues.put(COL5, items);

        Log.i("debug", "Inserting " + numero + " , " + nombre + " , " + boleta + " , " + items + " to " + table);

        long result = db.insert(table, null, contentValues);

        if(result == -1){
            return false;
        } else
            return true;

    }

    public Cursor getData(String table){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + table;
        Cursor data = db.rawQuery(query,null);
        return data;
    }

    public void cleanTable(String table){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + table;
        db.execSQL(query);
    }
}

