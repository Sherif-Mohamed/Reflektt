package com.reflektt.reflektt.mikelau.croperino;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.mikelau.magictoast.MagicToast;
import com.reflektt.reflektt.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by Mike on 9/15/2016.
 */
public class Croperino {
    private static String TAG = Croperino.class.getSimpleName();

    public static void runCropImage(File file, Activity ctx, boolean isScalable, int aspectX, int aspectY, int color, int bgColor) {
        Intent intent = new Intent(ctx,com.reflektt.reflektt.mikelau.croperino.CropImage.class);
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.IMAGE_PATH, file.getPath());
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.SCALE, isScalable);
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.ASPECT_X, aspectX);
        intent.putExtra(com.reflektt.reflektt.mikelau.croperino.CropImage.ASPECT_Y, aspectY);
        intent.putExtra("color", color);
        intent.putExtra("bgColor", bgColor);
        ctx.startActivityForResult(intent, com.reflektt.reflektt.mikelau.croperino.CroperinoConfig.REQUEST_CROP_PHOTO);
    }

    public static void prepareChooser(final Activity ctx, String message, int color) {
        CameraDialog.getConfirmDialog(ctx, ctx.getResources().getString(R.string.app_name),
                message,
                "CAMERA",
                "GALLERY",
                "CLOSE",
                color,
                true,
                new AlertInterface.WithNeutral() {
                    @Override
                    public void PositiveMethod(final DialogInterface dialog, final int id) {
                        if (com.reflektt.reflektt.mikelau.croperino.CroperinoFileUtil.verifyCameraPermissions(ctx)) {
                            ctx.startActivityForResult(prepareCamera(ctx), CroperinoConfig.REQUEST_TAKE_PHOTO);
                        }
                    }

                    @Override
                    public void NeutralMethod(final DialogInterface dialog, final int id) {
                        if (com.reflektt.reflektt.mikelau.croperino.CroperinoFileUtil.verifyStoragePermissions(ctx)) {
                            ctx.startActivityForResult(prepareGallery(),CroperinoConfig.REQUEST_PICK_FILE);
                        }
                    }

                    @Override
                    public void NegativeMethod(final DialogInterface dialog, final int id) {

                    }
                });
    }

    public static Intent prepareCamera(Activity ctx) {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri mImageCaptureUri;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                if (Uri.fromFile(com.reflektt.reflektt.mikelau.croperino.CroperinoFileUtil.newCameraFile()) != null && !Uri.EMPTY.equals(Uri.fromFile(com.reflektt.reflektt.mikelau.croperino.CroperinoFileUtil.newCameraFile()))) {
                    mImageCaptureUri = Uri.fromFile(com.reflektt.reflektt.mikelau.croperino.CroperinoFileUtil.newCameraFile());
                } else {
                    mImageCaptureUri = FileProvider.getUriForFile(ctx,
                            ctx.getApplicationContext().getPackageName() + ".provider",
                            com.reflektt.reflektt.mikelau.croperino.CroperinoFileUtil.newCameraFile());
                }
            } else {
                mImageCaptureUri = com.reflektt.reflektt.mikelau.croperino.InternalStorageContentProvider.CONTENT_URI;
            }
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            return intent;

        } catch (Exception e) {
            if (e instanceof ActivityNotFoundException) {
                MagicToast.showError(ctx, "Activity not found.");
            } else if (e instanceof IOException) {
                MagicToast.showError(ctx, "Image file captured not found.");
            }else {
                MagicToast.showError(ctx, "Camera access failed.");
            }
           // Log.e(TAG, "Failed to prepare camera: ", e);
            return null;
        }
    }

    public static Intent prepareGallery() {
        return new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    }
}
