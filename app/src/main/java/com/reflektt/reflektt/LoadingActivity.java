package com.reflektt.reflektt;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;
import com.backendless.persistence.local.UserTokenStorageFactory;
import com.kogitune.activity_transition.ActivityTransitionLauncher;
import com.reflektt.reflektt.RegisterSteps.RegisterActivity;
import com.reflektt.reflektt.Tables.Comments;
import com.reflektt.reflektt.Tables.Posts;
import com.reflektt.reflektt.Tables.Products;

import java.util.ArrayList;

public class LoadingActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;
    private ArrayList<String> permissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        //initialize Backendless
        Backendless.setUrl("https://api.backendless.com");
        Backendless.initApp(this, Constants.APP_ID, Constants.SECRET_KEY, Constants.VERSION);
        //map the database table to classes
        Backendless.Persistence.mapTableToClass("Comments", Comments.class);
        Backendless.Persistence.mapTableToClass("Posts", Posts.class);
        Backendless.Persistence.mapTableToClass("Products", Products.class);
        String userToken = UserTokenStorageFactory.instance().getStorage().get();
        getPermission(userToken);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private void getPermission(final String userToken) {
        String[] PERMISSIONS_REQUIRED = new String[]{};
        try {
            PERMISSIONS_REQUIRED = getPackageManager()
                    .getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS)
                    .requestedPermissions;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        for (final String permission : PERMISSIONS_REQUIRED) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(permission);
            }
        }
        if (!permissions.isEmpty()) {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setMessage("To use the app,You have to allow storage use and camera");
            builder.setTitle("Permissions");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // if the permission dialog appeared, then it's the first time to open the app
                    // So, no need to make loading of logged in user
                    String[] array = new String[permissions.size()];
                    ActivityCompat.requestPermissions(LoadingActivity.this, permissions.toArray(array), 1);
                }
            });
            builder.show();
        } else {
            loading(userToken);
        }
    }

    private void loading(String userToken) {
        if (userToken != null && !userToken.equals("")) {
            Backendless.UserService.isValidLogin(new AsyncCallback<Boolean>() {
                @Override
                public void handleResponse(Boolean response) {
                    BackendlessDataQuery query = new BackendlessDataQuery();
                    QueryOptions queryOptions = new QueryOptions();
                    queryOptions.addRelated("followings");
                    queryOptions.addRelated("followers");
                    queryOptions.addRelated("posts");
                    queryOptions.addRelated("favorite_items");
                    queryOptions.addRelated("favorite_items.rates1");
                    queryOptions.addRelated("favorite_items.rates2");
                    queryOptions.addRelated("favorite_items.rates3");
                    queryOptions.addRelated("favorite_items.rates4");
                    queryOptions.addRelated("favorite_items.rates5");
                    queryOptions.addRelated("favorite_items.comments");
                    queryOptions.addRelated("favorite_items.favorites");
                    query.setQueryOptions(queryOptions);
                    query.setWhereClause(String.format("objectId='%s'", Backendless.UserService.loggedInUser()));
                    Backendless.Persistence.of(BackendlessUser.class).find(query, new AsyncCallback<BackendlessCollection<BackendlessUser>>() {
                        @Override
                        public void handleResponse(BackendlessCollection<BackendlessUser> response) {
                            BackendlessUser user = response.getCurrentPage().get(0);
                            BackgroundService.getService().setCurrentUser(user);
                            Intent i = new Intent(LoadingActivity.this, BackgroundService.class);
                            i.setAction("null");
                            startService(i);
                            String username = (String) user.getProperty("username");
                            Intent intent;
                            if (username != null)
                                intent = new Intent(LoadingActivity.this, HomeActivity.class);
                            else {
                                intent = new Intent(LoadingActivity.this, RegisterActivity.class);
                                intent.putExtra("login", "social");
                            }
                            startActivity(intent);
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
                    if (fault.getCode().equals("3064"))
                        Backendless.UserService.logout(new AsyncCallback<Void>() {
                            @Override
                            public void handleResponse(Void response) {
                                ActivityTransitionLauncher.with(LoadingActivity.this).from(findViewById(R.id.splash)).launch(new Intent(LoadingActivity.this, LoginActivity.class));
                                finish();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {

                            }
                        });
                    Toast.makeText(LoadingActivity.this, getString(R.string.error), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            new CountDownTimer(2000, 1000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    ActivityTransitionLauncher.with(LoadingActivity.this).from(findViewById(R.id.splash)).launch(new Intent(LoadingActivity.this, LoginActivity.class));
                    finish();
                }
            }.start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            for (int permission : grantResults) {
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(LoadingActivity.this, "Permission Denied",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
            loading(UserTokenStorageFactory.instance().getStorage().get());
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            finish();
        }
    }
}
