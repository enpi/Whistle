package com.codamasters.whistle.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.codamasters.whistle.R;
import com.codamasters.whistle.thread.DetectorThread;
import com.codamasters.whistle.thread.RecorderThread;
import com.codamasters.whistle.ui.MainActivity;

public class WhistleService extends Service implements DetectorThread.OnWhistleListener {

    Handler handler;
    private DetectorThread detectorThread;
    private RecorderThread recorderThread;
    private Ringtone r;

    public WhistleService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startWhistleDetection();

        return START_STICKY;
    }

    private void startWhistleDetection() {

        try {
            stopWhistleDetection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        recorderThread = new RecorderThread();
        recorderThread.startRecording();
        detectorThread = new DetectorThread(recorderThread);
        detectorThread.setOnWhistleListener(this);
        detectorThread.start();

    }

    private void stopWhistleDetection() {
        if (detectorThread != null) {
            detectorThread.stopDetection();
            detectorThread.setOnWhistleListener(null);
            detectorThread = null;
        }

        if (recorderThread != null) {
            recorderThread.stopRecording();
            recorderThread = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopWhistleDetection();
    }

    @Override
    public void onWhistle() {
        //sendNotification();
        playSound();
    }


    private void playSound(){
        try {
            if(!r.isPlaying())
                r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification() {

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("notification", "showSummary");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.whistle)
                .setContentTitle("Whistle Controller")
                .setContentText(getText(R.string.info_message))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

    }

}