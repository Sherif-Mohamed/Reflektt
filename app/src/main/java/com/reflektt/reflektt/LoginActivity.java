package com.reflektt.reflektt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.async.callback.BackendlessCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.kogitune.activity_transition.ActivityTransition;
import com.reflektt.reflektt.RegisterSteps.RegisterActivity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.logbtn)
    Button login;
    @BindView(R.id.loginFacebookButton)
    ImageView fbLogin;
    @BindView(R.id.loginGoogleButton)
    ImageView gplusLogin;
    @BindView(R.id.pass)
    EditText password;
    @BindView(R.id.mail)
    EditText email;
    @BindView(R.id.reg_txt)
    TextView register;
    private ProgressDialog prog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //animation for logo and login form
        ActivityTransition.with(getIntent()).to(findViewById(R.id.splashLogo)).start(savedInstanceState);
        new CountDownTimer(800, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Fade fade = new Fade();
                fade.setDuration(500);
                TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.activity_login), fade);
                findViewById(R.id.contain).setVisibility(View.VISIBLE);
            }
        }.start();
        ButterKnife.bind(this);
        //gplusLogin.setSize(SignInButton.SIZE_WIDE);
        //normal login
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String emailin = email.getText().toString();
                String passwordin = password.getText().toString();
                prog = new ProgressDialog(LoginActivity.this);

                prog.setMessage(getString(R.string.logging_in));
                prog.show();
                Backendless.UserService.login(emailin, passwordin, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        BackendlessDataQuery query = new BackendlessDataQuery();
                        QueryOptions queryOptions = new QueryOptions();
                        queryOptions.addRelated("followings");
                        queryOptions.addRelated("followers");
                        queryOptions.addRelated("posts");
                        queryOptions.addRelated("favorite_items");
                        query.setQueryOptions(queryOptions);
                        query.setWhereClause(String.format("objectId='%s'", response.getObjectId()));
                        Backendless.Persistence.of(BackendlessUser.class).find(query, new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
                            @Override
                            public void handleResponse(BackendlessCollection<BackendlessUser> response) {
                                BackendlessUser user =response.getCurrentPage().get(0);
                                BackgroundService.getService().setCurrentUser(user);
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                startActivity(intent);
                                prog.dismiss();
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                finish();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {

                            }
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        prog.dismiss();
                        Toast.makeText(LoginActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                    }
                }, true);
            }
        });
        //facebook login
        fbLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> facebookFieldsMapping = new HashMap<>();
                facebookFieldsMapping.put("name", "name");
                facebookFieldsMapping.put("email", "email");

                List<String> facebookPermissions = new LinkedList<>();
                facebookPermissions.add("email");
                Backendless.UserService.loginWithFacebook(LoginActivity.this, null, facebookFieldsMapping, facebookPermissions, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser response) {
                        Intent intent;
                        BackgroundService.getService().setCurrentUser(response);
                        String username = (String) response.getProperty("username");
                        if (username == null) {
                            intent = new Intent(LoginActivity.this, RegisterActivity.class);
                            intent.putExtra("login", "social");
                            startActivity(intent);
                            finish();
                        } else {
                            BackendlessDataQuery query = new BackendlessDataQuery();
                            QueryOptions queryOptions = new QueryOptions();
                            queryOptions.addRelated("followings");
                            queryOptions.addRelated("followers");
                            queryOptions.addRelated("posts");
                            queryOptions.addRelated("favorite_items");
                            query.setQueryOptions(queryOptions);
                            query.setWhereClause(String.format("objectId='%s'", response.getObjectId()));
                            Backendless.Persistence.of(BackendlessUser.class).find(query, new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
                                @Override
                                public void handleResponse(BackendlessCollection<BackendlessUser> response) {
                                    BackendlessUser user =response.getCurrentPage().get(0);
                                    BackgroundService.getService().setCurrentUser(user);
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    finish();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {

                                }
                            });
                        }

                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        fault.getMessage();
                    }
                },true);
            }
        });
        //google plus login
        gplusLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> googleFieldsMapping = new HashMap<>();
                googleFieldsMapping.put("email", "email");

                List<String> googlePermissions = new LinkedList<>();

                Backendless.UserService.loginWithGooglePlus(LoginActivity.this, null, googleFieldsMapping, googlePermissions, new BackendlessCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser loggedInUser) {
                        // user logged in successfully
                        Intent intent;
                        BackgroundService.getService().setCurrentUser(loggedInUser);
                        String username = (String) loggedInUser.getProperty("username");
                        if (username == null) {
                            intent = new Intent(LoginActivity.this, RegisterActivity.class);
                            intent.putExtra("login", "social");
                            startActivity(intent);
                            finish();
                        } else{
                            BackendlessDataQuery query = new BackendlessDataQuery();
                            QueryOptions queryOptions = new QueryOptions();
                            queryOptions.addRelated("followings");
                            queryOptions.addRelated("followers");
                            queryOptions.addRelated("posts");
                            queryOptions.addRelated("favorite_items");
                            query.setQueryOptions(queryOptions);
                            query.setWhereClause(String.format("objectId='%s'", loggedInUser.getObjectId()));
                            Backendless.Persistence.of(BackendlessUser.class).find(query, new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
                                @Override
                                public void handleResponse(BackendlessCollection<BackendlessUser> response) {
                                    BackendlessUser user =response.getCurrentPage().get(0);
                                    BackgroundService.getService().setCurrentUser(user);
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                    finish();
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    fault.getMessage();
                                }
                            });
                        }

                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        // failed to log in
                        Toast.makeText(LoginActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                    }
                }, true);
            }
        });
        // register using email & password
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                intent.putExtra("login", "normal");
                startActivity(intent);
                finish();
            }
        });
    }

}
