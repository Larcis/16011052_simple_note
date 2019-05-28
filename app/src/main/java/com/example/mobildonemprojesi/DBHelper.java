package com.example.mobildonemprojesi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper  extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "NoteApp";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        //try{
            db.execSQL("CREATE TABLE IF NOT EXISTS note (" +
                       "id integer PRIMARY KEY AUTOINCREMENT," +
                       "time_ datetime DEFAULT (datetime('now','localtime'))," +
                       "header text NOT NULL," +
                       "editor_data text NOT NULL," +
                       "color integer NOT NULL,"+
                       "address text,"+
                       "priority integer DEFAULT 0)" );

            db.execSQL("CREATE TABLE IF NOT EXISTS extras (" +
                       "owner_id integer," +
                       "uri text NOT NULL, " +
                       "type_ text NOT NULL," +
                       "FOREIGN KEY(owner_id) REFERENCES note(id) ON DELETE CASCADE)");

            db.execSQL("CREATE TABLE IF NOT EXISTS time_reminder (" +
                       "owner_id integer ," +
                       "time_ integer NOT NULL," +
                       "FOREIGN KEY(owner_id) REFERENCES note(id) ON DELETE CASCADE)");

            db.execSQL("CREATE TABLE IF NOT EXISTS geo_reminder (" +
                       "owner_id integer," +
                       "place text NOT NULL," +
                       "FOREIGN KEY(owner_id) REFERENCES note(id) ON DELETE CASCADE)");
      /*  }catch (Exception ex){

        }*/

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS note");
        db.execSQL("DROP TABLE IF EXISTS extras");
        db.execSQL("DROP TABLE IF EXISTS time_reminder");
        db.execSQL("DROP TABLE IF EXISTS geo_reminder");

        onCreate(db);
    }
    int getLastId(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(id) FROM note", new String[]{});
        int last_id = 1;
        if(cursor != null){
            cursor.moveToFirst();
            last_id = cursor.getInt(0);
        }
        return last_id;
    }
    void addNote(Note nt) {
        if(nt.getHeader() == null ||nt.getEditor() == null)
            return;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("header", nt.getHeader());
        values.put("editor_data", nt.getEditor());
        values.put("color", nt.getColor());
        values.put("priority", nt.getPriority());
        values.put("address", nt.getAdr());
        db.insert("note", null, values);

        int last_id = getLastId();

        if(nt.getExtras() != null)
            for(Note.Extra i : nt.getExtras()){
                values.clear();
                values.put("owner_id", last_id);
                values.put("uri", i.uri);
                values.put("type_", i.type);
                db.insert("extras", null, values);
            }
        if(nt.getGeo_list() != null)
            for(CharSequence i : nt.getGeo_list()){
                values.clear();
                values.put("owner_id", last_id);
                values.put("place", i.toString());
                db.insert("geo_reminder", null, values);
            }
        if(nt.getTime_list() != null)
            for(long i : nt.getTime_list()){
                values.clear();
                values.put("owner_id", last_id);
                values.put("time_", i);
                db.insert("time_reminder", null, values);
            }
        db.close();
    }


    ArrayList<String> getGeoRemindersWithId(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> geo_list = new ArrayList<>();
        String selectQuery = String.format("SELECT  * FROM  geo_reminder WHERE owner_id=%d", id);
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                try{
                    String str = cursor.getString(1);
                    geo_list.add(str);
                }catch (Exception ignored){

                }
            } while (cursor.moveToNext());
        }
        db.close();
        return geo_list;
    }
    Note getNote(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("note", new String[] { "id","time_", "header", "editor_data", "color" ,"address","priority"},
                "id=?", new String[] { String.valueOf(id) }, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()){
            Note nt = new Note();
            nt.setId(Integer.parseInt(cursor.getString(0)));
            nt.setTime(cursor.getString(1));
            nt.setTime(nt.getTime().substring(0, nt.getTime().length() - 3 )); //saniyeyi siliyoruz.
            nt.setHeader(cursor.getString(2));
            nt.setEditor(cursor.getString(3));
            nt.setColor(Integer.parseInt(cursor.getString(4)));
            nt.setAdr(cursor.getString(5));
            nt.setPriority(Integer.parseInt(cursor.getString(6)));
            db.close();
            return nt;
        }
        return null;
    }

    public ArrayList<Note> getAllnotes(String txt) {
        ArrayList<Note> note_list = new ArrayList<>();
        String selectQuery = String.format("SELECT  * FROM  note %s",txt);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                try{
                    Note nt = new Note();
                    nt.setId(Integer.parseInt(cursor.getString(0)));
                    nt.setTime(cursor.getString(1));
                    nt.setTime(nt.getTime().substring(0, nt.getTime().length() - 3 )); //saniyeyi siliyoruz.
                    nt.setHeader(cursor.getString(2));
                    nt.setEditor(cursor.getString(3));
                    nt.setColor(Integer.parseInt(cursor.getString(4)));
                    nt.setAdr(cursor.getString(5));
                    nt.setPriority(Integer.parseInt(cursor.getString(6)));
                    note_list.add(nt);
                }catch (Exception ex){

                }

            } while (cursor.moveToNext());
        }
        db.close();
        return note_list;
    }

    public void updateNote(Note nt) {
       deleteNote(nt.getId());
       addNote(nt);
    }

    public void deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("note", "id = ?", new String[] { String.valueOf(id)});
        db.close();
    }


    public int getNotesCount() {
        String countQuery = "SELECT  * FROM note";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        return cursor.getCount();
    }

    public  ArrayList<Long> getTimeList(int id){
        ArrayList<Long> times = new ArrayList<>();
        String qr = "SELECT time_ FROM time_reminder WHERE owner_id = "+String.valueOf(id);
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete("time_reminder", "time_ < ?", new String[] { String.valueOf(System.currentTimeMillis())});

        Cursor cursor = db.rawQuery(qr, null);
        if (cursor.moveToFirst()) {
            do {
                times.add(cursor.getLong(0));
            } while (cursor.moveToNext());
        }
        db.close();
        return times;
    }

    public  int howManyReference(String path){
        String countQuery = String.format("SELECT  * FROM extras WHERE  uri = \"%s\"", path);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public  ArrayList<Note.Extra> getExtraList(int id){
        ArrayList<Note.Extra> lst = new ArrayList<>();
        String qr = "SELECT * FROM extras WHERE owner_id = "+String.valueOf(id);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(qr, null);
        if (cursor.moveToFirst()) {
            do {
                try{
                    Note.Extra ex = new Note.Extra();
                    ex.uri = cursor.getString(1);
                    ex.type = cursor.getString(2);
                    lst.add(ex);
                }catch (Exception e){

                }

            } while (cursor.moveToNext());
        }
        db.close();
        return lst;
    }

    public HashMap<Integer, Long> getTimeReminders() {
        HashMap<Integer, Long> reminder_list = new HashMap<>();

        String selectQuery = "SELECT  owner_id, MIN(time_) FROM  time_reminder GROUP BY owner_id";
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("time_reminder", "time_ < ?", new String[] { String.valueOf(System.currentTimeMillis())});
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                reminder_list.put(cursor.getInt(0),cursor.getLong(1));
            } while (cursor.moveToNext());
        }
        db.close();
        return reminder_list;
    }
}
