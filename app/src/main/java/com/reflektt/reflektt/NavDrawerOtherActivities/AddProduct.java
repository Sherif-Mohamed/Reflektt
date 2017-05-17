package com.reflektt.reflektt.NavDrawerOtherActivities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.mikelau.croperino.Croperino;
import com.reflektt.reflektt.mikelau.croperino.CroperinoConfig;
import com.reflektt.reflektt.mikelau.croperino.CroperinoFileUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddProduct extends AppCompatActivity {
    @BindView(R.id.gallery_view)View galleryView;
    @BindView(R.id.camera_view)View cameraView;
    @BindView(R.id.product_pic)ImageView productPicture;
    @BindView(R.id.product_name)EditText productName;
    @BindView(R.id.product_brand)EditText productBrand;
    @BindView(R.id.submit)Button submit;
    @BindView(R.id.selectViews)View selectViews;

    Bitmap bitmap;
    private boolean isSelected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //set settings of choosing picture
        new CroperinoConfig("IMG_" + System.currentTimeMillis() + ".jpg", "/Reflektt/Pictures", "/sdcard/Reflektt/Pictures");
        CroperinoFileUtil.setupDirectory(this);
        galleryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Croperino.prepareGallery(), CroperinoConfig.REQUEST_PICK_FILE);
            }
        });
        cameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Croperino.prepareCamera(AddProduct.this), CroperinoConfig.REQUEST_TAKE_PHOTO);
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isSelected){
                    Map<String,String> input = new HashMap<>();
                    final String name = productName.getText().toString();
                    String brand = productBrand.getText().toString();
                    if(name.isEmpty()){
                        Toast.makeText(AddProduct.this,"Product name is empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    else if(brand.isEmpty()){
                        Toast.makeText(AddProduct.this,"Product brand is empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    input.put("name",name);
                    input.put("companyName",brand);
                    Backendless.Persistence.of("EntryQuery").save(input, new AsyncCallback<Map>() {
                        @Override
                        public void handleResponse(Map response) {
                            Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.JPEG, 100,
                                        name + ".jpg", "new_products", new AsyncCallback<BackendlessFile>() {
                                            @Override
                                            public void handleResponse(BackendlessFile response) {

                                            }

                                            @Override
                                            public void handleFault(BackendlessFault fault) {

                                            }
                                        });
                            Toast.makeText(AddProduct.this,"Your query is added and it is being revised now",Toast.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(AddProduct.this,"There is some error, Please Try later",Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                    NavUtils.navigateUpFromSameTask(AddProduct.this);
                    finish();
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CroperinoConfig.REQUEST_PICK_FILE: {
                    CroperinoFileUtil.newGalleryFile(data, this);
                    Uri i = Uri.fromFile(CroperinoFileUtil.getmFileTemp());
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), i);
                        productPicture.setImageBitmap(bitmap);
                        selectViews.setVisibility(View.GONE);
                        productName.setVisibility(View.VISIBLE);
                        productBrand.setVisibility(View.VISIBLE);
                        submit.setVisibility(View.VISIBLE);
                        isSelected = true;
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case CroperinoConfig.REQUEST_TAKE_PHOTO: {
                    Uri i = Uri.fromFile(CroperinoFileUtil.getmFileTemp());
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), i);
                        productPicture.setImageBitmap(bitmap);
                        selectViews.setVisibility(View.GONE);
                        productName.setVisibility(View.VISIBLE);
                        productBrand.setVisibility(View.VISIBLE);
                        submit.setVisibility(View.VISIBLE);
                        isSelected = true;
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
