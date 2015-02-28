package ece473.trekker.imagesto3dmodels;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ImageCaptureActivity extends Activity implements CameraBridgeViewBase
        .CvCameraViewListener2
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_image_capture );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

        modelName = getIntent().getStringExtra( "modelName" );

        //create directory to save images in
        imageDirectory = MainMenuActivity.createDirectory( modelName + "/images" );

        cameraView = (CameraBridgeViewBase) findViewById( R.id.camera_view );
        cameraView.setVisibility( SurfaceView.VISIBLE );
        cameraView.setCvCameraViewListener( this );
        capture = false;
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
        //if the capture button was clicked save the frame to the Mat array
        if( capture )
        {
            //     Log.e("onCameraFrame", "capturing image " + imageArray.size());
            capture = false;
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat date = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.SSSZ" );
            String fileName = imageDirectory.getAbsolutePath() + "/" + date
                    .format( cal.getTime() ) + ".jpg";
            //write mat to jpg file
            Highgui.imwrite( fileName, inputFrame.rgba() );
        }

        return inputFrame.rgba();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync( OpenCVLoader.OPENCV_VERSION_2_4_6, this, loaderCallback );

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if( cameraView != null )
        {
            cameraView.disableView();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if( cameraView != null )
        {
            cameraView.disableView();
        }
    }

    /**
     * Called when capture button is clicked, sets the capture flag to true so that the next
     * frame is saved
     *
     * @param view - capture_button
     */
    public void captureImage( View view )
    {
        capture = true;
    }

    /**
     * Called when the done button is clicked, starts ModelPhotoGalleryActivity
     *
     * @param view - capture_complete_button
     */
    public void captureComplete( View view )
    {
        Intent galleryIntent = new Intent( this, ModelPhotoGalleryActivity.class );
        galleryIntent.putExtra( "modelName", modelName );
        galleryIntent.putExtra( "modelImageDirectory", imageDirectory.getAbsolutePath() );
        startActivity( galleryIntent );

    }

    /**
     * Callback that enables camera view
     */
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback( this )
    {
        @Override
        public void onManagerConnected( int status )
        {
            switch( status )
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    cameraView.enableView();
                }
                break;
                default:
                {
                    super.onManagerConnected( status );
                }
                break;
            }
        }
    };

    /**
     * View displaying camera preview
     */
    private CameraBridgeViewBase cameraView;

    /**
     * Boolean specifying that next camera frame should be saved
     */
    private boolean capture;

    /**
     * Name of 3D model to be created
     */
    private String modelName;

    /**
     * Directory storing all images for current model
     */
    private File imageDirectory;
}
