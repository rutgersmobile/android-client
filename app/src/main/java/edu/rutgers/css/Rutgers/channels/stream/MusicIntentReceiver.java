package edu.rutgers.css.Rutgers.channels.stream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.link.Link;

/**
 * Stop playing music when headphones are unplugged
 */
public class MusicIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            // I don't like this
            final List<String> pathParts = new ArrayList<>();
            pathParts.add("wrnu");
            StreamService.startStream(context, false, StreamService.getResourceUrl(), new Link("radio", pathParts, "WRNU").getUri(Link.Schema.RUTGERS));
        }
    }
}
