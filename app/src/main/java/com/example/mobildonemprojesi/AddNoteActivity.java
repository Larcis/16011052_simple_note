package com.example.mobildonemprojesi;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mobildonemprojesi.colorpicker.ColorPickerPopup;
import com.example.mobildonemprojesi.richeditor.RichEditor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.example.mobildonemprojesi.FileStuff.FileUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class AddNoteActivity extends FragmentActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status> {
    private RichEditor mEditor;
    private TextView geo_r;
    private TextView time_r;
    private TextView extras_r;
    private int bgcolor;
    private int fontcolor;
    private EditText header_et;
    private boolean isSaved;
    private int fontSize;
    private long[] times;
    private int times_idx;
    private long base_time;
    private boolean isUpdateMode;
    private int update_id;
    private ArrayList<Uri> videos, audios, documents;
    private ArrayList<CharSequence> geofences;

    private int extra_id;
    private LinearLayout hide_ll;
    private GoogleMap mMap = null;
    private FusedLocationProviderClient fusedLocationClient = null;
    Location last_location = null;

    private void selectExtra(int id) {
        if (ActivityCompat.checkSelfPermission(AddNoteActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            extra_id = id;
        } else {

            Intent intent;
            switch (id) {
                case 0:

                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    String[] mimetypes = {"application/pdf", "application/msword", "application/vnd.ms-excel",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/mspowerpoint", "text/*"};
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                    break;
                case 1:
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                    break;
                case 2:
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    break;
                case 3:
                    intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    break;
                default:
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
            }

            startActivityForResult(intent, id);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            Uri path = data.getData();
            if (path == null)
                return;
            switch (requestCode) {
                case 0:
                    if (!documents.contains(path))
                        documents.add(path);
                    else
                        Toast.makeText(this, "You can not add same document again!", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    audios.add(path);
                    break;
                case 2:
                    videos.add(path);
                    break;
                case 3:

                    mEditor.insertImage(FileUtils.getPath(this, path), "image");
                    break;
            }
            extras_r.setText("Extras: " + (videos.size() + audios.size() + documents.size()));
        }

    }

    public void show_geo_reminders_on_click(final View v){
        String[] chs = new String[2 * geofences.size()];
        for (int i = 0; i < 2 * geofences.size(); i += 2) {
            String str[] = geofences.get(i/2).toString().split("/");
            chs[i] = String.format("lat: %.3f lon: %.3f dia: %.2f", Double.valueOf(str[0]),Double.valueOf(str[1]),Double.valueOf(str[2]));
            chs[i + 1] = "Delete";
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Geo Reminders: ");
        builder.setItems(chs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which % 2 == 1) {
                    geofences.remove(which / 2);
                    geo_r.setText("Geo Reminders: " + geofences.size());
                }
            }
        });
        if (geofences.size() > 0)
            builder.show();
    }
    public void show_time_reminders_on_click(final View v) {
        String[] chs = new String[2 * times_idx];
        for (int i = 0; i < 2 * times_idx; i += 2) {
            if (times[i / 2] > System.currentTimeMillis()) {
                chs[i] = "++>" + getDate(times[i / 2], "dd/MM/yyyy HH:mm");//:ss.SSS");
                chs[i + 1] = "Delete";
            } else {
                chs[i] = "--> " + getDate(times[i / 2], "dd/MM/yyyy HH:mm");//:ss.SSS");
                chs[i + 1] = "Reminder is before now. Delete?";
            }

        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Time Reminders: ");
        builder.setItems(chs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which % 2 == 1) {
                    if (times_idx - 1 - which / 2 >= 0)
                        System.arraycopy(times, which / 2 + 1, times, which / 2, times_idx - 1 - which / 2);
                    times_idx--;
                    time_r.setText("Time Reminders: " + times_idx);
                }
            }
        });
        if (times_idx > 0)
            builder.show();
    }

    private String uri_to_fname(Uri uri) {
        String s = FileUtils.getFileName(this, uri);
        String[] lst = s.split("/");
        return lst[lst.length - 1];
    }

    public void show_extras_on_click(final View v) {
        ArrayList<String> chs = new ArrayList<>();
        if (videos != null)
            for (Uri u : videos) {
                chs.add("video: " + uri_to_fname(u));
                chs.add("delete");
            }
        if (audios != null)
            for (Uri u : audios) {
                chs.add("audio: " + uri_to_fname(u));
                chs.add("delete");
            }
        if (documents != null)
            for (Uri u : documents) {
                chs.add("doc: " + uri_to_fname(u));
                chs.add("delete");
            }
        String[] chs1 = new String[chs.size()];
        int idx = 0;
        for (String s : chs) {
            chs1[idx++] = s;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Extras: ");
        builder.setItems(chs1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which % 2 == 1) {
                    int x = which / 2;
                    if (x - videos.size() < 0) {
                        videos.remove(x);
                    } else if (x - videos.size() - audios.size() < 0) {
                        x = x - videos.size();
                        audios.remove(x);
                    } else {
                        x = x - videos.size() - audios.size();
                        String path = FileUtils.getPath(getApplicationContext(), documents.get(x));
                        DBHelper db = new DBHelper(getApplicationContext());
                        int ref = db.howManyReference(path);
                        if (ref == 1) {
                            File file = new File(path);
                            if (file.exists())
                                file.delete();
                        }
                        documents.remove(x);
                    }
                    extras_r.setText("Extras: " + (videos.size() + audios.size() + documents.size()));
                }
            }
        });
        if (idx > 0)
            builder.show();
    }

    private String getRealPathFromURI(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public void add_extra_on_click(View v) {
        String[] chs = {"Document", "Audio", "Video"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose attachment type: ");
        builder.setItems(chs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectExtra(which);
            }
        });
        builder.show();
    }

    private void setTopLayout(int id) {
        findViewById(R.id.hide_ll).setVisibility(id);
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        if (id == View.GONE) {
            findViewById(R.id.map_fl).setVisibility(View.VISIBLE);
            fm.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .show(getSupportFragmentManager().findFragmentById(R.id.map))
                    .commit();
        } else {
            findViewById(R.id.map_fl).setVisibility(View.GONE);
            fm.beginTransaction()
                    .hide(getSupportFragmentManager().findFragmentById(R.id.map))
                    .commit();

        }
    }

    private void markLastLocation() {
        LatLng center;
        if (last_location == null)
            center = new LatLng(41.0268453, 28.8867936); // davutpasa kampus
        else
            center = new LatLng(last_location.getLatitude(), last_location.getLongitude());
        if (mMap != null) {
            mMap.addMarker(new MarkerOptions().position(center).title("You are here."));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, 15), 3000, null);
        }
    }


    private  void drawCurrentSelectedGeofences(){
        clearGeofence();
        mMap.clear();
        if(geofences != null && geofences.size() >0){
            for(CharSequence s : geofences){
                String str[] = s.toString().split("/");
                double lat, lon, dia;
                lat = Double.valueOf(str[0]);
                lon = Double.valueOf(str[1]);
                dia = Double.valueOf(str[2]);

                CircleOptions circleOptions = new CircleOptions()
                        .center( new LatLng(lat, lon))
                        .strokeColor(Color.argb(50, 70,70,70))
                        .fillColor( Color.argb(100, 150,150,0) )
                        .radius( dia );
                if(mMap != null)
                    mMap.addCircle( circleOptions );
            }
        }
    }
    private void showMapFragment() {
        drawCurrentSelectedGeofences();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        hideKeyboard(this);
        setTopLayout(View.GONE);
        markLastLocation();
    }

    public void add_reminder_on_click(final View v) {
        String[] chs = {"Geographic", "Time"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose reminder type: ");
        builder.setItems(chs, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //TODO add geofence and set geofence alarm
                    if (ActivityCompat.checkSelfPermission(AddNoteActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(AddNoteActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                        return;
                    }
                    if (mMap != null) {
                        showMapFragment();
                    }

                } else {
                    choose_date(v);
                }
            }
        });
        builder.show();
    }

    public void save_on_click(View v) {
        String header = header_et.getText().toString();
        String editor_data = mEditor.getHtml();
        int color = bgcolor;
        if (header == null || header.length() == 0 || editor_data == null || editor_data.length() == 0) {
            Snackbar.make(v, "Note header or body can't be empty!", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        } else {
            DBHelper db = new DBHelper(this);
            ArrayList<Long> tlist = new ArrayList<>();
            for (int i = 0; i < times_idx; i++) {
                tlist.add(times[i]);
            }
            ArrayList<Note.Extra> exts = new ArrayList<>();
            if (videos != null)
                for (Uri u : videos) {
                    Note.Extra ex = new Note.Extra();
                    ex.type = "video";
                    ex.uri = getRealPathFromURI(u);
                    exts.add(ex);
                }
            if (audios != null)
                for (Uri u : audios) {
                    Note.Extra ex = new Note.Extra();
                    ex.type = "audio";
                    ex.uri = getRealPathFromURI(u);
                    exts.add(ex);
                }
            if (documents != null)
                for (Uri u : documents) {
                    Note.Extra ex = new Note.Extra();
                    ex.type = "document";
                    ex.uri = FileUtils.getPath(this, u);
                    exts.add(ex);
                }
            String adr = "";
            if(last_location != null){
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(last_location.getLatitude(), last_location.getLongitude(), 1);
                    String address = addresses.get(0).getAddressLine(0);
                    if(address != null)
                        adr +=address;
                    String city = addresses.get(0).getLocality();
                    if(city != null)
                        adr+="//" +city;
                    String state = addresses.get(0).getAdminArea();
                    /*if(state != null)
                        adr+="//" +state;*/
                    String country = addresses.get(0).getCountryName();
                    if(country != null)
                        adr+="//" +country;
                    String postalCode = addresses.get(0).getPostalCode();
                    if(postalCode != null)
                        adr+="//" +postalCode;
                    String knownName = addresses.get(0).getFeatureName();
                    if(knownName != null)
                        adr+="//" +knownName;
                }catch (Exception ignored){
                }
            }
            Note nt = new Note(adr,header, editor_data, color, exts, geofences, tlist);
            nt.setPriority(((SeekBar) findViewById(R.id.seekBar)).getProgress());
            if (!isSaved) {
                db.addNote(nt);
                isSaved = true;
                Snackbar.make(v, "Note saved.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            } else {
                if (isUpdateMode)
                    nt.setId(update_id);
                else
                    nt.setId(db.getLastId());
                db.updateNote(nt);
                if (isUpdateMode)
                    update_id = db.getLastId();
                Snackbar.make(v, "Note updated.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
            }


        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //TODO kontrol et kaydetmedıgın var mı
        outState.putString("header", header_et.getText().toString());
        outState.putString("text", mEditor.getHtml());
        outState.putInt("bgcolor", bgcolor);
        outState.putInt("fontcolor", fontcolor);
        outState.putBoolean("issaved", isSaved);
        outState.putInt("fontsize", fontSize);
        outState.putInt("times_idx", times_idx);
        outState.putLongArray("times", times);

        outState.putInt("update_id", update_id);
        outState.putBoolean("isUpdateMode", isUpdateMode);
        outState.putInt("topLayerVisibility", hide_ll.getVisibility());
        outState.putParcelableArrayList("videos", videos);
        outState.putParcelableArrayList("audios", audios);
        outState.putParcelableArrayList("documents", documents);
        outState.putParcelable("lastLocation", last_location);

        outState.putCharSequenceArrayList("geofences", geofences);
    }

    private void updateComponents(Bundle savedInstanceState) {
        //TODO kontrol et cekmedıgın var mı
        mEditor.setHtml(savedInstanceState.getString("text"));
        bgcolor = savedInstanceState.getInt("bgcolor");
        fontcolor = savedInstanceState.getInt("fontcolor");
        header_et.setText(savedInstanceState.getString("header"));
        mEditor.setEditorBackgroundColor(bgcolor);
        mEditor.setTextColor(fontcolor);
        fontSize = savedInstanceState.getInt("fontsize");
        mEditor.setFontSize(fontSize);
        header_et.setBackgroundColor(bgcolor);
        isSaved = savedInstanceState.getBoolean("issaved");
        times = savedInstanceState.getLongArray("times");
        times_idx = savedInstanceState.getInt("times_idx");
        time_r.setText("Time Reminders: " + times_idx);
        isUpdateMode = savedInstanceState.getBoolean("isUpdateMode");
        update_id = savedInstanceState.getInt("update_id");

        videos = savedInstanceState.getParcelableArrayList("videos");
        audios = savedInstanceState.getParcelableArrayList("audios");
        documents = savedInstanceState.getParcelableArrayList("documents");
        geofences = savedInstanceState.getCharSequenceArrayList("geofences");
        extras_r.setText("Extras: " + (videos.size() + audios.size() + documents.size()));
        geo_r.setText("Geo Reminders: "+geofences.size());
        setTopLayout(savedInstanceState.getInt("topLayerVisibility"));
        last_location = savedInstanceState.getParcelable("lastLocation");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupMapIfNeeded();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        updateComponents(savedInstanceState);
    }


    private void btnColorInvert(int id) {
        if (mEditor.isFocused()) {
            ImageButton btn = findViewById(id);
            int buttonColor = ((ColorDrawable) btn.getBackground()).getColor();
            btn.setBackgroundColor((buttonColor ^ 0xFFFFFFFF | 0xFF000000));
        }
    }

    private void setupMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }
    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
    public void saveGeofenceToList(View v) {
        if(geoFenceMarker == null)
            return;
        LatLng ll = geoFenceMarker.getPosition();
        for(CharSequence c : geofences){
            String res[] = c.toString().split("/");
            double lat, lon, dia;
            lat = Double.valueOf(res[0]);
            lon = Double.valueOf(res[1]);
            dia = Double.valueOf(res[2]);

            double dist = distance(lat, ll.latitude, lon , ll.longitude, 0, 0);
            if(dist < (dia + GEOFENCE_RADIUS)){
                Snackbar.make(v, "Geofence not added. To close to others ", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                return;
            }
        }
        String str = String.format("%f/%f/%f", ll.latitude, ll.longitude, GEOFENCE_RADIUS);
        geofences.add(str);
        geo_r.setText(String.format("Geo Reminders: %d", geofences.size()));
        Snackbar.make(v, "Geofence added.", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        drawCurrentSelectedGeofences();
    }

    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        final SeekBar sk=findViewById(R.id.seekBar2);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                if(progress < 100)
                    progress = 100;
                GEOFENCE_RADIUS = progress;
                drawGeofence();
            }
        });
        setupMapIfNeeded();
        createGoogleApi();

        setTopLayout(View.VISIBLE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AddNoteActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 3);
        } else {
            addLastLocationListener();
        }

        hide_ll = findViewById(R.id.hide_ll);
        header_et = findViewById(R.id.header_et);
        mEditor = findViewById(R.id.editor);
        geo_r = findViewById(R.id.geo_r);
        time_r = findViewById(R.id.time_r);
        extras_r = findViewById(R.id.extras);
        isUpdateMode = false;
        Intent i = getIntent();
        if (i.hasExtra("id")) {
            isUpdateMode = true;
            int id = i.getExtras().getInt("id");
            update_id = id;
            DBHelper db = new DBHelper(this);
            Note nt = db.getNote(id);

            mEditor.setHtml(nt.getEditor());
            bgcolor = nt.getColor();
            header_et.setText(nt.getHeader());
            header_et.setBackgroundColor(bgcolor);
            mEditor.setEditorBackgroundColor(bgcolor);
            isSaved = true;
            fontSize = 5;
            times_idx = 0;
            times = new long[30];
            videos = new ArrayList<>();
            audios = new ArrayList<>();
            documents = new ArrayList<>();
            geofences = new ArrayList<>();
            ArrayList<String> geoWithId = db.getGeoRemindersWithId(id);
            for(String s : geoWithId){
                //String str[] = s.split("/");
                //geofences.add(str[0]+"/"+str[1]+"/"+str[2]);
                geofences.add(s);
            }
            geo_r.setText("Geo Reminders: "+geofences.size());

            ((SeekBar) findViewById(R.id.seekBar)).setProgress(nt.getPriority());
            ArrayList<Note.Extra> lst2 = db.getExtraList(id);
            if (lst2 != null) {
                for (Note.Extra ex : lst2) {
                    switch (ex.type) {
                        case "video":
                            videos.add(Uri.parse(ex.uri));
                            break;
                        case "audio":
                            audios.add(Uri.parse(ex.uri));
                            break;
                        case "document":
                            documents.add(FileUtils.getUri(new File(ex.uri)));
                            break;
                    }
                }
                extras_r.setText("Extras: " + (videos.size() + audios.size() + documents.size()));
            }

            ArrayList<Long> lst = db.getTimeList(id);
            if (lst != null) {
                for (Long l : lst) {
                    times[times_idx++] = l;
                }
                time_r.setText("Time Reminders: " + times_idx);
            }

        } else if (savedInstanceState != null) {
            updateComponents(savedInstanceState);
        } else {
            bgcolor = 0xFFFFFFFF;
            mEditor.setPlaceholder("Enter your note here...");
            isSaved = false;
            fontSize = 5;
            times = new long[30];
            times_idx = 0;
            videos = new ArrayList<>();
            audios = new ArrayList<>();
            documents = new ArrayList<>();
            geofences = new ArrayList<>();
        }
        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.undo();
            }
        });

        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.redo();
            }
        });

        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBold();
                btnColorInvert(R.id.action_bold);
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setItalic();
                btnColorInvert(R.id.action_italic);
            }
        });


        findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setStrikeThrough();
                btnColorInvert(R.id.action_strikethrough);
            }
        });

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setUnderline();
                btnColorInvert(R.id.action_underline);
            }
        });


        findViewById(R.id.font_size_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fontSize < 7)
                    mEditor.setFontSize(++fontSize);
            }
        });
        findViewById(R.id.font_size_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fontSize > 1)
                    mEditor.setFontSize(--fontSize);
            }
        });

        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ColorPickerPopup.Builder(v.getContext())
                        .initialColor(0xFF0000FF)
                        .enableBrightness(true)
                        .enableAlpha(false)
                        .okTitle("Choose")
                        .cancelTitle("Cancel")
                        .showIndicator(true)
                        .showValue(false)
                        .build()
                        .show(v, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                mEditor.setTextColor(color);
                                fontcolor = color;
                            }
                        });
            }
        });

        findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideKeyboard(AddNoteActivity.this);
                new ColorPickerPopup.Builder(v.getContext())
                        .initialColor(bgcolor)
                        .enableBrightness(true)
                        .enableAlpha(false)
                        .okTitle("Choose")
                        .cancelTitle("Cancel")
                        .showIndicator(true)
                        .showValue(false)
                        .build()
                        .show(v, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                mEditor.setEditorBackgroundColor(color);
                                header_et.setBackgroundColor(color);
                                bgcolor = color;
                            }
                        });
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });


        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setNumbers();
            }
        });

        findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectExtra(3);
            }
        });

        findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Link Information");

                final LayoutInflater inflater = getLayoutInflater();
                final View vw = inflater.inflate(R.layout.link_dialog_layout, null);
                builder.setView(vw);

                builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String address = ((EditText) vw.findViewById(R.id.link_address_et)).getText().toString();
                        if (!address.contains("https://") && !address.contains("http://")) {
                            address = "https://" + address;
                        }
                        if (!address.contains(".com")) {
                            address += ".com";
                        }
                        mEditor.insertLink(address, ((EditText) vw.findViewById(R.id.link_name_et)).getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });
    }

    private static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void choose_date(View view) {
        hideKeyboard(AddNoteActivity.this);
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar cl = Calendar.getInstance();
                cl.clear();
                cl.set(year, monthOfYear, dayOfMonth);
                base_time = cl.getTimeInMillis();
                choose_time();
            }
        };
        Calendar cl = Calendar.getInstance();
        new DatePickerDialog(AddNoteActivity.this, dateListener, cl.get(Calendar.YEAR),
                cl.get(Calendar.MONTH), cl.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void choose_time() {
        Calendar cl = Calendar.getInstance();
        TimePickerDialog tpd = new TimePickerDialog(AddNoteActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        base_time += 60 * 60 * 1000 * hourOfDay + 60 * 1000 * minute;
                        times[times_idx++] = base_time;
                        time_r.setText("Time Reminders: " + times_idx);
                    }
                }, cl.get(Calendar.HOUR_OF_DAY), cl.get(Calendar.MINUTE) + 1, true);
        tpd.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    selectExtra(extra_id);
                } else {
                    Toast.makeText(this, "In order to attach files to your notes you must grant storage permission!", Toast.LENGTH_LONG).show();
                }
                break;
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showMapFragment();
                } else {
                    Toast.makeText(this, "In order to add geo reminders to your notes you must grant location permission!", Toast.LENGTH_LONG).show();
                }
                break;

            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addLastLocationListener();
                }
                break;
        }


    }

    private void addLastLocationListener() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                last_location = location;
                            }
                        }
                    });
        }

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (findViewById(R.id.hide_ll).getVisibility() != View.GONE)
            startActivity(new Intent(AddNoteActivity.this, MainActivity.class));
        else {
            setTopLayout(View.VISIBLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);

    }

    private static final String TAG = AddNoteActivity.class.getSimpleName();
    private GoogleApiClient googleApiClient;
    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();

    }
    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();

    }



    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "onMapClick(" + latLng + ")");
        markerForGeofence(latLng);
        drawGeofence();
        findViewById(R.id.seekBar2).setVisibility(View.VISIBLE);
        //startGeofence();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(TAG, "onMarkerClickListener: " + marker.getPosition());
        return false;
    }

    private Marker geoFenceMarker;
    private void markerForGeofence(LatLng latLng) {
        Log.i(TAG, "markerForGeofence("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        // Define marker options
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( mMap!=null ) {
            if (geoFenceMarker != null)
                geoFenceMarker.remove();

            geoFenceMarker = mMap.addMarker(markerOptions);

        }
    }

    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if( geoFenceMarker != null ) {
            Geofence geofence = createGeofence( geoFenceMarker.getPosition(), GEOFENCE_RADIUS );
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
            addGeofence( geofenceRequest );
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    private static final long GEO_DURATION = -1;//60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "Geo Geo";
    private static float GEOFENCE_RADIUS = 500.0f; // in meters

    private Geofence createGeofence( LatLng latLng, float radius ) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( GEO_DURATION )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    private GeofencingRequest createGeofenceRequest( Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence( geofence )
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;
    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class );
        intent.setAction("com.example.intent.action.ALARM");
        if(isUpdateMode)
            intent.putExtra("id", update_id);
        else{
            DBHelper db = new DBHelper(this);
            intent.putExtra("id", db.getLastId());
        }
        //sendBroadcast(intent);
        return PendingIntent.getBroadcast(this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
    }

    @Override
    public void onResult( Status status) {
        Log.i(TAG, "onResult: " + status);
        if ( status.isSuccess() ) {
            drawGeofence();
            //Toast.makeText(this, "you arrived!", Toast.LENGTH_LONG).show();
        } else {
            // inform about fail
        }
    }

    private Circle geoFenceLimits;
    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if ( geoFenceLimits != null )
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center( geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(100, 150,150,150) )
                .radius( GEOFENCE_RADIUS );
        geoFenceLimits = mMap.addCircle( circleOptions );
    }

    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                createGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult( Status status) {
                if ( status.isSuccess() ) {
                    removeGeofenceDraw();
                }
            }
        });
    }

    private void removeGeofenceDraw() {
        Log.d(TAG, "removeGeofenceDraw()");
        if ( geoFenceMarker != null)
            geoFenceMarker.remove();
        if ( geoFenceLimits != null )
            geoFenceLimits.remove();
    }

    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

}

