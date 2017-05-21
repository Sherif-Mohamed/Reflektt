package com.reflektt.reflektt.HomeFragments.AddPost;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.cameraview.CameraView;
import com.reflektt.reflektt.BackgroundService;
import com.reflektt.reflektt.R;
import com.reflektt.reflektt.mikelau.croperino.Croperino;
import com.reflektt.reflektt.mikelau.croperino.CroperinoConfig;
import com.reflektt.reflektt.mikelau.croperino.CroperinoFileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;


public class AddPost_Phase1 extends Fragment {
    @BindView(R.id.camera_view)
    CameraView cameraView;
    @BindView(R.id.flip_camera)
    ImageView flipCamera;
    @BindView(R.id.select_photo)
    ImageView selectPhoto;
    @BindView(R.id.take_photo)
    FloatingActionButton takePhoto;
    private Handler fileSaveHandler;

    private Handler getBackgroundHandler() {
        if (fileSaveHandler == null) {
            HandlerThread thread = new HandlerThread("background");
            thread.start();
            fileSaveHandler = new Handler(thread.getLooper());
        }
        return fileSaveHandler;
    }

    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            //Log.d("TAG", "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            //Log.d("TAG", "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            getBackgroundHandler().post(new Runnable() {
                @Override
                public void run() {
                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Reflektt/Pictures/",
                            BackgroundService.getService().getUser().getProperty("username")+"_" + System.currentTimeMillis() + ".jpg");
                    OutputStream os = null;
                    try {
                        os = new FileOutputStream(file);
                        os.write(data);
                        os.close();

                        runCropImage(file, getActivity(), true, 1, 1, R.color.colorPrimary, 0);
                    } catch (IOException e) {
                        //ignore
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                // Ignore
                            }
                        }
                    }
                }
            });
        }

    };

    public AddPost_Phase1() {
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
        View v = inflater.inflate(R.layout.fragment_add_post, container, false);
        ButterKnife.bind(this, v);
        new CroperinoConfig(BackgroundService.getService().getUser().getProperty("username")+ "_" + System.currentTimeMillis() + ".jpg"
                , "/Reflektt/Pictures/",
                Environment.getExternalStorageDirectory().getAbsolutePath());

        cameraView.addCallback(mCallback);

        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.takePicture();
            }
        });

        flipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.setFacing(cameraView.getFacing() == CameraView.FACING_FRONT ?
                        CameraView.FACING_BACK : CameraView.FACING_FRONT);
            }
        });

        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(Croperino.prepareGallery(), CroperinoConfig.REQUEST_PICK_FILE);
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraView.start();
        }
    }

    @Override
    public void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fileSaveHandler != null) {
            fileSaveHandler.getLooper().quitSafely();
            fileSaveHandler = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CroperinoConfig.REQUEST_PICK_FILE: {
                    CroperinoFileUtil.newGalleryFile(data, getActivity());
                    runCropImage(CroperinoFileUtil.getmFileTemp(), getActivity(), true, 1, 1, R.color.colorPrimary, 0);
                    break;
                }
                case CroperinoConfig.REQUEST_CROP_PHOTO: {
                    Uri uri = (Uri) data.getExtras().get("saveURI");
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    AddPost_Phase2 fragment = new AddPost_Phase2();
                    Bundle b = new Bundle();
                    b.putParcelable("pictureUri",uri);
                    fragment.setArguments(b);
                    ft.replace(R.id.fragment_frame,fragment);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.commit();
                }
                default:
                    break;
            }
        }
    }

    public void runCropImage(File file, Context ctx, boolean isScalable, int aspectX, int aspectY, int color, int bgColor) {
        Intent intent = new Intent(ctx, com.reflektt.reflektt.mikelau.croperino.CropImage.class);
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.IMAGE_PATH, file.getPath());
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.SCALE, isScalable);
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.ASPECT_X, aspectX);
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.ASPECT_Y, aspectY);
        intent.putExtra("color", color);
        intent.putExtra("bgColor", bgColor);
        startActivityForResult(intent, com.reflektt.reflektt.mikelau.croperino.CroperinoConfig.REQUEST_CROP_PHOTO);
    }

}
