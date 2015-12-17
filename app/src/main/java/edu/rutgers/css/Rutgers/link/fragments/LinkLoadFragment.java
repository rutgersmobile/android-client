package edu.rutgers.css.Rutgers.link.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import com.squareup.otto.Subscribe;

import java.io.Serializable;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.soc.model.ScheduleAPI;
import edu.rutgers.css.Rutgers.link.LinkLoadArgs;
import edu.rutgers.css.Rutgers.link.LinkLoadTask;
import edu.rutgers.css.Rutgers.model.Channel;
import edu.rutgers.css.Rutgers.ui.MainActivity;
import edu.rutgers.css.Rutgers.link.LinkBus;
import edu.rutgers.css.Rutgers.utils.PrefUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import lombok.NonNull;

import static edu.rutgers.css.Rutgers.utils.LogUtils.*;

/**
 * Fragment for performing deep linking
 */
public final class LinkLoadFragment extends Fragment {

    public static final String TAG = "TaskFragment";

    /* Bundle arguments */
    private static final String ARG_CHANNEL_TAG = "channel";
    private static final String ARG_PATH_TAG = "path";

    public static Bundle createBundle(@NonNull final Channel channel,
                                      @NonNull final List<String> pathParts) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_CHANNEL_TAG, channel);
        bundle.putSerializable(ARG_PATH_TAG, (Serializable) pathParts);
        return bundle;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        final Bundle args = getArguments();

        final List<String> pathParts = (List<String>) args.getSerializable(ARG_PATH_TAG);
        final Channel channel = (Channel) getArguments().getSerializable(ARG_CHANNEL_TAG);
        final String homeCampus = RutgersUtils.getHomeCampus(getContext());

        if (channel != null && homeCampus != null) {
            final String channelTag = channel.getView();

            // Launch channel immediately if that's all we have
            if (pathParts != null && pathParts.size() == 0) {
                LOGI(TAG, "Linking to channel: " + channelTag);
                Bundle channelArgs = channel.getBundle();

                channelArgs.putString(ComponentFactory.ARG_TITLE_TAG, channel.getTitle(homeCampus));
                switchFragments(channelArgs);
                return;
            }

            // We don't have a reference to the enclosing activity in the task
            // so we have to get all this information here
            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            final String prefLevel = sharedPref.getString(PrefUtils.KEY_PREF_SOC_LEVEL, ScheduleAPI.CODE_LEVEL_UNDERGRAD);
            final String prefCampus = sharedPref.getString(PrefUtils.KEY_PREF_SOC_CAMPUS, ScheduleAPI.CODE_CAMPUS_NB);
            final String prefSemester = sharedPref.getString(PrefUtils.KEY_PREF_SOC_SEMESTER, null);

            LinkBus.getInstance().register(this);
            new LinkLoadTask(homeCampus, prefCampus, prefLevel, prefSemester)
                    .execute(new LinkLoadArgs(channel, pathParts));
        }
    }

    /**
     * Method called when the task comes back. Launches the desired fragment
     * @param args Arguments for fragment creation
     */
    @Subscribe
    public void switchFragments(Bundle args) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).getFragmentMediator().switchFragments(args);
        }
    }
}
