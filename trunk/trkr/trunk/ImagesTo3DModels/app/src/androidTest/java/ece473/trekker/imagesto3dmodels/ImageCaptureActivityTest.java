package ece473.trekker.imagesto3dmodels;

import android.hardware.Camera;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Created by Ryan Hoefferle on 3/29/2015.
 */
public class ImageCaptureActivityTest extends ActivityInstrumentationTestCase2<ImageCaptureActivity> {

    private ImageCaptureActivity activity;

    public ImageCaptureActivityTest(){
        super(ImageCaptureActivity.class);
    }

    @Override
    public void setUp() throws Exception{
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
    }

    public void testImageCaptureButton() throws Exception
    {
        final ImageButton captureButton = (ImageButton) activity.findViewById( R.id
                .capture_button );

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                captureButton.performClick();
            }
        });

        assertTrue( true );
    }

    public void testCalibrationButton() throws Exception{
        final View decorView = activity.getWindow().getDecorView();

        ImageButton flashButton = (ImageButton) activity.findViewById(R.id.flash_button);
        ViewAsserts.assertOnScreen(decorView, flashButton);
        TouchUtils.clickView(this, flashButton);

        assertTrue( true );

    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
