package hanpp.soundman;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;

public class MainActivity extends Activity {

    private Intent intent;
    private Switch sws;
    private Switch swb;
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefed;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


        prefs = this.getSharedPreferences("hanpp.soundman", Context.MODE_PRIVATE);
        prefed = prefs.edit();

        sws = (Switch) findViewById(R.id.switch_jackService);
        swb = (Switch) findViewById(R.id.switch_startonBoot);

        intent = new Intent(this, JackListener.class);

        if (!prefs.contains("autostart")) {
            ChangeStartOnBootSetting(null); //make the autostart setting if it doesn't exist
        }

        if (!prefs.getBoolean("autostart", false)) {
            swb.setChecked(false); //set the switch to false if autostart is false
        }
        if (BootStart.intent2 == null) {
            startService(intent);
        }
    }

    public void JackServiceSwitch(View v) {
        if (sws.isChecked()) {
            startService(intent);
        } else {
            if (BootStart.intent2 != null) {
                stopService(BootStart.intent2);
                BootStart.intent2 = null;
            } else {
                stopService(intent);
            }
            JackListener.mgr.setStreamMute(AudioManager.STREAM_MUSIC, false);
        }
    }

    public void ChangeStartOnBootSetting(View v) {
        prefed.putBoolean("autostart", swb.isChecked());
        prefed.apply();
    }

    public void UnMute(View v) {
        //unmute button
        JackListener.mgr.setStreamMute(AudioManager.STREAM_MUSIC, false);
    }
}
