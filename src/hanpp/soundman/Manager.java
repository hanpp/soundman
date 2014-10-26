package hanpp.soundman;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class Manager {
    public static AudioManager mgr;
    public static Intent listenerIntent;
    public static boolean listenerServiceRunning;
    public static boolean audioMuted;
    private static boolean initialized = false;

    public Manager(Context context) {
        mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        listenerIntent = new Intent(context, JackListener.class);
        listenerServiceRunning = false;
        audioMuted = false;

        initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
