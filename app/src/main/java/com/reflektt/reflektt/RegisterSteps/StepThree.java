package com.reflektt.reflektt.RegisterSteps;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.reflektt.reflektt.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class StepThree extends Fragment {
    private EditText username;
    Button checkButton;
    private TextView status;
    private boolean isChecked = false;
    private boolean isAvailable = false;
    private ProgressBar prog;

    public StepThree() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_step_three, container, false);

        username = (EditText) v.findViewById(R.id.reg_username);
        checkButton = (Button) v.findViewById(R.id.check_username);
        status = (TextView) v.findViewById(R.id.check_status);
        prog = (ProgressBar) v.findViewById(R.id.prog);

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String whereClause = "username = '" + username.getText().toString() + "'";
                BackendlessDataQuery dataQuery = new BackendlessDataQuery();
                dataQuery.setWhereClause(whereClause);
                prog.setVisibility(View.VISIBLE);
                Backendless.Persistence.of(BackendlessUser.class).find(dataQuery, new AsyncCallback<BackendlessCollection<BackendlessUser>>() {

                    @Override
                    public void handleResponse(BackendlessCollection<BackendlessUser> result) {
                         if(result.getData().size() > 0) {
                            status.setText(R.string.username_taken);
                            status.setTextColor(Color.RED);
                            prog.setVisibility(View.GONE);
                            isChecked = true;
                            isAvailable = false;
                            return;
                        }
                        status.setText(R.string.username_valid);
                        status.setTextColor(Color.GREEN);
                        prog.setVisibility(View.GONE);
                        isChecked = true;
                        isAvailable = true;
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(getContext(),getString(R.string.error),Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
        return v;
    }

    public BackendlessUser process(BackendlessUser user) {
        if (isAvailable && isChecked) {
            user.setProperty("username", username.getText().toString());
            return user;
        } else {
            if (!isAvailable)
                Toast.makeText(getContext(), getString(R.string.existing_username), Toast.LENGTH_SHORT).show();
            else if (!isChecked)
                Toast.makeText(getContext(), getString(R.string.not_checked), Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
