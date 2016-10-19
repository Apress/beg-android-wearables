package com.ocddevelopers.androidwearables.glasscamera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.glass.content.Intents;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.Slider;

import java.io.File;
import java.lang.ref.WeakReference;


/**
 * Shows how to take pictures and record videos with the native camera Glassware.
 */
public class CameraIntentActivity extends Activity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 2;
    private ImageView mImage;
    private CardBuilder mCardBuilder;
    private AudioManager mAudioManager;
    private boolean mCapturingMedia;
    private Slider mSlider;
    private Slider.Indeterminate mIndeterminate;
    private FileObserver mObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        View content = getLayoutInflater().inflate(R.layout.activity_cameraintent, null);

        ViewGroup container = (ViewGroup) content.findViewById(R.id.container);
        mImage = (ImageView) content.findViewById(R.id.image);

        TuggableView tuggableView = new TuggableView(this, content);
        setContentView(tuggableView);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        mCardBuilder = new CardBuilder(this, CardBuilder.Layout.TEXT)
                .setText("Tap to open the menu");

        container.addView(mCardBuilder.getView());

        mImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mSlider = Slider.from(tuggableView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            mAudioManager.playSoundEffect(Sounds.TAP);
            openOptionsMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.camera_intent, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mCapturingMedia) {
            mAudioManager.playSoundEffect(Sounds.DISALLOWED);
            return true;
        }

        switch(item.getItemId()) {
            case R.id.action_take_picture:
                startTakePictureIntent();
                return true;
            case R.id.action_record_video:
                startRecordVideoIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        if(mIndeterminate != null && mCapturingMedia) {
            mIndeterminate.show();
        }
    }

    private void startTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        mCapturingMedia = true;
    }

    private void startRecordVideoIntent() {
        Intent recordVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(recordVideoIntent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
        mCapturingMedia = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_CANCELED) {
            // user cancelled the image or video capture
            mCapturingMedia = false;
            return;
        } else if(resultCode != RESULT_OK) {
            // capture failed, advise user
            Toast.makeText(this, "Error capturing media", Toast.LENGTH_SHORT).show();
            mCapturingMedia = false;
            return;
        }

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            final String picturePath = data.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);
            // do something with picturePath if needed
            mIndeterminate = mSlider.startIndeterminate();
            processPictureWhenReady(picturePath);
        } else if(requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            String videoPath = data.getStringExtra(Intents.EXTRA_VIDEO_FILE_PATH);
            // do something with videoPath if needed
            mCapturingMedia = false;
        }

        String thumbnailPath = data.getStringExtra(Intents.EXTRA_THUMBNAIL_FILE_PATH);
        displayImage(thumbnailPath);

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void displayImage(String imagePath) {
        new LoadAndSetImageTask(mImage).execute(imagePath);
    }

    private class LoadAndSetImageTask extends AsyncTask<String, Void, Bitmap> {
        private WeakReference<ImageView> mImageViewWeakReference;

        public LoadAndSetImageTask(ImageView imageView) {
            mImageViewWeakReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return BitmapFactory.decodeFile(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (mImageViewWeakReference != null && bitmap != null) {
                final ImageView imageView = mImageViewWeakReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    /*
    private Bitmap loadImage(String picPath) {
        // Get the dimensions of the View
        int targetW = mImage.getWidth();
        int targetH = mImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        Log.e("orig", photoW + ", " + photoH);
        // Determine how much to scale down the image
        //int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = calculateInSampleSize(bmOptions, targetW, targetH);

        Bitmap bitmap = BitmapFactory.decodeFile(picPath, bmOptions);

        Log.e("real", bitmap.getWidth() + ", " + bitmap.getHeight());

        return bitmap;
    }

    // from http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    public int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
    */


    // from https://developers.google.com/glass/develop/gdk/camera
    private void processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            // The picture is ready; process it.
            Toast.makeText(getApplicationContext(), "picture is now ready for use",
                    Toast.LENGTH_SHORT).show();
            mCapturingMedia = false;
            mIndeterminate.hide();
            mIndeterminate = null;
        } else {
            final File parentDirectory = pictureFile.getParentFile();
            mObserver = new FileObserver(parentDirectory.getPath(),
                    FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
                // Protect against additional pending events after CLOSE_WRITE
                // or MOVED_TO is handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        File affectedFile = new File(parentDirectory, path);
                        isFileWritten = affectedFile.equals(pictureFile);

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };

            mObserver.startWatching();
        }
    }

}
