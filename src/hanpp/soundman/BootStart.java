package hanpp.soundman;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;


public class BootStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("hanpp.soundman", Context.MODE_PRIVATE);
        if (prefs.getBoolean("autostart", false)) { //check preferences whether to start the service on boot
            if (Manager.isInitialized()) { //check if manager has been initialized
                context.startService(Manager.listenerIntent);
            } else {
                new Manager(context);
                context.startService(Manager.listenerIntent);
            }
        }
    }
}
