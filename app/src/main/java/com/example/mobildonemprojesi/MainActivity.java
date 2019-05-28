package com.example.mobildonemprojesi;

import android.app.AlarmManager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;


public class MainActivity extends AppCompatActivity   implements AdapterView.OnItemSelectedListener {

    RecyclerView recyclerView;
    AppBarLayout abl;
    SearchView svw;
    Spinner spin;
    boolean asc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //optimization purposes
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MapView mv = new MapView(getApplicationContext());
                    mv.onCreate(null);
                    mv.onPause();
                    mv.onDestroy();
                }catch (Exception ignored){

                }
            }
        }).start();

        ((Spinner)findViewById(R.id.spinner)).setOnItemSelectedListener(this);
        abl = findViewById(R.id.AppBarLayout_id);
        recyclerView =  findViewById(R.id.recylerview);
        svw = findViewById(R.id.search_vw);
        spin = findViewById(R.id.spinner);
        asc = true;
        //TODO bunu buradan sil
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        svw.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                setRecyclerView(newText);
                return false;
            }
        });

    }
    public void sortTypeOnclick(View v){
        asc = !asc;
        if(asc){
            ((ImageButton)findViewById(R.id.sort_type)).setImageResource(R.drawable.asc);
        }else{
            ((ImageButton)findViewById(R.id.sort_type)).setImageResource(R.drawable.desc);
        }
        setRecyclerView(svw.getQuery().toString());
    }
    private void setRecyclerView(String txt){
        DBHelper db = new DBHelper(this);

        String[] cols = {"time_", "priority", "header"};
        String order = "ORDER BY " + cols[spin.getSelectedItemPosition()] + (asc ? " ASC" : " DESC") , whr;

        whr = order;
        if(txt.length() != 0)
            whr = String.format("WHERE header LIKE \"%%%s%%\"  OR " +
                                "editor_data LIKE \"%%%s%%\" OR " +
                                "address LIKE \"%%%s%%\" OR " +
                                "time_ LIKE \"%%%s%%\" %s",txt, txt, txt, txt, order);

        ArrayList<Note> lst = db.getAllnotes(whr);
        if(lst != null ){
            NoteAdapter nt_adapter = new NoteAdapter(this, lst);
            recyclerView.setAdapter(nt_adapter);
            /*if(lst.size() >0){
                abl.setBackgroundColor(averageColor(lst));
            }else{
                abl.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }*/

            abl.setBackgroundColor(0xAAAAAAAA);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        svw.clearFocus();
        svw.setQuery("",false);
        setRecyclerView("");
        setAlarms();

    }

    private int averageColor(ArrayList<Note> lst){
        int r_sum = 0, g_sum = 0, b_sum = 0;
        for(Note i : lst){
            int color = i.getColor();
            r_sum += (color & 0x00FF0000) >> 16;
            g_sum += (color & 0x0000FF00) >> 8;
            b_sum += (color & 0x000000FF);
        }
        r_sum /= lst.size();
        g_sum /= lst.size();
        b_sum /= lst.size();

        r_sum = (r_sum << 16) & 0x00FF0000;
        g_sum = (g_sum << 8) & 0x0000FF00;
        b_sum = b_sum & 0x000000FF;
        return 0xFF000000 | r_sum | g_sum | b_sum;
    }
    public void AddOnClick(View view){
        startActivity(new Intent(MainActivity.this, AddNoteActivity.class));
    }
    private  void setAlarms(){
        DBHelper db = new DBHelper(this);
        HashMap<Integer, Long> reminders = db.getTimeReminders();

        AlarmManager mgr=(AlarmManager)this.getSystemService(Context.ALARM_SERVICE);

        for(int i : reminders.keySet()){
            Intent it = new Intent(getApplicationContext(), AlarmReceiver.class);
            it.setAction("com.example.intent.action.ALARM");
            it.putExtra("id", i);
            PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), i, it,  PendingIntent.FLAG_UPDATE_CURRENT);
            mgr.set(AlarmManager.RTC_WAKEUP, reminders.get(i) , pi);
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setRecyclerView(svw.getQuery().toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {

    }

}
