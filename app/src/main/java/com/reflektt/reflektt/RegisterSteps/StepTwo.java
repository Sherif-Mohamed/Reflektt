package com.reflektt.reflektt.RegisterSteps;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.backendless.BackendlessUser;
import com.reflektt.reflektt.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class StepTwo extends Fragment {
    private EditText firstName;
    private EditText secondName;

    public StepTwo() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_step_two, container, false);

        firstName = (EditText) v.findViewById(R.id.reg_firstname);
        secondName = (EditText) v.findViewById(R.id.reg_lastname);
        return v;
    }
    public BackendlessUser process(BackendlessUser user){

        String first = firstName.getText().toString();
        String last = secondName.getText().toString();
        if (first.equals("")){
            Toast.makeText(getContext(),getString(R.string.firstname),Toast.LENGTH_SHORT).show();
            return null;
        }
        else if (last.equals("")){
            Toast.makeText(getContext(),getString(R.string.lastname),Toast.LENGTH_SHORT).show();
            return null;
        }
        else{
            user.setProperty("name",first+" "+last);
            return user;
        }
    }
}
