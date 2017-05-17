package com.reflektt.reflektt.mikelau.croperino;

import android.app.ProgressDialog;
import android.os.Handler;
/**
 * Created by Mike on 9/14/2016.
 */
public class BackgroundJob extends com.reflektt.reflektt.mikelau.croperino.MonitoredActivity.LifeCycleAdapter implements Runnable {

    private final com.reflektt.reflektt.mikelau.croperino.MonitoredActivity mActivity;
    private final ProgressDialog mDialog;
    private final Runnable          mJob;
    private final Handler mHandler;
    private final Runnable mCleanupRunner = new Runnable() {
        public void run() {

            mActivity.removeLifeCycleListener(BackgroundJob.this);
            if (mDialog.getWindow() != null) mDialog.dismiss();
        }
    };

    public BackgroundJob(com.reflektt.reflektt.mikelau.croperino.MonitoredActivity activity, Runnable job,
                         ProgressDialog dialog, Handler handler) {

        mActivity = activity;
        mDialog = dialog;
        mJob = job;
        mActivity.addLifeCycleListener(this);
        mHandler = handler;
    }

    public void run() {
        try {
            mJob.run();
        } finally {
            mHandler.post(mCleanupRunner);
        }
    }


    @Override
    public void onActivityDestroyed(com.reflektt.reflektt.mikelau.croperino.MonitoredActivity activity) {
        mCleanupRunner.run();
        mHandler.removeCallbacks(mCleanupRunner);
    }

    @Override
    public void onActivityStopped(com.reflektt.reflektt.mikelau.croperino.MonitoredActivity activity) {

        mDialog.hide();
    }

    @Override
    public void onActivityStarted(com.reflektt.reflektt.mikelau.croperino.MonitoredActivity activity) {

        mDialog.show();
    }
}
