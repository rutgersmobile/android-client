package edu.rutgers.css.Rutgers.fragments;

import org.json.JSONArray;

import edu.rutgers.css.Rutgers2.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FoodNutrition extends DialogFragment{
	public static FoodNutrition newInstance(String name, int calories, String serving, String[] ingredients){
		FoodNutrition frag = new FoodNutrition();
		Bundle args = new Bundle();
		args.putString("name", name);
		args.putInt("calories", calories);
		args.putString("serving", serving);
		args.putStringArray("ingredients", ingredients);
		frag.setArguments(args);
		return frag;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_food_nutrition, null);
		
		builder.setTitle(getArguments().getString("name"));
		builder.setView(view);
		
		TextView textCalories = (TextView)view.findViewById(R.id.foodnutritiontext1);
		textCalories.setText(""+getArguments().getInt("calories"));
		TextView textServing = (TextView)view.findViewById(R.id.foodnutritiontext2);
		textServing.setText(getArguments().getString("serving"));
		
		ListView listIngredients = (ListView)view.findViewById(R.id.foodnutritionlistview);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.title_row,R.id.title,
				getArguments().getStringArray("ingredients"));
		listIngredients.setAdapter(adapter);
		
		return builder.create();
	}
}
