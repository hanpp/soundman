package hanpp.soundman;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;

public class JackListener extends Service {
    public static AudioManager mgr;
    int mStartMode;       // indicates how to behave if the service is killed
    IBinder mBinder;      // interface for clients that bind
    boolean mAllowRebind; // indicates whether onRebind should be used
    private JackIntentReceiver mir;
    private IntentFilter filter;

    private int myID;
    private Context context;

    private Boolean isForeGround;

    @Override
    public void onCreate() {
        // The service is being created
        context = getApplicationContext();
        isForeGround = false;
        mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mir = new JackIntentReceiver();
        filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mir, filter); //start the headphone jack listener
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        myID = startId;
        ChangeStreamMode(); //set initial audio mode
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
        mgr.setStreamMute(AudioManager.STREAM_MUSIC, false); //unmute
        unregisterReceiver(mir);
    }

    public void foreGround(String ContentTitle) {
        if (isForeGround) {
            stopForeground(true);
        }
        isForeGround = true;

        //notifications
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pint = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notif = new Notification.Builder(context)
                .setContentTitle(ContentTitle)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pint)
                .build();
        //send the notification
        startForeground(myID, notif);
    }

    public void ChangeStreamMode() {
        if (mgr.isWiredHeadsetOn()) {
            //Headset is plugged
            mgr.setStreamMute(AudioManager.STREAM_MUSIC, false);
            foreGround("Headset is plugged in");
        } else {
            //Headset is unplugged, set silent
            mgr.setStreamMute(AudioManager.STREAM_MUSIC, true);
            foreGround("Headset is unplugged");
        }
    }

    public class JackIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChangeStreamMode();
        }
    }
}

