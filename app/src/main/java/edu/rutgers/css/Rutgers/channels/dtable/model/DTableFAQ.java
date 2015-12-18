package edu.rutgers.css.Rutgers.channels.dtable.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * A question/answer pair.
 */
public class DTableFAQ extends DTableElement {

    private boolean window;
    private String answer;
    private boolean opened;

    public DTableFAQ(JsonObject jsonObject, DTableElement parent) throws JsonSyntaxException {
        super(jsonObject, parent);

        JsonPrimitive p = jsonObject.getAsJsonPrimitive("window");
        window = p != null && p.getAsBoolean();
        answer = jsonObject.getAsJsonPrimitive("answer").getAsString();
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
