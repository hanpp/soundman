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

        if (!prefs.contains("autostart")) {
            changeStartOnBootSetting(null); //make the autostart setting if it doesn't exist
        }

        if (!prefs.getBoolean("autostart", false)) {
            swb.setChecked(false); //set the switch to false if autostart is false, checked by default
        }
        if (Manager.isInitialized()) { //check if manager has been initialized
            if (!Manager.listenerServiceRunning) { //start the service if it is not running
                startJackService(null); //start the service
            }
        } else {
            new Manager(this); //initialize the manager
        }
        startJackService(null); //disable the start button
    }

    public void startJackService(View v) {
        if (!Manager.listenerServiceRunning) {
            startService(Manager.listenerIntent);
            stopbtn.setEnabled(false); //disable the stop button
            startbtn.setEnabled(false); //disable the start button
            waitListenerService(true, 0); //wait until the service has started
        } else {
            startbtn.setEnabled(false); //disable the start button
        }
    }

    public void stopJackService(View v) {
        if (Manager.listenerServiceRunning) {
            stopbtn.setEnabled(false); //disable the stop button
            startbtn.setEnabled(false); //disable the start button
            stopService(Manager.listenerIntent);
        }
        waitListenerService(false, 1); //wait until the service has stopped
    }

    public void changeStartOnBootSetting(View v) {
        swb.setEnabled(false); //disable the checkbox
        final Boolean oldValue = prefs.getBoolean("autostart", false);  //get the old value
        prefed.putBoolean("autostart", swb.isChecked()).commit();  //change the autostart variable and save it
        waitingTimer(oldValue, 2); //wait until the value has changed
    }

    public void unMute(View v) {  //unmute button press
        Manager.mgr.setStreamMute(AudioManager.STREAM_MUSIC, false);
    }

    public void waitListenerService(final Boolean condition, final int arg) {
        waitingTimer(condition, arg); //wait until the service has started/stopped
    }

    public void waitingTimer(final boolean condition, final int arg) {
        progb.setVisibility(View.VISIBLE); //unhide the loading circle
        final Timer timerWait = new Timer();
        timerWait.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (checkCondition(arg, condition)) {
                    Message msg = new Message();
                    msg.arg1 = arg;
                    handler.sendMessage(msg);
                    cancel();
                }
            }
        }, 0, 1000);
    }

    public boolean checkCondition(int arg, boolean condition){
        switch (arg){
            case 0: //fall through
            case 1:
                return Manager.listenerServiceRunning == condition; //check if listenerservice started/stopped
            case 2:
                return prefs.getBoolean("autostart", false) != condition; //check if autostart variable changed
        }
        return true;
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progb.setVisibility(View.INVISIBLE); //hide the loading circle
            switch (msg.arg1) {
                case 0:
                    //re-enable stop button after service has started
                    stopbtn.setEnabled(true);
                    swb.setEnabled(true);
                    break;
                case 1://re-enable start button after service has stopped
                    startbtn.setEnabled(true); //enable the start button
                    swb.setEnabled(true);
                    break;
                case 2: //re-enable the checkbox after the setting has been saved
                    swb.setEnabled(true);
                    break;
                default:
            }
        }
    };
}