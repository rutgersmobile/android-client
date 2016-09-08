package edu.rutgers.css.Rutgers.channels.dtable.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

/**
 * A question/answer pair.
 */
public class DTableFAQ extends DTableElement implements Serializable {

    @Getter
    private final String answer;
    private final List<String> answerList;

    public DTableFAQ(JsonObject jsonObject, DTableElement parent) throws JsonSyntaxException {
        super(jsonObject, parent);

        answer = jsonObject.get("answer").getAsString();
        answerList = new ArrayList<>();
        answerList.add(answer);
    }

    @Override
    public List<?> getChildItemList() {
        return answerList;
    }
}
