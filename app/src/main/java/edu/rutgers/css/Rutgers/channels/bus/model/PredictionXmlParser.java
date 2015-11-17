package edu.rutgers.css.Rutgers.channels.bus.model;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import edu.rutgers.css.Rutgers.interfaces.XmlParser;

/**
 * Xml parser used to get predictions for routes in the bus channel
 */
public class PredictionXmlParser implements XmlParser<List<Prediction>> {
    private static final String ns = null;

    public enum PredictionType {
        ROUTE, STOP
    }

    PredictionType type;

    public PredictionXmlParser(PredictionType type) {
        this.type = type;
    }

    public List<Prediction> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readBody(parser);
        } finally {
            in.close();
        }
    }

    private List<Prediction> readBody(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Prediction> entries = new ArrayList<>();

        parser.require(XmlPullParser.START_TAG, ns, "body");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("predictions")) {
                entries.addAll(readPredictions(parser));
            } else {
                skip(parser);
            }
        }

        return entries;
    }

    private List<Prediction> readPredictions(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Prediction> entries = new ArrayList<>();
        parser.require(XmlPullParser.START_TAG, ns, "predictions");
        String title;
        String tag;
        if (type == PredictionType.ROUTE) {
            title = parser.getAttributeValue(ns, "stopTitle");
            tag = parser.getAttributeValue(ns, "stopTag");
        } else {
            title = parser.getAttributeValue(ns, "routeTitle");
            tag = parser.getAttributeValue(ns, "routeTag");
        }
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("direction")) {
                entries.add(readDirection(parser, title, tag));
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private Prediction readDirection(XmlPullParser parser, String stopTitle, String stopTag) throws XmlPullParserException, IOException {
        Prediction prediction = new Prediction(stopTitle, stopTag);
        parser.require(XmlPullParser.START_TAG, ns, "direction");
        String direction = parser.getAttributeValue(ns, "title");
        prediction.setDirection(direction);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("prediction")) {
                prediction.addMinutes(readPrediction(parser));
            } else {
                skip(parser);
            }
        }
        return prediction;
    }

    private int readPrediction(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "prediction");
        int minutes = Integer.parseInt(parser.getAttributeValue(ns, "minutes"));
        while (parser.next() != XmlPullParser.END_TAG) {}
        parser.require(XmlPullParser.END_TAG, ns, "prediction");
        return minutes;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
