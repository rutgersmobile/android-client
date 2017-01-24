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
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.fragments.DtableChannelFragment;

/**
 * Has the Rutgers radio stream
 */

public class StreamDisplay extends DtableChannelFragment {
    public static final String HANDLE = "radio";
    private static final String TAG = "StreamDisplay";
    private String radioUrl;
    private String title;
    private ImageView playButton;

    public static Bundle createArgs(String title, String url) {
        final Bundle args = new Bundle();
        args.putString(ComponentFactory.ARG_COMPONENT_TAG, HANDLE);
        args.putString(ComponentFactory.ARG_TITLE_TAG, title);
        args.putString(ComponentFactory.ARG_URL_TAG, url);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        radioUrl = args.getString(ComponentFactory.ARG_URL_TAG);
        title = args.getString(ComponentFactory.ARG_TITLE_TAG);
    }

    @Override
    public void onResume() {
        super.onResume();
        setPlayButtonIcon();
        StreamService.playing()
            .compose(bindToLifecycle())
            .subscribe(playing -> {
                if (playButton != null) {
                    playButton.setImageDrawable(getIconForStream(playing));
                }
            }, this::logError);
        getActivity().setTitle(title);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = super.createView(inflater, container, savedInstanceState, R.layout.fragment_radio);
        hideProgressCircle();
        playButton = (ImageView) v.findViewById(R.id.play_button);
        setPlayButtonIcon();
        playButton.setOnClickListener(view -> {
            if (!StreamService.isPlaying()) {
                StreamService.startStream(getContext(), true, radioUrl, getLink().getUri(Link.Schema.RUTGERS));
            } else {
                StreamService.startStream(getContext(), false, radioUrl, getLink().getUri(Link.Schema.RUTGERS));
            }
        });
        ImageView stopButton = (ImageView) v.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(view -> StreamService.stopStream(getContext()));
        return v;
    }

    private void setPlayButtonIcon() {
        if (playButton != null) {
            playButton.setImageDrawable(getIconForStream(StreamService.isPlaying()));
        }
    }

    private Drawable getIconForStream(boolean playing) {
        int icon = playing
            ? R.drawable.ic_pause_black_24dp
            : R.drawable.ic_play_arrow_black_24dp;
        return ContextCompat.getDrawable(getContext(), icon);
    }
}
