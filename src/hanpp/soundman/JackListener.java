package hanpp.soundman;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;

public class JackListener extends Service {
    int mStartMode;       // indicates how to behave if the service is killed
    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used
    private jackIntentReceiver mir;
    private IntentFilter filter;

    private int notificationID = 1;
    private Context context;

    private NotificationManager notificationManager;
    private boolean isForeground = false;

    @Override
    public void onCreate() {
        // The service is being created
        context = getApplicationContext();
        mir = new jackIntentReceiver();
        filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mir, filter); //start the headphone jack listener
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        Manager.listenerServiceRunning = true; //register the service started
        changeStreamMode(); //set initial audio mode
        return mStartMode;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // A client is binding to the service with bindService()
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // All clients have unbound with unbindService()
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        // A client is binding to the service with bindService(),
        // after onUnbind() has already been called
    }

    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        Manager.mgr.setStreamMute(AudioManager.STREAM_MUSIC, false); //unmute
        unregisterReceiver(mir);
        stopForeground(true);
        Manager.listenerServiceRunning = false;
    }

    public void foreGround(String ContentTitle) {
        //notifications
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pint = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(context)
                .setContentTitle(ContentTitle)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pint)
                .build();
        if (isForeground) { //check if a notification has already been created
            if (notificationManager == null) {
                notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            }
            notificationManager.notify(notificationID, notification); //change the notification if a notification exists
        } else {
            startForeground(notificationID, notification); //create a new notification
            notificationManager = (NotificationManager) getApplicationContext().getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        }
    }

    public void changeStreamMode() {
        if (Manager.mgr.isWiredHeadsetOn()) {
            //Headset is plugged, unmute
            if (Manager.audioMuted) { //if muted, unmute
                Manager.audioMuted = false;
                Manager.mgr.setStreamMute(AudioManager.STREAM_MUSIC, false);
            } else { //if unmuted then mute and unmute
                Manager.mgr.setStreamMute(AudioManager.STREAM_MUSIC, true);
                Manager.mgr.setStreamMute(AudioManager.STREAM_MUSIC, false);
            }
            foreGround("Headset is plugged in");
        } else {
            //Headset is unplugged, mute
            if (!Manager.audioMuted) { //if unmuted, mute
                Manager.audioMuted = true;
                Manager.mgr.setStreamMute(AudioManager.STREAM_MUSIC, true);
            } else { //if muted then unmute and mute
                Manager.mgr.setStreamMute(AudioManager.STREAM_MUSIC, false);
                Manager.mgr.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
            foreGround("Headset is unplugged");
        }
    }

    public class jackIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            changeStreamMode();
        }
    }
}

