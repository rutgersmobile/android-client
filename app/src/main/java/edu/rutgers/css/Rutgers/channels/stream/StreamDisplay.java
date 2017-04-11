package edu.rutgers.css.Rutgers.channels.stream;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.channels.ComponentFactory;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.ui.fragments.DtableChannelFragment;
import edu.rutgers.css.Rutgers.ui.fragments.WebDisplay;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGE;

/**
 * Has the Rutgers radio stream
 */

public class StreamDisplay extends DtableChannelFragment {
    public static final String HANDLE = "radio";
    private static final String TAG = "StreamDisplay";
    private String radioUrl;
    private String title;
    private ImageView playButton;

    private static final String TWITTER_TITLE = "Twitter";
    private static final String INSTAGRAM_TITLE = "Instagram";
    private static final String FACEBOOK_TITLE = "Facebook";
    private static final String SOUNDCLOUD_TITLE = "SoundCloud";

    private static final String TWITTER_URL = "https://twitter.com/WRNU";
    private static final String INSTAGRAM_URL = "https://www.instagram.com/_wrnu/";
    private static final String FACEBOOK_URL = "https://www.facebook.com/CampusBeatRadio/";
    private static final String SOUNDCLOUD_URL = "https://soundcloud.com/rutgers-wrnu";

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

        final TextView campusBeat = (TextView) v.findViewById(R.id.campus_beat);
        campusBeat.setPaintFlags(campusBeat.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        final ImageView twitterLogo = (ImageView) v.findViewById(R.id.twitter_logo);
        final ImageView instagramLogo = (ImageView) v.findViewById(R.id.instagram_logo);
        final ImageView facebookLogo = (ImageView) v.findViewById(R.id.facebook_logo);
        final ImageView soundCloudLogo = (ImageView) v.findViewById(R.id.soundCloud_logo);

        try {
            final URL twitterURL = new URL(TWITTER_URL);
            final URL instagramURL = new URL(INSTAGRAM_URL);
            final URL facebookURL = new URL(FACEBOOK_URL);
            final URL soundCloudURL = new URL(SOUNDCLOUD_URL);

            setupLink(twitterLogo, TWITTER_TITLE, twitterURL);
            setupLink(instagramLogo, INSTAGRAM_TITLE, instagramURL);
            setupLink(facebookLogo, FACEBOOK_TITLE, facebookURL);
            setupLink(soundCloudLogo, SOUNDCLOUD_TITLE, soundCloudURL);
        } catch (MalformedURLException e) {
            LOGE(TAG, e.getMessage());
        }

        final Button emailButton = (Button) v.findViewById(R.id.email);
        emailButton.setOnClickListener(view -> {
            final Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:wrnurutgersradio@gmail.com"));

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                LOGE(TAG, e.getMessage());
            }
        });

        return v;
    }

    private void setupLink(View v, String title, URL url) {
        v.setOnClickListener(view -> {
            final Bundle args = WebDisplay.createArgs(title, url.toString());
            switchFragments(args);
        });
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

    @Override
    public String getChannelHandle() {
        return HANDLE;
    }
}
