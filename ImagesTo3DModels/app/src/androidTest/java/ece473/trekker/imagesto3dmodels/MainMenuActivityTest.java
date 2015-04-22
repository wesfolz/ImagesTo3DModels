package ece473.trekker.imagesto3dmodels;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import junit.framework.Assert;

/**
 * Created by Ryan Hoefferle on 3/29/2015.
 */
public class MainMenuActivityTest extends ActivityInstrumentationTestCase2<MainMenuActivity> {

    private MainMenuActivity activity;

    public MainMenuActivityTest() {
        super(MainMenuActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);
        activity = getActivity();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


    /**
     * Tests requirement 1.9 to ensure sharing of object file.
     *
     * @throws Exception
     */
    @MediumTest
    public void testFileSharing() throws Exception{

        //Get the View and make sure that it is on the screen
        final int position = 0;
        final View decorView = activity.getWindow().getDecorView();
        activity.share = true;
        final ImageView object  = (ImageView) activity.getImgAdapter().getViewByPosition(position);
        ViewAsserts.assertOnScreen(decorView, object);
        assertTrue(activity.share);

        //Click the Object to share it
        final boolean[] flag = new boolean[]{false};
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.getGridview().performItemClick(object, position, position);
                synchronized (flag) {
                    flag[0] = true;
                    flag.notify();
                }

            }
        });
        if (!flag[0]) {
            // Wait (if necessary) for the asynchronous runOnUiThread to do its work
            synchronized (flag) {

                try {
                    flag.wait();
                } catch (InterruptedException e) {
                }
            }
        }
        //Check if email client intent has been set to action_send
        Intent email = activity.emailDataIntent;
        assertNotNull(email);
        String action = email.getAction();
        assertEquals(action, Intent.ACTION_SEND_MULTIPLE);
    }
}
