package edu.rutgers.css.Rutgers.channels.feedback.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import edu.rutgers.css.Rutgers.Config;
import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.ChannelManager;
import edu.rutgers.css.Rutgers.api.ComponentFactory;
import edu.rutgers.css.Rutgers.channels.ruinfo.fragments.RUInfoMain;
import edu.rutgers.css.Rutgers.interfaces.ChannelManagerProvider;
import edu.rutgers.css.Rutgers.link.Link;
import edu.rutgers.css.Rutgers.model.Channel;
import edu.rutgers.css.Rutgers.ui.fragments.BaseChannelFragment;
import edu.rutgers.css.Rutgers.utils.AppUtils;
import edu.rutgers.css.Rutgers.utils.RutgersUtils;
import lombok.Data;

import static edu.rutgers.css.Rutgers.utils.LogUtils.LOGW;

/** Feedback form. */
public class FeedbackMain extends BaseChannelFragment implements OnItemSelectedListener {

    /* Log tag and component handle */
    private static final String TAG = "FeedbackMain";
    public static final String HANDLE = "feedback";
    private static final String API = Config.API_BASE + "feedback.php";
    //private static final String API = "http://sauron.rutgers.edu/~jamchamb/feedback.php"; // TODO Replace

    /* Argument bundle tags */
    private static final String ARG_TITLE_TAG       = ComponentFactory.ARG_TITLE_TAG;

    /* Member data */
    private OkHttpClient client;
    private boolean mLockSend;

    /* View references */
    private Spinner mSubjectSpinner;
    private Spinner mChannelSpinner;
    private TextView mChannelSpinnerText;
    private EditText mMessageEditText;
    private EditText mEmailEditText;
    private CheckBox mRequestReplyCheckbox;

    @Override
    public Link getLink() {
        return null;
    }

    enum Status {
        SUCCESS, FAIL, IOFAIL
    };

    /**
     * Class for holding result status.
     */
    @Data
    class ResultHolder {
        final String statusMessage;
        final Status status;
    }


    public FeedbackMain() {
        // Required empty public constructor
    }

    public static Bundle createArgs(@NonNull String title) {
        Bundle bundle = new Bundle();
        bundle.putString(ComponentFactory.ARG_COMPONENT_TAG, FeedbackMain.HANDLE);
        bundle.putString(ARG_TITLE_TAG, title);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        client = new OkHttpClient();
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        final View v = super.createView(inflater, parent, savedInstanceState, R.layout.fragment_feedback_main);
        final Bundle args = getArguments();

        // Set title from JSON
        if (args.getString(ARG_TITLE_TAG) != null) getActivity().setTitle(args.getString(ARG_TITLE_TAG));
        else getActivity().setTitle(R.string.feedback_title);
        
        mLockSend = false;

        mRequestReplyCheckbox = (CheckBox) v.findViewById(R.id.request_reply);
        mRequestReplyCheckbox.setEnabled(false);

        mMessageEditText = (EditText) v.findViewById(R.id.messageEditText);
        mEmailEditText = (EditText) v.findViewById(R.id.emailEditText);
        mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (StringUtils.isBlank(charSequence)) {
                    mRequestReplyCheckbox.setEnabled(false);
                } else {
                    mRequestReplyCheckbox.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        mChannelSpinnerText = (TextView) v.findViewById(R.id.channel_spinner_text);

        mSubjectSpinner = (Spinner) v.findViewById(R.id.subjectSpinner);
        ArrayAdapter<CharSequence> subjectAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.feedback_subjects, R.layout.spinner);
        subjectAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mSubjectSpinner.setAdapter(subjectAdapter);
        mSubjectSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<String> channelAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item);
        channelAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mChannelSpinner = (Spinner) v.findViewById(R.id.channelSpinner);
        mChannelSpinner.setAdapter(channelAdapter);

        final String homeCampus = RutgersUtils.getHomeCampus(getActivity());

        ChannelManager channelManager = ((ChannelManagerProvider) getActivity()).getChannelManager();
        for (Channel channel: channelManager.getChannels()) {
            channelAdapter.add(channel.getTitle(homeCampus));
        }
        
        return v;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.feedback_menu, menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        // Handle send button
        if (item.getItemId() == R.id.action_send) {
            if (!mLockSend) sendFeedback();
            return true;
        }
        
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Get rid of view references
        mSubjectSpinner = null;
        mChannelSpinner = null;
        mMessageEditText = null;
        mEmailEditText = null;
    }
        
    /**
     * Submit the feedback
     */
    private void sendFeedback() {
        // Empty message - do nothing
        if (StringUtils.isBlank(mMessageEditText.getText().toString())) {
            return;
        }

        // TODO Validate email address format

        Integer wantsResonse = mRequestReplyCheckbox.isChecked() && mRequestReplyCheckbox.isEnabled() ? 1 : 0;
        
        // Build POST request
        final FormEncodingBuilder builder = new FormEncodingBuilder()
            .add("subject", (String)(mSubjectSpinner.getSelectedItem()))
            .add("email", mEmailEditText.getText().toString().trim())
            .add("uuid", AppUtils.getUUID(getActivity()))
            .add("message", mMessageEditText.getText().toString().trim())
            .add("wants_response", wantsResonse.toString());
        // Post the selected channel if this is channel feedback
        if (mSubjectSpinner.getSelectedItem().equals(getString(R.string.feedback_channel_feedback))) {
            builder.add("channel", (String) (mChannelSpinner.getSelectedItem()));
        }
        //params.put("debuglog", "");
        builder.add("version", Config.VERSION);
        builder.add("osname", Config.OSNAME);
        builder.add("betamode", Config.BETAMODE);
        
        // Lock send button until POST request goes through
        mLockSend = true;

        final String feedbackErrorString = getString(R.string.feedback_error);
        final String feedbackSuccessString = getString(R.string.feedback_success);

        final Gson gson = new Gson();

        // We need to make our request in the background.
        // Android doesn't allow network on the ui thread
        new AsyncTask<Void, Void, ResultHolder>() {
            @Override
            protected ResultHolder doInBackground(Void... params) {
                final Request request = new Request.Builder()
                        .url(API)
                        .post(builder.build())
                        .build();
                String status = "";
                try {
                    final Response response = client.newCall(request).execute();
                    final JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);
                    mLockSend = false;
                    if (jsonObject.getAsJsonArray("errors") != null) {
                        // If errors exist, get the first and set the status from it
                        JsonArray errors = jsonObject.getAsJsonArray("errors");
                        try {
                            JsonElement error = errors.get(0);
                            status = error.getAsString();
                        } catch (IndexOutOfBoundsException | ClassCastException e) {
                            status = feedbackErrorString;
                        }
                        return new ResultHolder(status, FeedbackMain.Status.SUCCESS);
                    } else if (jsonObject.has("success") && !jsonObject.isJsonNull()) {
                        // Set the status message in the response, or if it doesn't exist, use
                        // our own
                        try {
                            status = jsonObject.getAsJsonPrimitive("success").getAsString();
                        } catch (ClassCastException e) {
                            status = feedbackSuccessString;
                        }
                        return new ResultHolder(status, FeedbackMain.Status.FAIL);
                    }
                } catch (IOException e) {
                    LOGW(TAG, e.getMessage());
                }
                // Failure is so catastrophic we found out before we even got to the server
                return new ResultHolder(status, FeedbackMain.Status.IOFAIL);
            }

            @Override
            protected void onPostExecute(ResultHolder result) {
                // Display message in slightly different ways depending on
                // if there was a success or failure
                switch (result.getStatus()) {
                    case SUCCESS:
                        Toast.makeText(getActivity(), result.getStatusMessage(), Toast.LENGTH_SHORT).show();
                        // Only reset the form on success in case the user wants to try
                        // to send it again
                        resetForm();
                        break;
                    case FAIL:
                        Toast.makeText(getActivity(), result.getStatusMessage(), Toast.LENGTH_SHORT).show();
                        break;
                    case IOFAIL:
                        Toast toast = Toast.makeText(getActivity(), result.getStatusMessage(), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP, 0, 125);
                        toast.show();
                        break;
                }
            }
        }.execute();
    }
    
    /**
     * Reset the feedback forms.
     */
    private void resetForm() {
        if (mSubjectSpinner != null) mSubjectSpinner.setSelection(0);
        if (mChannelSpinner != null) mChannelSpinner.setSelection(0);
        if (mEmailEditText != null) mEmailEditText.setText("");
        if (mMessageEditText != null) mMessageEditText.setText("");

        // Close soft keyboard
        AppUtils.closeKeyboard(getActivity());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.subjectSpinner) {
            String selection = (String) parent.getItemAtPosition(position);
            
            // Channel feedback allows user to select a specific channel
            if (selection.equals(getString(R.string.feedback_channel_feedback))) {
                if (mChannelSpinner != null) mChannelSpinner.setVisibility(View.VISIBLE);
                if (mChannelSpinnerText != null) mChannelSpinnerText.setVisibility(View.VISIBLE);
            } else {
                if (mChannelSpinner != null) mChannelSpinner.setVisibility(View.GONE);
                if (mChannelSpinnerText != null) mChannelSpinnerText.setVisibility(View.GONE);
            }
            
            // "General questions" boots you to RU-info. BURNNNN!!!
            if (selection.equals(getString(R.string.feedback_general))) {
                // Reset selection so that the user can hit back without getting booted right away
                // (this means general questions can never be the default option!)
                parent.setSelection(0);
                
                // Launch RU-info channel
                switchFragments(RUInfoMain.createArgs(null));
            }

        }
        
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
    
}
