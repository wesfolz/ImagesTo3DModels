package ece473.trekker.imagesto3dmodels;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.view.View;
import android.widget.ImageButton;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by Ryan Hoefferle on 3/29/2015.
 */
public class ImageCaptureActivityTest extends ActivityInstrumentationTestCase2<ImageCaptureActivity>
{

    private ImageCaptureActivity activity;

    public ImageCaptureActivityTest()
    {
        super( ImageCaptureActivity.class );
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        setActivityInitialTouchMode( false );
        activity = getActivity();
    }

    public void testCalibrationButton() throws Exception
    {
        final View decorView = activity.getWindow().getDecorView();

        ImageButton flashButton = (ImageButton) activity.findViewById( R.id.flash_button );
        ViewAsserts.assertOnScreen( decorView, flashButton );
        TouchUtils.clickView( this, flashButton );

        assertTrue( true );

    }


    /**
     * Tests requirement 1.4 to make sure image planes are creating outer edges
     *
     * @throws Exception
     */
    public void testOuterEdges() throws Exception
    {
        String testImagePath = MainMenuActivity.appDir + "/test/images/";

        Mat image = Highgui.imread( testImagePath + "noBackground.jpg" );

        ImagePlane plane = new ImagePlane( image );

        assertTrue( plane.topEdge.size() > 0 );
        assertTrue( plane.bottomEdge.size() > 0 );
        assertTrue( plane.rightEdge.size() > 0 );
        assertTrue( plane.leftEdge.size() > 0 );
    }


    /**
     * Tests requirements 1.6 and 1.7 to make sure vertices and triangle faces are being created
     *
     * @throws Exception
     */
    public void testVerticesAndFaces() throws Exception
    {
        String testImagePath = MainMenuActivity.appDir + "/test/images/";
        Object3DModel model = new Object3DModel( "test", testImagePath );
        Mat image = Highgui.imread( testImagePath + "noBackground.jpg" );

        ImagePlane plane = new ImagePlane( image );
        model.triangulateImage2D( image, 0, false, plane.topEdge, plane.leftEdge );
        assertTrue( model.getTriangleFaceArray().size() > 0 );
        assertTrue( model.getVertexArray().size() > 0 );
    }


    /**
     * Tests requirement 1.8 and 2.3 to see if an output file(.obj) can be saved to the device
     *
     * @throws Exception
     */
    public void testFileOutput_OBJ() throws Exception
    {

        String testImagePath = MainMenuActivity.appDir + "/test/images/";
        Object3DModel model = new Object3DModel( "test", testImagePath );
        Mat image = Highgui.imread( testImagePath + "noBackground.jpg" );

        ImagePlane plane = new ImagePlane( image );

        model.triangulateImage2D( image, 0, true, plane.topEdge,
                plane.leftEdge );

        model.writeOBJFile( MainMenuActivity.appDir + "/test/test.obj" );
        File objFile = new File( MainMenuActivity.appDir + "/test/test.obj" );
        assertTrue( objFile.exists() );
        assertTrue( objFile.length() > 0 );
    }


    /**
     * Tests requirement 2.4 to see if a .ply file can be saved to the device
     *
     * @throws Exception
     */
    public void testFileOutput_PLY() throws Exception
    {
        String testImagePath = MainMenuActivity.appDir + "/test/images/";
        Object3DModel model = new Object3DModel( "test", testImagePath );
        Mat image = Highgui.imread( testImagePath + "noBackground.jpg" );

        ImagePlane plane = new ImagePlane( image );

        model.triangulateImage2D( image, 0, true, plane.topEdge,
                plane.leftEdge );

        model.writePLYFile( MainMenuActivity.appDir + "/test/test.ply" );
        File plyFile = new File( MainMenuActivity.appDir + "/test/test.ply" );
        assertTrue( plyFile.exists() );
        assertTrue( plyFile.length() > 0 );
    }

    /**
     * Tests requirement 1.2 to test if multiple images are saved as internal data structures
     *
     * @throws Exception
     */
    public void testForMultipleImages() throws Exception
    {

        OutputStream out = null;
        String testImagePath = MainMenuActivity.appDir + "/test12/images";

        Object3DModel testModel = new Object3DModel( "testModel", testImagePath );

        File testDir = new File( testImagePath );
        if( testDir.exists() )
        {
            MainMenuActivity.DeleteRecursive( testDir );
        }

        MainMenuActivity.createDirectory( "test12" );
        MainMenuActivity.createDirectory( "test12/images" );

        View dummyView = activity.findViewById( R.id.create_model_button );
        Bitmap bitmap = BitmapFactory.decodeResource( MyApplication.getAppContext().getResources
                (), R.drawable.plus );

        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest1.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );
        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest2.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );
        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest3.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );
        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest4.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );
        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest5.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );
        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest6.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );
        assertNotNull( testModel.initData() );

    }


    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
}
