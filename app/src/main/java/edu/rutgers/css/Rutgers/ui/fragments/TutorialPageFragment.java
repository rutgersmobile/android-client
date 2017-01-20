package edu.rutgers.css.Rutgers.ui.fragments;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.function.Function;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.utils.FuncWrapper;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialPageFragment extends Fragment {
    private TextView title, body;
    private ImageView img;
    private Button nextBtn;
    public TutorialPageFragment() {
        // Required empty public constructor
    }

    public enum TutorialPageElement {
        TUTORIAL_PAGE_TITLE,
        TUTORIAL_PAGE_IMAGE,
        TUTORIAL_PAGE_DESCR
    }

    public enum ModifyValue {
        COLOR,
        TEXT
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_tutorial_page, container, false);
        this.title = (TextView) v.findViewById(R.id.tutorial_fragment_title);
        this.body = (TextView) v.findViewById(R.id.tutorial_fragment_body);
        this.img = (ImageView) v.findViewById(R.id.tutorial_fragment_image);
        this.nextBtn = (Button) v.findViewById(R.id.tutorial_fragment_btn);
        return v;
    }

    public void stylizeTextElement(TutorialPageElement element, String text, ModifyValue mod) {
        switch(mod) {
            case COLOR:
                int color = Color.parseColor(text);
                if (element == TutorialPageElement.TUTORIAL_PAGE_TITLE) {
                    this.title.setTextColor(color);
                } else if (element == TutorialPageElement.TUTORIAL_PAGE_DESCR) {
                    this.title.setTextColor(color);
                }
                break;
            case TEXT:
                if (element == TutorialPageElement.TUTORIAL_PAGE_TITLE) {
                    this.title.setText(text);
                } else if (element == TutorialPageElement.TUTORIAL_PAGE_DESCR) {
                    this.body.setText(text);
                }
        }
    }

    public void setImage(String src) {

    }

    public String[] getPageElementData(TutorialPageElement element) {
        if (element == TutorialPageElement.TUTORIAL_PAGE_TITLE) {
            return new String[]{ this.title.getText().toString() };
        } else if (element == TutorialPageElement.TUTORIAL_PAGE_DESCR) {
            return new String[]{ this.body.getText().toString() };
        }
        return null;
    }

    public void overrideDefaultButtonBehaviour(FuncWrapper.Function0 fn) {
        nextBtn.setOnClickListener(view -> fn.run());
    }
}
