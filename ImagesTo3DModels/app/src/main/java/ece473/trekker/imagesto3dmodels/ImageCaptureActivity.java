package ece473.trekker.imagesto3dmodels;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


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
        imageDirectory = MainMenuActivity.createDirectory( modelName + "/images" );
        captureNumber = getCaptureNumber();

        this.getWindow().getDecorView();

        //create directory to save images in

        cameraView = (TrekkerCameraView) findViewById( R.id.camera_view );
        cameraView.setVisibility( SurfaceView.VISIBLE );
        cameraView.setCvCameraViewListener( this );

        //make window fullscreen
        this.getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );

        cameraView.setOnTouchListener( new View.OnTouchListener()
        {
            @Override
            public boolean onTouch( View v, MotionEvent event )
            {
                Point size = new Point();
                getWindowManager().getDefaultDisplay().getSize( size );
                xBackground = (int) (1280 * event.getRawX() / size.x);
                yBackground = (int) (720 * event.getRawY() / size.y);
                Log.e( "OnTouch", Integer.toString( xBackground ) );
                Log.e( "OnTouch", Integer.toString( yBackground ) );

                Log.e( "OnTouch", "WINDOW SIZE " + size );
                return false;
            }
        } );

        capture = false;
    }

    @Override
    public void onWindowFocusChanged( boolean hasFocus )
    {
        super.onWindowFocusChanged( hasFocus );
        if( hasFocus )
        {
            //make window fullscreen
            this.getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
        }
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
            capture = false;

            String fileName = imageDirectory.getAbsolutePath() + "/capture" + Integer.toString(
                    captureNumber ) + ".jpg";
            //write mat to jpg file
            Highgui.imwrite( fileName, inputFrame.rgba() );
            try
            {
                String outFile = MainMenuActivity.thmNailDir.getPath() + "/" + getIntent()
                        .getStringExtra( "modelName" ) + ".png";
                Bitmap thumbImage = ThumbnailUtils.extractThumbnail( BitmapFactory.decodeFile(
                        fileName ), THUMBNAIL_SIZE, THUMBNAIL_SIZE );
                OutputStream out = null;
                try
                {
                    out = new BufferedOutputStream( new FileOutputStream( outFile ) );
                    thumbImage.compress( Bitmap.CompressFormat.PNG, 100, out );
                }
                finally
                {
                    if( out != null )
                    {
                        try
                        {
                            out.close();
                        }
                        catch( IOException e )
                        {
                            e.printStackTrace();
                        }
                    }
                }

            }
            catch( FileNotFoundException e )
            {
                e.printStackTrace();
            }
            captureNumber++;
            Log.e( "onCameraFrame", "Mat size: " + inputFrame.rgba().size() );
        }

        Mat rectMat = inputFrame.rgba();
        int rows = rectMat.rows() - 1;
        int cols = rectMat.cols() - 1;
        Scalar blue = new Scalar( 0, 0, 255 );
        Scalar red = new Scalar( 255, 0, 0 );
        //background rectangles
        Core.rectangle( rectMat, new org.opencv.core.Point( 0, 0 ), new org.opencv
                .core.Point( cols, 100 ), blue );
        Core.rectangle( rectMat, new org.opencv.core.Point( cols - 200, 0 ), new org.opencv
                .core.Point( cols, rows ), blue );
        Core.rectangle( rectMat, new org.opencv.core.Point( 0, 0 ), new org.opencv
                .core.Point( 200, rows ), blue );
        Core.rectangle( rectMat, new org.opencv.core.Point( 0, rows - 100 ), new org.opencv
                .core.Point( cols, rows ), blue );
        //object rectangles
        Core.rectangle( rectMat, new org.opencv.core.Point( cols / 2 - 200, rows / 2 - 100 ),
                new org.opencv
                        .core.Point( cols / 2 + 200, rows / 2 + 100 ), red );

/*
        Mat grayImg = new Mat(  );
        Mat binaryMask = new Mat(  );
        Imgproc.cvtColor( inputFrame.rgba(), grayImg, Imgproc.COLOR_RGB2GRAY );
        double thresh = grayImg.get( 0,0 )[0] + 50.0;

        Imgproc.threshold( grayImg, binaryMask, thresh, 255, Imgproc.THRESH_TOZERO_INV );

        return binaryMask;
*/

        return rectMat;
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
        if( getCaptureNumber() < 7 )
        {
            //cameraView.turnOnFlash();
            capture = true;
        }
        else
        {
            Toast.makeText( getApplicationContext(), "Maximum Number of Captures Reached.",
                    Toast.LENGTH_SHORT ).show();

        }
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


    private int getCaptureNumber()
    {
        int numberOfCaptures = 1;
        if( imageDirectory.exists() )
        {
            File[] fileList = imageDirectory.listFiles();

            if( fileList.length == 0 )
            {
                return numberOfCaptures;
            }

            for( File f : imageDirectory.listFiles() )
            {
                if( f.isFile() )
                {
                    String fileName = f.getName();
                    if( fileName.contains( "capture" ) ) numberOfCaptures++;

                }
                // make something with the name
            }
        }
        return numberOfCaptures;
    }

    /**
     * Called when flash button is clicked
     *
     * @param view
     */
    public void setFlash( View view )
    {
        cameraView.setFlashMode();
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
    private TrekkerCameraView cameraView;

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
    private int captureNumber;
    private int xBackground;
    private int yBackground;
    final int THUMBNAIL_SIZE = 128;

}
