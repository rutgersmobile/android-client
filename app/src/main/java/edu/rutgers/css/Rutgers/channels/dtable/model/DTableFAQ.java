package edu.rutgers.css.Rutgers.channels.dtable.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A question/answer pair.
 */
public class DTableFAQ extends DTableElement {

    private boolean window;
    private String answer;
    private boolean opened;

    public DTableFAQ(JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        window = jsonObject.optBoolean("window");
        answer = jsonObject.getString("answer");
    }

    public boolean isWindow() {
        return window;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

}
