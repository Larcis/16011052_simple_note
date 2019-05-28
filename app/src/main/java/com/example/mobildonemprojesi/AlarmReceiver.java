package com.example.mobildonemprojesi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private NotificationManager notifManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null && intent.hasExtra("id")){
            createNotification(intent.getExtras().getInt("id"), context);
}
    }

    private void createNotification(int id_, Context context) {
        String id = "uygar";
        String title =" Simple Note App Notifications";

        Note nt = new DBHelper(context).getNote(id_);
        if(nt == null)
            return;

        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;
        if (notifManager == null) {
            notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mChannel.enableLights(true);


                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);
            intent = new Intent(context, ShowNoteActivity.class);
            intent.putExtra("id", id_);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, nt.getId(), intent, 0);
            builder.setContentTitle(nt.getHeader())
                    .setSmallIcon(R.drawable.ic_stat_call_white)
                    .setColor(nt.getColor())
                    .setContentText("Hey! You have a reminder! Come in.")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker("ticker")
                    .setLights(nt.getColor(), 100, 50)
                    .setSound(alarmSound)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        }
        else {
            builder = new NotificationCompat.Builder(context, id);
            intent = new Intent(context, ShowNoteActivity.class);
            intent.putExtra("id", id_);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context,  nt.getId(), intent, 0);
            builder.setContentTitle(nt.getHeader())
                    .setSmallIcon(R.drawable.ic_stat_call_white)
                    .setColor(nt.getColor())
                    .setContentText("Hey! You have a reminder! Come in.")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker("ticker")
                    .setLights(nt.getColor(), 100, 50)
                    .setSound(alarmSound)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        Notification notification = builder.build();
        notifManager.notify(id_, notification);
    }
}
