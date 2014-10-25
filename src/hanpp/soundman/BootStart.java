package hanpp.soundman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


public class BootStart extends BroadcastReceiver {

    public static Intent intent2;

    @Override
    public void onReceive(Context context, Intent intent) {
        //check preferences whether to start the service on boot

        SharedPreferences prefs = context.getSharedPreferences("hanpp.soundman", Context.MODE_PRIVATE);
        if (prefs.getBoolean("autostart", false)) {
            intent2 = new Intent(context, JackListener.class);
            context.startService(intent2);
        }
    }
}
