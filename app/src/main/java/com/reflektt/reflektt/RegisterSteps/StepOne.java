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


public class StepOne extends Fragment {
    private EditText email;
    private EditText password;
    private EditText confirmation;

    public StepOne() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_step_one, container, false);

        email = (EditText) view.findViewById(R.id.reg_email);
        password = (EditText) view.findViewById(R.id.reg_password);
        confirmation = (EditText) view.findViewById(R.id.confirm_password);

        return view;
    }

    public BackendlessUser process(){
        BackendlessUser user = new BackendlessUser();
        if (email.getText().toString().equals("")){
            Toast.makeText(getContext(),getString(R.string.enter_email),Toast.LENGTH_SHORT).show();
            return null;
        }
        else if(password.getText().toString().equals("")){
            Toast.makeText(getContext(),getString(R.string.enter_pass),Toast.LENGTH_SHORT).show();
            return null;
        }
        else if(!password.getText().toString().equals(confirmation.getText().toString())){
            Toast.makeText(getContext(),getString(R.string.mismatch),Toast.LENGTH_SHORT).show();
            return null;
        }
        else{
            user.setEmail(email.getText().toString());
            user.setPassword(password.getText().toString());
            return user;
        }
    }

}
