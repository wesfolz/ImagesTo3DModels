package ece473.trekker.imagesto3dmodels;

import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;

/**
 * Created by Ryan Hoefferle on 3/29/2015.
 */
public class ModelPhotoGalleryActivityTest extends
        ActivityInstrumentationTestCase2<ModelPhotoGalleryActivity>
{

    private ModelPhotoGalleryActivity activity;

    private BaseLoaderCallback mLoaderCallback;

    public ModelPhotoGalleryActivityTest()
    {
        super( ModelPhotoGalleryActivity.class );
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        setActivityInitialTouchMode( false );
        Intent galleryIntent = new Intent();
        galleryIntent.setClassName( "ece473.trekker.imagesto3dmodels",
                "ece473.trekker.imagesto3dmodels.ModelPhotoGalleryActivity" );
        galleryIntent.putExtra( "modelName", "test" );
        galleryIntent.putExtra( "modelImageDirectory", MainMenuActivity.appDir + "/test/images" );
        setActivityIntent( galleryIntent );
        activity = getActivity();

        mLoaderCallback = new BaseLoaderCallback( activity.getApplicationContext() )
        {

            @Override
            public void onManagerConnected( int status )
            {
                switch( status )
                {
                    case LoaderCallbackInterface.SUCCESS:
                    {
                        synchronized(this)
                        {
                            notify();
                        }
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

        OpenCVLoader.initAsync( OpenCVLoader.OPENCV_VERSION_2_4_6, activity.getApplicationContext
                (), mLoaderCallback );


    }

    /**
     * Tests requirement 1.4 to make sure image planes are creating outer edges
     *
     * @throws Exception
     */
    public void testOuterEdges() throws Exception
    {
        String testImagePath = MainMenuActivity.appDir + "/test/images";
        Object3DModel model = new Object3DModel( "test", testImagePath );
        Mat image = Highgui.imread( testImagePath + "noBackground.jpg" );

        ImagePlane plane = new ImagePlane( image, 0 );
        plane.setPlane( 0, 0 );
        plane.findMinEdges();

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
        String testImagePath = MainMenuActivity.appDir + "/test/images";
        Object3DModel model = new Object3DModel( "test", testImagePath );
        Mat image = Highgui.imread( testImagePath + "noBackground.jpg" );

        ImagePlane plane = new ImagePlane( image, 0 );
        plane.setPlane( 0, 0 );
        plane.findMinEdges();
        model.triangulateImage2D( image, 0, plane.getPlaneFace(), plane.topEdge,
                plane.bottomEdge, plane.rightEdge, plane.leftEdge );
        assertTrue( model.getTriangleFaceArray().size() > 0 );
        assertTrue( model.getVertexArray().size() > 0 );
    }

    /**
     * Tests requirement 1.8 to see if an output file can be saved to the device
     *
     * @throws Exception
     */
    public void testFileOutput_OBJ() throws Exception
    {
        String testImagePath = MainMenuActivity.appDir + "/test/images";
        Object3DModel model = new Object3DModel( "test", testImagePath );
        Mat image = Highgui.imread( testImagePath + "noBackground.jpg" );

        ImagePlane plane = new ImagePlane( image, 0 );
        plane.setPlane( 0, 0 );
        plane.findMinEdges();
        model.triangulateImage2D( image, 0, plane.getPlaneFace(), plane.topEdge,
                plane.bottomEdge, plane.rightEdge, plane.leftEdge );

        model.writeOBJFile( MainMenuActivity.appDir + "/test" );
        File objFile = new File( MainMenuActivity.appDir + "/test/test.obj" );
        assertTrue( objFile.exists() );
        assertTrue( objFile.length() > 0 );
    }

    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
}
