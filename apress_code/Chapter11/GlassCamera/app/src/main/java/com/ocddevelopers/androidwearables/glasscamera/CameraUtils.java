package com.ocddevelopers.androidwearables.glasscamera;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Contains utility methods for accesing a camera and taking pictures.
 */
public class CameraUtils {
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public static File getOutputPictureFile() {
        return getOutputMediaFile("IMG", "jpg");
    }

    public static File getOutputTimelapseFile() {
        return getOutputMediaFile("TLAPSE", "mp4");
    }

    // derived from http://developer.android.com/guide/topics/media/camera.html
    private static File getOutputMediaFile(String prefix, String extension){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "AndroidWearables");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // as opposed to getExternalFilesDir

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("AndroidWearables", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                prefix + "_" + timeStamp + "." + extension);


        return mediaFile;
    }

    public static void scanFile(Context context, String filename) {
        context.getApplicationContext().sendBroadcast(
                new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE",
                        Uri.fromFile(new File(filename))));
    }
}
