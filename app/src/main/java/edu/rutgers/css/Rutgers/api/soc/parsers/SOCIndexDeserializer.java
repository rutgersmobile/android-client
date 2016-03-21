package edu.rutgers.css.Rutgers.api.soc.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import edu.rutgers.css.Rutgers.api.soc.model.SOCIndex;
import edu.rutgers.css.Rutgers.utils.JsonUtils;

/**
 * Custom deserializer for SOCIndex. This is needed because the way the json is structured makes it
 * difficult to automatically deserialize with GSON
 */
public class SOCIndexDeserializer implements JsonDeserializer<SOCIndex> {
    String campusCode;
    String levelCode;
    String semesterCode;

    public SOCIndexDeserializer(String campusCode, String levelCode, String semesterCode) {
        this.campusCode = campusCode;
        this.levelCode = levelCode;
        this.semesterCode = semesterCode;
    }

    @Override
    public SOCIndex deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if (!jsonObject.has("abbrevs") || !jsonObject.has("courses") || !jsonObject.has("ids") || !jsonObject.has("names")) {
            throw new IllegalArgumentException("Invalid index, missing critical fields");
        }

        JsonObject abbrevs = jsonObject.getAsJsonObject("abbrevs");
        HashMap<String, String[]> abbreviations = new HashMap<>();
        for (Map.Entry<String, JsonElement> e : abbrevs.entrySet()) {
            String curAbbrev = e.getKey();
            JsonArray curContents = e.getValue().getAsJsonArray();
            abbreviations.put(curAbbrev, JsonUtils.jsonToStringArray(curContents));
        }

        JsonObject ids = jsonObject.getAsJsonObject("ids");
        HashMap<String, SOCIndex.IndexSubject> subjectsByCode = new HashMap<>();
        for (Map.Entry<String, JsonElement> e : ids.entrySet()) {
            String curID = e.getKey();
            JsonObject curContents = e.getValue().getAsJsonObject();

            JsonObject curCourses = curContents.getAsJsonObject("courses");
            HashMap<String, String> courseMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> e2 : curCourses.entrySet()) {
                String curCourseID = e2.getKey();
                String curCourseName = e2.getValue().getAsString();
                courseMap.put(curCourseID, curCourseName);
            }

            SOCIndex.IndexSubject newSubject = new SOCIndex.IndexSubject();
            newSubject.setId(curID);
            newSubject.setName(curContents.getAsJsonPrimitive("name").getAsString());
            newSubject.setCourses(courseMap);

            subjectsByCode.put(curID, newSubject);
        }

        JsonObject names = jsonObject.getAsJsonObject("names");
        HashMap<String, String> subjectsByName = new HashMap<>();
        for (Map.Entry<String, JsonElement> e : names.entrySet()) {
            String curName = e.getKey();
            String curContents = e.getValue().getAsString();
            subjectsByName.put(curName, curContents);
        }

        JsonObject courses = jsonObject.getAsJsonObject("courses");
        HashMap<String, SOCIndex.IndexCourse> coursesByName = new HashMap<>();
        for (Map.Entry<String, JsonElement> e : courses.entrySet()) {
            String curCourseName = e.getKey();
            JsonObject curContents = e.getValue().getAsJsonObject();
            SOCIndex.IndexCourse newCourse = new SOCIndex.IndexCourse();
            newCourse.setCourse(curContents.getAsJsonPrimitive("course").getAsString());
            newCourse.setSubj(curContents.getAsJsonPrimitive("subj").getAsString());
            coursesByName.put(curCourseName, newCourse);
        }

        return new SOCIndex(campusCode, levelCode, semesterCode, abbreviations, coursesByName, subjectsByCode, subjectsByName);
    }
}
