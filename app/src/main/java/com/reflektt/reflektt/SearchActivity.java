package com.reflektt.reflektt;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
import com.reflektt.reflektt.Fragments.ProductsFragment;
import com.reflektt.reflektt.Fragments.UsersFragment;
import com.reflektt.reflektt.Tables.Products;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {
    @BindView(R.id.search_bar)EditText searchView;
    @BindView(R.id.queries)View queries;
    @BindView(R.id.fragment_frame)View frame;
    @BindView(R.id.profiles_query)TextView profilesQuery;
    @BindView(R.id.products_query)TextView productsQuery;
    @BindView(R.id.search_progress)ProgressBar searchProgress;
    @BindView(R.id.empty_text)TextView empty;
    @BindView(R.id.tint)View background;
    boolean requestFocus = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        //if the search bar has focus, show the queries
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && requestFocus){
                    queries.setVisibility(View.VISIBLE);
                    background.setAlpha(0.5f);
                }
                else {
                    queries.setVisibility(View.GONE);
                    background.setAlpha(0);
                }

            }
        });
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                profilesQuery.setText(String.format("%s %s",getResources().getString(R.string.profileQuery),s));
                productsQuery.setText(String.format("%s %s",getResources().getString(R.string.productsQuery),s));
            }
        });
        profilesQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFocus = false;
                searchView.clearFocus();
                searchProgress.setVisibility(View.VISIBLE);
                String clause = "name LIKE '%"+searchView.getText().toString()+"%' " +
                        "or email LIKE '%"+searchView.getText().toString()+"%'";

                Backendless.Persistence.of(BackendlessUser.class).find(new BackendlessDataQuery(clause), new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
                    @Override
                    public void handleResponse(BackendlessCollection<BackendlessUser> response) {
                        searchProgress.setVisibility(View.GONE);
                        if (response.getData().isEmpty()) empty.setVisibility(View.VISIBLE);
                        else{
                            UsersFragment fragment = new UsersFragment();
                            fragment.users = response.getData().toArray(new BackendlessUser[0]);
                            Bundle b = new Bundle();
                            b.putInt("callerId", R.id.fragment_frame);
                            fragment.setArguments(b);
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.fragment_frame, fragment);
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            ft.commit();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        searchProgress.setVisibility(View.GONE);
                        empty.setVisibility(View.VISIBLE);
                        Toast.makeText(SearchActivity.this,R.string.error,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        productsQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFocus = false;
                searchView.clearFocus();
                searchProgress.setVisibility(View.VISIBLE);
                String clause = "name LIKE '%"+searchView.getText().toString()+"%' " +
                        "or companyName LIKE '%"+searchView.getText().toString()+"%' "+
                        "or category LIKE '%"+searchView.getText().toString()+"%' ";

                Backendless.Persistence.of(Products.class).find(new BackendlessDataQuery(clause), new AsyncCallback<BackendlessCollection<Products>>() {
                    @Override
                    public void handleResponse(BackendlessCollection<Products> response) {
                        searchProgress.setVisibility(View.GONE);
                        if (response.getData().isEmpty()) empty.setVisibility(View.VISIBLE);
                        else{
                            ProductsFragment fragment = new ProductsFragment();
                            fragment.setData(response.getData());
                            Bundle b = new Bundle();
                            b.putInt("callerId", R.id.fragment_frame);
                            fragment.setArguments(b);
                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.fragment_frame, fragment);
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            ft.commit();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        searchProgress.setVisibility(View.GONE);
                        empty.setVisibility(View.VISIBLE);
                        Toast.makeText(SearchActivity.this,R.string.error,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestFocus = true;
                searchView.setFocusableInTouchMode(true);
                searchView.clearFocus();
                searchView.requestFocus();
            }
        });
    }
}
