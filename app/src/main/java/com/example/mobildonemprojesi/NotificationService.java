package com.example.mobildonemprojesi;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class NotificationService extends Service {
    private AlarmReceiver receiver;

    public NotificationService() { }

    @Override
    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addDataScheme("TIME_REMINDER_TIME");
        receiver = new AlarmReceiver();
        registerReceiver(receiver, intentFilter);
    }
    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}