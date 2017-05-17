package com.reflektt.reflektt.NavDrawerOtherActivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.reflektt.reflektt.BackgroundService;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.mikelau.croperino.Croperino;
import com.reflektt.reflektt.mikelau.croperino.CroperinoConfig;
import com.reflektt.reflektt.mikelau.croperino.CroperinoFileUtil;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE;

public class Settings extends AppCompatActivity {
    @BindView(R.id.settings_recycler)
    RecyclerView recyclerView;
    @BindView(R.id.app_bar) Toolbar toolbar;
    private String[] listItems ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        listItems =getResources().getStringArray(R.array.settings);
        View v = toolbar.findViewById(R.id.logo);
        v.setVisibility(View.INVISIBLE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(new SimpleAdapter());
    }


    class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleVH> {
        LayoutInflater inflater;

        SimpleAdapter() {
            inflater = (LayoutInflater) Settings.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public SimpleVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.settings_item, parent, false);
            return new SimpleVH(view);
        }

        @Override
        public void onBindViewHolder(SimpleVH holder, int position) {
            holder.query.setText(listItems[position]);
            if (listItems[position].equals(getResources().getString(R.string.name)) ||listItems[position].equals(getResources().getString(R.string.biography))) {
                holder.property.setText((CharSequence) BackgroundService.getService().getUser().getProperty(listItems[position].toLowerCase()));
            } else
                holder.property.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return listItems.length;
        }

        class SimpleVH extends RecyclerView.ViewHolder {
            @BindView(R.id.query)
            TextView query;
            @BindView(R.id.property)
            TextView property;

            SimpleVH(final View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (getAdapterPosition()) {
                            case 0: {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Settings.this);
                                LayoutInflater inflater = Settings.this.getLayoutInflater();
                                final View dialogView = inflater.inflate(R.layout.alert_edit, null);
                                dialogBuilder.setView(dialogView);
                                final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);
                                dialogBuilder.setTitle(getResources().getString(R.string.name));
                                dialogBuilder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        final String result = edt.getText().toString();
                                        BackendlessUser user = BackgroundService.getService().getUser();
                                        user.setProperty("name", result);
                                        final ProgressDialog p = new ProgressDialog(Settings.this);
                                        p.setMessage(getResources().getString(R.string.please_wait));
                                        p.show();
                                        Backendless.Persistence.of(BackendlessUser.class).save(user, new AsyncCallback<BackendlessUser>() {
                                            @Override
                                            public void handleResponse(BackendlessUser response) {
                                                p.dismiss();
                                                BackgroundService.getService().setCurrentUser(response);
                                                property.setText(result);
                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {
                                                p.dismiss();
                                                Toast.makeText(Settings.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                });
                                dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                });
                                AlertDialog b = dialogBuilder.create();
                                b.show();
                                break;
                            }
                            case 1: {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Settings.this);
                                LayoutInflater inflater = Settings.this.getLayoutInflater();
                                final View dialogView = inflater.inflate(R.layout.alert_edit, null);
                                dialogBuilder.setView(dialogView);
                                final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);
                                dialogBuilder.setTitle(getResources().getString(R.string.password));
                                dialogBuilder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        final String result= edt.getText().toString();
                                        BackendlessUser user = BackgroundService.getService().getUser();
                                        user.setProperty("password", result);

                                        final ProgressDialog p = new ProgressDialog(Settings.this);
                                        p.setMessage(getResources().getString(R.string.please_wait));
                                        p.show();
                                        Backendless.Persistence.of(BackendlessUser.class).save(user, new AsyncCallback<BackendlessUser>() {
                                            @Override
                                            public void handleResponse(BackendlessUser response) {
                                                p.dismiss();
                                                BackgroundService.getService().setCurrentUser(response);
                                                property.setText(result);
                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {
                                                p.dismiss();
                                                Toast.makeText(Settings.this, "getResources().getString(R.string.error)", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                });
                                dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                });
                                AlertDialog b = dialogBuilder.create();
                                b.show();
                                break;
                            }
                            case 2: {
                                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Settings.this);
                                LayoutInflater inflater = Settings.this.getLayoutInflater();
                                final View dialogView = inflater.inflate(R.layout.alert_edit, null);
                                dialogBuilder.setView(dialogView);
                                final EditText edt = (EditText) dialogView.findViewById(R.id.edit1);
                                edt.setInputType(TYPE_TEXT_FLAG_MULTI_LINE);
                                edt.setLines(3);
                                dialogBuilder.setTitle(getResources().getString(R.string.biography));
                                dialogBuilder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        final String result = edt.getText().toString();
                                        BackendlessUser user = BackgroundService.getService().getUser();
                                        user.setProperty("biography", result);
                                        final ProgressDialog p = new ProgressDialog(Settings.this);
                                        p.setMessage(getResources().getString(R.string.please_wait));
                                        p.show();
                                        Backendless.Persistence.of(BackendlessUser.class).save(user, new AsyncCallback<BackendlessUser>() {
                                            @Override
                                            public void handleResponse(BackendlessUser response) {
                                                p.dismiss();
                                                BackgroundService.getService().setCurrentUser(response);
                                                property.setText(result);
                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {
                                                p.dismiss();
                                                Toast.makeText(Settings.this, "getResources().getString(R.string.error)", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                });
                                dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                });
                                AlertDialog b = dialogBuilder.create();
                                b.show();
                                break;
                            }
                            case 3: {
                                new CroperinoConfig("IMG_" + System.currentTimeMillis() + ".jpg", "/Reflektt/Pictures", "/sdcard/Reflektt/Pictures");
                                CroperinoFileUtil.setupDirectory(Settings.this);
                                Croperino.prepareChooser(Settings.this,"Choose Photo",R.color.colorPrimary);
                                break;
                            }
                            case 4:{
                                final CharSequence[] items = getResources().getStringArray(R.array.privacy_options);
                                // Creating and Building the Dialog
                                final AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                                builder.setTitle(getResources().getString(R.string.privacy));
                                final AlertDialog[] levelDialog = new AlertDialog[1];
                                boolean selection = (boolean) BackgroundService.getService().getUser().getProperty("privacy");
                                int x;
                                if (selection) x = 1;
                                else x = 0;
                                builder.setSingleChoiceItems(items, x, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int item) {
                                        switch(item) {
                                            case 0: {
                                                BackendlessUser user = BackgroundService.getService().getUser();
                                                user.setProperty("privacy", false);
                                                Backendless.Persistence.of(BackendlessUser.class).save(user, new AsyncCallback<BackendlessUser>() {
                                                    @Override
                                                    public void handleResponse(BackendlessUser response) {
                                                        BackgroundService.getService().setCurrentUser(response);
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {

                                                    }
                                                });
                                                break;
                                            }
                                            case 1:
                                                BackendlessUser user = BackgroundService.getService().getUser();
                                                user.setProperty("privacy", true);
                                                Backendless.Persistence.of(BackendlessUser.class).save(user, new AsyncCallback<BackendlessUser>() {
                                                    @Override
                                                    public void handleResponse(BackendlessUser response) {
                                                        BackgroundService.getService().setCurrentUser(response);
                                                    }

                                                    @Override
                                                    public void handleFault(BackendlessFault fault) {

                                                    }
                                                });
                                                break;
                                        }
                                        levelDialog[0].dismiss();
                                    }
                                });
                                levelDialog[0] = builder.create();
                                levelDialog[0].show();

                                break;
                            }
                            default:
                                new AlertDialog.Builder(Settings.this)
                                        .setMessage(R.string.about)
                                        .setCancelable(false)
                                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                            }
                                        })
                                        .show();
                                break;
                        }
                    }
                });
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CroperinoConfig.REQUEST_TAKE_PHOTO: {
                    Croperino.runCropImage(CroperinoFileUtil.getmFileTemp(), this, true, 1, 1, R.color.colorPrimary, 0);
                    break;
                }
                case CroperinoConfig.REQUEST_PICK_FILE: {
                    CroperinoFileUtil.newGalleryFile(data, this);
                    Croperino.runCropImage(CroperinoFileUtil.getmFileTemp(), this, true, 1, 1, R.color.colorPrimary, 0);
                    break;
                }
                case CroperinoConfig.REQUEST_CROP_PHOTO: {
                    Uri i = (Uri) data.getExtras().get("saveURI");
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), i);
                        final ProgressDialog p = new ProgressDialog(this);
                        p.setMessage(getResources().getString(R.string.please_wait));
                        p.show();
                        Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.JPEG, 100,
                                BackgroundService.getService().getUser().getProperty("username") + ".jpg", "profile_pictures",true, new AsyncCallback<BackendlessFile>() {
                                    @Override
                                    public void handleResponse(BackendlessFile response) {
                                        p.dismiss();
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {

                                    }
                                });
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Set the Image in ImageView
                }
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
        finish();
    }

}
