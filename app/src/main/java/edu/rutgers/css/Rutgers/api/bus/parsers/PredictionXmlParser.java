package edu.rutgers.css.Rutgers.api.bus.parsers;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;

import edu.rutgers.css.Rutgers.api.XmlParser;
import edu.rutgers.css.Rutgers.api.bus.model.Prediction;
import edu.rutgers.css.Rutgers.api.bus.model.Predictions;

/**
 * Xml parser used to get predictions for routes in the bus channel
 */
public class PredictionXmlParser implements XmlParser<Predictions> {
    private static final String ns = null;

    public enum PredictionType {
        ROUTE, STOP
    }

    PredictionType type;

    public PredictionXmlParser(PredictionType type) {
        this.type = type;
    }

    public Predictions parse(InputStream in) throws XmlPullParserException, IOException {
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

    private Predictions readBody(XmlPullParser parser) throws XmlPullParserException, IOException {
        Predictions predictions = new Predictions(new LinkedHashSet<String>(), new ArrayList<Prediction>());

        parser.require(XmlPullParser.START_TAG, ns, "body");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("predictions")) {
                predictions.add(readPredictions(parser));
            } else {
                skip(parser);
            }
        }

        return predictions;
    }

    private Predictions readPredictions(XmlPullParser parser) throws XmlPullParserException, IOException {
        Predictions predictions = new Predictions(new LinkedHashSet<String>(), new ArrayList<Prediction>());
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
            switch (name) {
                case "direction":
                    predictions.getPredictions().add(readDirection(parser, title, tag));
                    break;
                case "message":
                    predictions.getMessages().add(readMessage(parser));
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return predictions;
    }

    private String readMessage(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "message");
        String message = parser.getAttributeValue(ns, "text");
        while (parser.next() != XmlPullParser.END_TAG) {
            skip(parser);
        }
        return message;
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
