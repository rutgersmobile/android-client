package edu.rutgers.css.Rutgers.channels.stream;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.ui.fragments.DtableChannelFragment;

/**
 * Has the Rutgers radio stream
 */

public class StreamDisplay extends DtableChannelFragment {
    public static final String HANDLE = "stream";
    private static final String TAG = "StreamDisplay";
    private static final String RADIO_URL = "http://crystalout.surfernetwork.com:8001/WRNU-FM_MP3";
    private ImageView playButton;

    public static Bundle createArgs() {
        final Bundle args = new Bundle();
        args.putString(ComponentFactory.ARG_COMPONENT_TAG, HANDLE);
        return args;
    }

    @Override
    public void onResume() {
        super.onResume();
        StreamService.playing()
            .compose(bindToLifecycle())
            .subscribe(playing -> {
                if (playButton != null) {
                    playButton.setImageDrawable(getIconForStream(playing));
                }
            }, this::logError);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = super.createView(inflater, container, savedInstanceState, R.layout.fragment_radio);
        playButton = (ImageView) v.findViewById(R.id.play_button);
        playButton.setImageDrawable(getIconForStream(StreamService.isPlaying()));
        playButton.setOnClickListener(view -> {
            if (!StreamService.isPlaying()) {
                StreamService.startStream(getContext(), true, RADIO_URL);
            } else {
                StreamService.startStream(getContext(), false, RADIO_URL);
            }
        });
        return v;
    }

    private Drawable getIconForStream(boolean playing) {
        int icon = playing
            ? R.drawable.ic_pause_black_24dp
            : R.drawable.ic_play_arrow_black_24dp;
        return ContextCompat.getDrawable(getContext(), icon);
    }
}
