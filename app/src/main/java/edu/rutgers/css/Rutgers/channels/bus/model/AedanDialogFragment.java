package edu.rutgers.css.Rutgers.channels.bus.model;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

import edu.rutgers.css.Rutgers.R;
import edu.rutgers.css.Rutgers.api.model.bus.VehiclePrediction;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Popup fragment for selecting the bus you are interested in
 */
public class AedanDialogFragment extends DialogFragment {
    private static final String ARG_VEHICLE = "vehicle";

    private VehiclePrediction selected;
    private final PublishSubject<VehiclePrediction> predictionSubject = PublishSubject.create();
    public Observable<VehiclePrediction> getSelection() {
        return predictionSubject.asObservable();
    }

    public static Bundle createArgs(ArrayList<VehiclePrediction> vehiclePrediction) {
        final Bundle args = new Bundle();
        args.putSerializable(ARG_VEHICLE, vehiclePrediction);
        return args;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        ArrayList<VehiclePrediction> predictions = (ArrayList<VehiclePrediction>) args.getSerializable(ARG_VEHICLE);

        final View v = LayoutInflater
            .from(getContext())
            .inflate(R.layout.dialog_bus_vehicle_select, null);

        final Spinner vehicleSelect = (Spinner) v.findViewById(R.id.vehicle_select);
        vehicleSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selected = (VehiclePrediction) adapterView.getAdapter().getItem(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selected = null;
            }
        });
        final ArrayAdapter<VehiclePrediction> vehicleAdapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            predictions
        );
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleSelect.setAdapter(vehicleAdapter);
        return new AlertDialog.Builder(getContext())
            .setView(v)
            .setMessage("Track bus arriving in...")
            .setPositiveButton("Submit", (dialogInterface, i) ->
                predictionSubject.onNext(selected)
            )
            .setNegativeButton("Cancel", (dialogInterface, i) -> {})
            .create();
    }
}
