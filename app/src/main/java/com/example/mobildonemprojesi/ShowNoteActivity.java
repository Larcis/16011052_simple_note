package com.example.mobildonemprojesi;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;

import android.graphics.pdf.PdfRenderer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.constraint.Guideline;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.VideoView;
import com.example.mobildonemprojesi.richeditor.RichEditor;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;


public class ShowNoteActivity extends AppCompatActivity {

    TextView head;
    RichEditor edt;
    LinearLayout video_l ;
    FloatingActionButton btn;
    ImageView imageViewPdf;
    MediaPlayer mp = new MediaPlayer();

    int id_;
    @Override
    protected void onPause() {
        super.onPause();
    }


    public void geo_reminder_text_on_click(View v){
        if(id_ != -1){
            DBHelper db = new DBHelper(this);
            ArrayList<String> rmd_lst = db.getGeoRemindersWithId(id_);
            if(rmd_lst != null && rmd_lst.size() > 0){
                String[] chs = new String[rmd_lst.size()];
                int i = 0;
                for(String l : rmd_lst){
                    String str[] = l.split("/");
                    chs[i++] = String.format("lat: %.3f lon: %.3f dia: %.2f", Double.valueOf(str[0]),Double.valueOf(str[1]),Double.valueOf(str[2]));
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Geo Reminders: ");
                builder.setItems(chs,null);
                builder.show();
            }
        }
    }
    public void time_reminder_text_on_click(View v){
        if(id_ != -1){
            DBHelper db = new DBHelper(this);
            ArrayList<Long> rmd_lst = db.getTimeList(id_);
            if(rmd_lst != null && rmd_lst.size() > 0){
                String[] chs = new String[rmd_lst.size()];
                int i = 0;
                for(long l : rmd_lst){
                    chs[i++] = AddNoteActivity.getDate(l, "dd/MM/yyyy HH:mm");
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Time Reminders: ");
                builder.setItems(chs,null);
                builder.show();
            }
        }
    }
    public  void extras_on_click(View v){
        if(id_!=-1){

            DBHelper db = new DBHelper(this);
            final ArrayList<Note.Extra> exts = db.getExtraList(id_);
            if(exts != null && exts.size() != 0){
                String[] chs = new String[exts.size() - video_l.getChildCount()];
                int i = 0, j = 0;
                final int arr[] = new int[50];
                for(Note.Extra ex : exts){
                    if(!ex.type.equals("video")){
                        String[] prs = ex.uri.split("/");
                        chs[i]  = prs[prs.length -1];
                        arr[i++] = j;
                    }
                    j++;
                }
                String[] chs2 = new String[i];
                System.arraycopy(chs, 0, chs2, 0, i);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Extras: ");
                final Context ctx = this;
                builder.setItems(chs2, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String path = exts.get(arr[which]).uri;
                        String type = exts.get(arr[which]).type;

                        //Toast.makeText(ctx, path+ " " +type+" sss "+which, Toast.LENGTH_SHORT).show();
                        if(path.contains(".pdf")){
                            try{
                                File file = new File(path);
                                ParcelFileDescriptor mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                                if (mFileDescriptor != null) {
                                    mPdfRenderer = new PdfRenderer(mFileDescriptor);
                                }
                            }catch (Exception er) {
                            }
                            if(mPdfRenderer != null){
                                index = 0;
                                showPage();
                            }

                        }else if(type.equals("audio")){
                            mp = new MediaPlayer();
                            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    mp.stop();
                                }
                            });

                            try {
                                mp.setDataSource(path);
                                mp.prepare();
                                mp.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            final Dialog dg = new Dialog(ShowNoteActivity.this);
                            dg.setContentView(R.layout.mp_layout);
                            dg.setCanceledOnTouchOutside(true);


                            final ImageButton play =  dg.findViewById(R.id.play_pause);
                            ImageButton stop =  dg.findViewById(R.id.stop);
                            final ProgressBar pb = dg.findViewById(R.id.progressBar2);
                            pb.setMax(mp.getDuration());
                            final Handler mSeekbarUpdateHandler = new Handler();
                            final Runnable mUpdateSeekbar = new Runnable() {
                                @Override
                                public void run() {
                                    pb.setProgress(mp.getCurrentPosition());
                                    mSeekbarUpdateHandler.postDelayed(this, 50);
                                }
                            };
                            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);

                            dg.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    mp.stop();
                                    mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
                                    dg.dismiss();
                                }
                            });

                            play.setImageResource(R.drawable.pausepause);
                            play.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(mp.isPlaying()){
                                        mp.pause();
                                        play.setImageResource(R.drawable.playplay);
                                    }else {
                                        mp.start();
                                        play.setImageResource(R.drawable.pausepause);
                                    }
                                }
                            });
                            stop.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    mp.stop();
                                    mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);

                                    dg.dismiss();
                                }
                            });
                            dg.show();
                        }
                    }
                });
                if(i > 0)
                    builder.show();
            }
        }

    }
    public void pdfImageOnClick(View  v){
        showPage();
    }
    PdfRenderer.Page currentPage = null;
    PdfRenderer mPdfRenderer = null;
    int index = 0;
    private void showPage() {
        if (null != currentPage) {
            currentPage.close();
        }
        if(mPdfRenderer.getPageCount() <= index)
            index = 0;
        currentPage = mPdfRenderer.openPage(index++);
        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(),
                Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        imageViewPdf.setImageBitmap(bitmap);
        findViewById(R.id.hide_this).setVisibility(View.GONE);
        findViewById(R.id.back_from_pdf).setVisibility(View.VISIBLE);
        imageViewPdf.setVisibility(View.VISIBLE);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id_", id_);
        if(mp != null && mp.isPlaying())
            mp.stop();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        video_l.removeAllViewsInLayout();
        update_components(savedInstanceState);

    }
    private void update_components(Bundle ss){
        id_ = ss.getInt("id_");
        update_with_id(id_);
    }

    private void update_with_id(int id){
        DBHelper db = new DBHelper(this);
        Note nt = db.getNote(id);
        TextView pri =  findViewById(R.id.priority_tv);
        pri.setText("p: "+nt.getPriority());

        /*GradientDrawable gd = (GradientDrawable) pri.getBackground();
        gd.setColor(nt.getColor());
        pri.setTextColor((nt.getColor() ^0x00FFFFFF));*/
        id_ = nt.getId();
        edt.setFocusable(false);
        edt.setInputEnabled(false);

        head.setText(nt.getHeader());
        edt.setHtml(nt.getEditor());

        edt.pageDown(true);
        edt.insertAddress(nt.getAdr().split("//")[0], nt.getColor());

        if (ActivityCompat.checkSelfPermission(ShowNoteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ShowNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        ArrayList<Note.Extra> exts = db.getExtraList(nt.getId());
        if(exts!=null && exts.size() != 0){
            int i = 0;

            for(Note.Extra ex : exts){
                if(ex.type.equals("video")){
                    VideoView vw = new VideoView(this);
                    GradientDrawable border = new GradientDrawable();
                    border.setColor(0x0fFFFFFF);
                    border.setStroke(2, 0xFFFFFFFF);
                    vw.setBackground(border);
                    vw.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            VideoView vw = ((VideoView)v);
                            if(vw.isPlaying())
                                vw.pause();
                            else
                                vw.start();
                        }
                    });
                    vw.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            VideoView vw = ((VideoView)v);
                            vw.setOnClickListener(null);
                            vw.setOnLongClickListener(null);
                            vw.stopPlayback();
                            vw.setVisibility(View.GONE);
                            check_is_there_any_visible();
                            return false;
                        }
                    });
                    vw.setVideoPath(ex.uri);
                    vw.setLayoutParams(new FrameLayout.LayoutParams(450, FrameLayout.LayoutParams.MATCH_PARENT));
                    //vw.seekTo(1);
                    video_l.addView(vw, i++);

                }
            }
            ((TextView)findViewById(R.id.extras1)).setText("Extras: "+ (exts.size()-i));
            if(video_l.getChildCount() == 0)
                ((Guideline)findViewById(R.id.show_note_guideline)).setGuidelinePercent(1);

        }else{
            ((Guideline)findViewById(R.id.show_note_guideline)).setGuidelinePercent(1);
        }

        ArrayList<Long> time_lst = db.getTimeList(nt.getId());
        ArrayList<String> geo_list = db.getGeoRemindersWithId(nt.getId());
        if(geo_list != null && geo_list.size() !=0){
            ((TextView)findViewById(R.id.geo_r1)).setText("Geo Reminders: "+geo_list.size());
        }
        if(time_lst != null && time_lst.size() != 0){
            ((TextView)findViewById(R.id.time_r1)).setText("Time reminders: "+time_lst.size());
        }
    }
    public  void check_is_there_any_visible(){
        int count = 0;
        int childCount = video_l.getChildCount();
        for(int i = 0; i < childCount; i++) {
            if(video_l.getChildAt(i).getVisibility() == View.VISIBLE) {
                count++;
            }
        }
        if(count == 0)
            ((Guideline)findViewById(R.id.show_note_guideline)).setGuidelinePercent(1);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_note);

        head = findViewById(R.id.head);
        edt  = findViewById(R.id.body);
        video_l = findViewById(R.id.video_linear);
        imageViewPdf = findViewById(R.id.pdf_render_img);
        Intent i = getIntent();

        if(i.hasExtra("id")){
            Bundle extras = i.getExtras();
            update_with_id(extras.getInt("id"));
        }else if(savedInstanceState != null){
            update_components(savedInstanceState);
        }else{
            id_ = -1;

        }

    }
    public void hidePdfOnClick(View v ){
        imageViewPdf.setVisibility(View.GONE);
        findViewById(R.id.back_from_pdf).setVisibility(View.GONE);
        findViewById(R.id.hide_this).setVisibility(View.VISIBLE);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ShowNoteActivity.this, MainActivity.class));
    }

}
