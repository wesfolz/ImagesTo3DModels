package ece473.trekker.imagesto3dmodels;

import android.test.ActivityInstrumentationTestCase2;

/**
 * Created by Ryan Hoefferle on 3/29/2015.
 */
public class MainMenuActivityTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

    private MainMenuActivity activity;

    public MainMenuActivityTest(){
        super(MainMenuActivity.class);
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
