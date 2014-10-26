package hanpp.soundman;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private Switch swb;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefed;
    private Button startbtn;
    private Button stopbtn;
    private ProgressBar progb;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        prefs = this.getSharedPreferences("hanpp.soundman", Context.MODE_PRIVATE);
        prefed = prefs.edit();

        swb = (Switch) findViewById(R.id.switch_startonBoot);
        startbtn = (Button) findViewById(R.id.startButton);
        stopbtn = (Button) findViewById(R.id.stopButton);
        progb = (ProgressBar) findViewById(R.id.progressBar);

        progb.setVisibility(View.INVISIBLE);

        if (!prefs.contains("autostart")) {
            ChangeStartOnBootSetting(null); //make the autostart setting if it doesn't exist
        }

        if (!prefs.getBoolean("autostart", false)) {
            swb.setChecked(false); //set the switch to false if autostart is false, checked by default
        }
        if (Manager.isInitialized()) { //check if manager has been initialized
            if (!Manager.listenerServiceRunning) { //start the service if it is not running
                this.startService(Manager.listenerIntent);
            }
        } else {
            new Manager(this); //initialize the manager
            this.startService(Manager.listenerIntent);
        }
        startbtn.setEnabled(false); //disable the start button
        stopbtn.setEnabled(true); //enable the stop button
    }

    public void StartJackService(View v) {
        if (!Manager.listenerServiceRunning) {
            startService(Manager.listenerIntent);
            stopbtn.setEnabled(true); //enable the stop button
            startbtn.setEnabled(false); //disable the start button
        } else {
            startbtn.setEnabled(false); //enable the start button
            waitStopListenerService();
        }
    }

    public void StopJackService(View v) {
        if (Manager.listenerServiceRunning) {
            stopService(Manager.listenerIntent);
        }
        stopbtn.setEnabled(false); //disable the stop button
        waitStopListenerService();
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            startbtn.setEnabled(true); //enable the start button
            progb.setVisibility(View.INVISIBLE);
        }
    };

    public void waitStopListenerService() {
        //wait until the service has stopped
        progb.setVisibility(View.VISIBLE);
        final Timer timerWait = new Timer();

        timerWait.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!Manager.listenerServiceRunning) {
                    handler.sendMessage(new Message());
                    cancel();
                }
            }
        }, 0, 1000);
    }

    public void ChangeStartOnBootSetting(View v) {
        //change the autostart variable
        prefed.putBoolean("autostart", swb.isChecked());
        prefed.apply();
    }

    public void UnMute(View v) {
        //unmute button
        Manager.mgr.setStreamMute(AudioManager.STREAM_MUSIC, false);
    }
}
