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
import android.widget.CheckBox;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    private CheckBox swb;
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

        swb = (CheckBox) findViewById(R.id.checkBox_startonBoot);
        startbtn = (Button) findViewById(R.id.startButton);
        stopbtn = (Button) findViewById(R.id.stopButton);
        progb = (ProgressBar) findViewById(R.id.progressBar);

        progb.setVisibility(View.INVISIBLE);

        if (!prefs.contains("autostart")) {
            changeStartOnBootSetting(null); //make the autostart setting if it doesn't exist
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

    public void startJackService(View v) {
        if (!Manager.listenerServiceRunning) {
            startService(Manager.listenerIntent);
            stopbtn.setEnabled(true); //enable the stop button
            startbtn.setEnabled(false); //disable the start button
        } else {
            startbtn.setEnabled(false); //enable the start button
            waitStopListenerService();
        }
    }

    public void stopJackService(View v) {
        if (Manager.listenerServiceRunning) {
            stopService(Manager.listenerIntent);
        }
        stopbtn.setEnabled(false); //disable the stop button
        swb.setEnabled(false);
        waitStopListenerService();
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case 0: //re-enable buttons after service has stopped
                    progb.setVisibility(View.INVISIBLE);
                    startbtn.setEnabled(true); //enable the start button
                    swb.setEnabled(true);
                    break;
                case 1: //re-enable the checkbox after the setting has been saved
                    swb.setEnabled(true);
                    break;
                default:
            }
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
                    Message msg = new Message();
                    msg.arg1 = 0;
                    handler.sendMessage(msg);
                    cancel();
                }
            }
        }, 0, 1000);
    }

    public void changeStartOnBootSetting(View v) {
        //change the autostart variable and save it
        final Boolean oldValue = prefs.getBoolean("autostart", false);
        prefed.putBoolean("autostart", swb.isChecked()).commit();

        //disable the checkbox and wait until the value is saved
        swb.setEnabled(false);
        final Timer timerWait2 = new Timer();
        timerWait2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (prefs.getBoolean("autostart", false) != oldValue) {
                    Message msg = new Message();
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                    cancel();
                }
            }
        }, 0, 1000);
    }

    public void unMute(View v) {
        //unmute button
        Manager.mgr.setStreamMute(AudioManager.STREAM_MUSIC, false);
    }
}
