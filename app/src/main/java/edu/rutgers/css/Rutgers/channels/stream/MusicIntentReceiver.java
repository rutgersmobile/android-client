package edu.rutgers.css.Rutgers.channels.stream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

/**
 * Created by mattro on 12/19/16.
 */

public class MusicIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            StreamService.startStream(context, false, StreamService.getResourceUrl());
        }
    }
}
