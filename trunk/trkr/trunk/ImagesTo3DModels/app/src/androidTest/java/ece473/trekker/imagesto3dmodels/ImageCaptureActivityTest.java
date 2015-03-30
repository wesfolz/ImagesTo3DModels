package ece473.trekker.imagesto3dmodels;

import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;

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

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
