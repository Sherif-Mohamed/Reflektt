package com.reflektt.reflektt.RegisterSteps;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.mikelau.croperino.Croperino;
import com.reflektt.reflektt.mikelau.croperino.CroperinoConfig;
import com.reflektt.reflektt.mikelau.croperino.CroperinoFileUtil;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class StepFour extends Fragment {
    @BindView(R.id.preview_pic)
    CircularImageView previewPicture;
    @BindView(R.id.gallery_view)
    View gallerySelect;
    @BindView(R.id.camera_view)
    View cameraSelect;
    Bitmap bitmap;
    boolean isSelected = false;

    public StepFour() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_step_four, container, false);
        ButterKnife.bind(this, v);
        new CroperinoConfig("IMG_" + System.currentTimeMillis() + ".jpg", "/Reflektt/Pictures", Environment.getExternalStorageDirectory().getAbsolutePath());
        CroperinoFileUtil.setupDirectory(getContext());
        //Initialize on every usage
        gallerySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(Croperino.prepareGallery(), CroperinoConfig.REQUEST_PICK_FILE);
            }
        });
        cameraSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Croperino.prepareCamera(getActivity());
                startActivityForResult(intent, CroperinoConfig.REQUEST_TAKE_PHOTO);
            }
        });
        return v;
    }

    public BackendlessUser process(final BackendlessUser user) {
        if (isSelected) {
            Backendless.Files.Android.upload(bitmap, Bitmap.CompressFormat.JPEG, 100,
                    user.getProperty("username") + ".jpg", "profile_pictures", new AsyncCallback<BackendlessFile>() {
                        @Override
                        public void handleResponse(BackendlessFile response) {
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {

                        }
                    });

        } else {
            Backendless.Files.Android.upload(BitmapFactory.decodeResource(getResources(), R.drawable.profile_default,null),
                    Bitmap.CompressFormat.JPEG, 100, user.getProperty("username") + ".jpg", "profile_pictures"
                    , new AsyncCallback<BackendlessFile>() {
                        @Override
                        public void handleResponse(BackendlessFile response) {
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {

                        }
                    });
        }
        return user;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CroperinoConfig.REQUEST_TAKE_PHOTO: {
                    runCropImage(CroperinoFileUtil.getmFileTemp(), getContext(), true, 1, 1, R.color.colorPrimary, 0);
                    break;
                }
                case CroperinoConfig.REQUEST_PICK_FILE: {
                    CroperinoFileUtil.newGalleryFile(data, getActivity());
                    runCropImage(CroperinoFileUtil.getmFileTemp(), getContext(), true, 1, 1, R.color.colorPrimary, 0);
                    break;
                }
                case CroperinoConfig.REQUEST_CROP_PHOTO: {
                    Uri i = (Uri) data.getExtras().get("saveURI");
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), i);
                        previewPicture.setImageBitmap(bitmap);
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
    public void runCropImage(File file, Context ctx, boolean isScalable, int aspectX, int aspectY, int color, int bgColor) {
        Intent intent = new Intent(ctx,com.reflektt.reflektt.mikelau.croperino.CropImage.class);
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.IMAGE_PATH, file.getPath());
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.SCALE, isScalable);
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.ASPECT_X, aspectX);
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.ASPECT_Y, aspectY);
        intent.putExtra("color", color);
        intent.putExtra("bgColor", bgColor);
        startActivityForResult(intent, com.reflektt.reflektt.mikelau.croperino.CroperinoConfig.REQUEST_CROP_PHOTO);
    }
}
