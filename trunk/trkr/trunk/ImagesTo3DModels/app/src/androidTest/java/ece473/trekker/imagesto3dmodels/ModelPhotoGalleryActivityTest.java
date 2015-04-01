package ece473.trekker.imagesto3dmodels;

import android.test.ActivityInstrumentationTestCase2;

/**
 * Created by Ryan Hoefferle on 3/29/2015.
 */
public class ModelPhotoGalleryActivityTest extends ActivityInstrumentationTestCase2<ModelPhotoGalleryActivity> {

    private ModelPhotoGalleryActivity activity;

    public ModelPhotoGalleryActivityTest(){
        super(ModelPhotoGalleryActivity.class);
    }

    @Override
    public void setUp() throws Exception{
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
    }

    public void testVertexCollection() throws Exception
    {


    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
