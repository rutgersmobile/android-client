package edu.rutgers.css.Rutgers.channels.dtable.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A question/answer pair.
 */
public class DTableFAQ extends DTableElement implements Serializable {

    private final String answer;
    private final List<String> answerList;

    public DTableFAQ(JsonObject jsonObject, DTableElement parent) throws JsonSyntaxException {
        super(jsonObject, parent);

        answer = jsonObject.get("answer").getAsString();
        answerList = new ArrayList<>();
        answerList.add(answer);
    }

    public String getAnswer() {
        return answer;
    }

    @Override
    public List<?> getChildItemList() {
        return answerList;
    }
}
