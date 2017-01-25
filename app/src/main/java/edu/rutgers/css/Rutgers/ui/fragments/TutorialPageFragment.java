package edu.rutgers.css.Rutgers.ui.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.function.Function;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.utils.FuncWrapper;
import edu.rutgers.css.Rutgers.utils.LogUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialPageFragment extends Fragment {
    private TextView title, body;
    private ImageView img;
    private RelativeLayout parent;
    final private static String TAG = "TUTORIALPAGEFRAG";

    class Changes {
        public TutorialPageElement element;
        public String text;
        public ModifyValue mod;
        public Changes(TutorialPageElement element, String text, ModifyValue mod) {
            this.element = element;
            this.text = text;
            this.mod = mod;
        }
    }

    private ArrayList<Changes> changes = new ArrayList<>();
    public TutorialPageFragment() {
        // Required empty public constructor
    }

    public enum TutorialPageElement {
        TUTORIAL_PAGE_TITLE,
        TUTORIAL_PAGE_IMAGE,
        TUTORIAL_PAGE_DESCR,
        TUTORIAL_PAGE_BG
    }

    public enum ModifyValue {
        COLOR,
        TEXT,
        NONE
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tutorial_page, container, false);
        this.title = (TextView) v.findViewById(R.id.tutorial_fragment_title);
        this.body = (TextView) v.findViewById(R.id.tutorial_fragment_body);
        this.img = (ImageView) v.findViewById(R.id.tutorial_fragment_image);
        this.parent = (RelativeLayout) v.findViewById(R.id.basic_tutorial_page1);
        applyAllChanges();
        return v;
    }

    public void stylizeElement(TutorialPageElement element, String text, ModifyValue mod) {
        changes.add(new Changes(element, text, mod));
    }

    private void applyAllChanges() {
        Changes[] changesArray = toArray();
        for (Changes cng : changesArray) {
            applyChange(cng);
        }
    }

    private Changes[] toArray() {
        Changes[] changesArray = new Changes[changes.size()];
        for (int i = 0; i < changes.size(); i++) {
            changesArray[i] = changes.get(i);
        }
        return changesArray;
    }

    private void applyChange(Changes cng) {
        if (cng.element == TutorialPageElement.TUTORIAL_PAGE_IMAGE) {
            setImage(Integer.parseInt(cng.text));
            return;
        }

        if (cng.element == TutorialPageElement.TUTORIAL_PAGE_BG) {
            this.parent.setBackgroundColor(Color.parseColor(cng.text));
        }
        switch(cng.mod) {
            case COLOR:
                int color = Color.parseColor(cng.text);
                if (cng.element == TutorialPageElement.TUTORIAL_PAGE_TITLE) {
                    this.title.setTextColor(color);
                } else if (cng.element == TutorialPageElement.TUTORIAL_PAGE_DESCR) {
                    this.title.setTextColor(color);
                }
                break;
            case TEXT:
                if (cng.element == TutorialPageElement.TUTORIAL_PAGE_TITLE) {
                    this.title.setText(cng.text);
                } else if (cng.element == TutorialPageElement.TUTORIAL_PAGE_DESCR) {
                    this.body.setText(cng.text);
                }
        }
    }

    public void presetImageDisplayed(int src) {
        changes.add(new Changes(TutorialPageElement.TUTORIAL_PAGE_IMAGE, Integer.toString(src), ModifyValue.NONE));
    }

    public void presetBackground(String color) {
        changes.add(new Changes(TutorialPageElement.TUTORIAL_PAGE_BG, color, ModifyValue.NONE));
    }

    private void setImage(int src) {
        this.img.setImageResource(src);
    }

    public String getPageElementData(TutorialPageElement element) {
        if (element == TutorialPageElement.TUTORIAL_PAGE_TITLE) {
            return this.title.getText().toString();
        } else if (element == TutorialPageElement.TUTORIAL_PAGE_DESCR) {
            return this.body.getText().toString();
        }
        return null;
    }
}
