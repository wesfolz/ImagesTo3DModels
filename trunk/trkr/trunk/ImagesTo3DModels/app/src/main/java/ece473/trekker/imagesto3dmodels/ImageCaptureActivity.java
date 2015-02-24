package ece473.trekker.imagesto3dmodels;

import android.app.Activity;
import android.os.Bundle;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;


public class ImageCaptureActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_image_capture );
    }

    /**
     * This method is invoked when camera preview has started. After this method is invoked
     * the frames will start to be delivered to client via the onCameraFrame() callback.
     *
     * @param width  -  the width of the frames that will be delivered
     * @param height - the height of the frames that will be delivered
     */
    @Override
    public void onCameraViewStarted( int width, int height )
    {

    }

    /**
     * This method is invoked when camera preview has been stopped for some reason.
     * No frames will be delivered via onCameraFrame() callback after this method is called.
     */
    @Override
    public void onCameraViewStopped()
    {

    }

    /**
     * This method is invoked when delivery of the frame needs to be done.
     * The returned values - is a modified frame which needs to be displayed on the screen.
     * TODO: pass the parameters specifying the format of the frame (BPP, YUV or RGB and etc)
     *
     * @param inputFrame Input camera frame
     */
    @Override
    public Mat onCameraFrame( CameraBridgeViewBase.CvCameraViewFrame inputFrame )
    {
        return null;
    }


}
