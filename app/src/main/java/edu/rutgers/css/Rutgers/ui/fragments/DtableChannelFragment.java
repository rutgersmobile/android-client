package edu.rutgers.css.Rutgers.ui.fragments;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.link.Link;

/**
 * Base class for channels that can be in a Dtable
 * currently just figures out the link
 */
public class DtableChannelFragment extends BaseChannelFragment {
    @Override
    public Link getLink() {
        final Bundle args = getArguments();
        final String topHandle = args.getString(ComponentFactory.ARG_HANDLE_TAG);
        final List<String> history = args.getStringArrayList(ComponentFactory.ARG_HIST_TAG);
        final String pathPart = args.getString(ComponentFactory.ARG_TITLE_TAG);

        if (topHandle != null && history != null && pathPart != null) {
            final List<String> linkArgs = new ArrayList<>();
            for (final String title : history) {
                linkArgs.add(title);
            }
            linkArgs.add(pathPart);
            return new Link(topHandle, linkArgs, getLinkTitle());
        }

        return null;
    }
}
