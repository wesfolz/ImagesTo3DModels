package ece473.trekker.imagesto3dmodels;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.internal.widget.ViewUtils;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.view.View;
import android.widget.Button;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by Ryan Hoefferle on 3/29/2015.
 */
public class ModelPhotoGalleryActivityTest extends
        ActivityInstrumentationTestCase2<ModelPhotoGalleryActivity>
{

    private ModelPhotoGalleryActivity activity;

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
    }

    /**
     * Tests requirement 2.2 to see if an 6 images must be present to do 3D conversion
     *
     * @throws Exception
     */
    public void testNumberOfImages() throws Exception
    {

        OutputStream out = null;

        String testImagePath = MainMenuActivity.appDir + "/test/images";

        File testDir = new File( testImagePath );
        if( testDir.exists() )
        {
            MainMenuActivity.DeleteRecursive( testDir );
        }

        MainMenuActivity.createDirectory( "test" );
        MainMenuActivity.createDirectory( "test/images" );


        View dummyView = activity.findViewById( R.id.create_model_button );
        Bitmap bitmap = BitmapFactory.decodeResource( MyApplication.getAppContext().getResources
                (), R.drawable.plus );

        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest1.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );

        activity.runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {

                activity.updateImgAdapter();

            }
        } );
        assertFalse( activity.createModel( dummyView ) );

        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest2.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );
        activity.runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {

                activity.updateImgAdapter();

            }
        } );
        assertFalse( activity.createModel( dummyView ) );

        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest3.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );
        activity.runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {

                activity.updateImgAdapter();

            }
        } );
        assertFalse( activity.createModel( dummyView ) );

        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest4.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );
        activity.runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {

                activity.updateImgAdapter();

            }
        } );
        assertFalse( activity.createModel( dummyView ) );

        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest5.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );
        activity.runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {

                activity.updateImgAdapter();

            }
        } );
        assertFalse( activity.createModel( dummyView ) );

        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest6.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );
        activity.runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {

                activity.updateImgAdapter();

            }
        } );
        assertTrue( activity.createModel( dummyView ) );

    }

    /**
     * Tests requirement 1.11 TSS compress current object files.
     *
     * @throws Exception
     */
    public void testCompression() throws Exception
    {

        OutputStream out = null;

        String testImagePath = MainMenuActivity.appDir + "/test/images";

        File testDir = new File( testImagePath );
        if( testDir.exists() )
        {
            MainMenuActivity.DeleteRecursive( testDir );
        }

        MainMenuActivity.createDirectory( "test" );
        MainMenuActivity.createDirectory( "test/images" );


        final View dummyView = activity.findViewById( R.id.create_model_button );
        Bitmap bitmap = BitmapFactory.decodeResource( MyApplication.getAppContext().getResources
                (), R.drawable.plus );

        out = new BufferedOutputStream( new FileOutputStream( testImagePath + "/capturetest1.jpg"
        ) );
        bitmap.compress( Bitmap.CompressFormat.JPEG, 100, out );

        Object3DModel.compressFile(testImagePath + "/capturetest1.jpg");


        File dir = activity.getModelImageDirectory();
        boolean compressed = false;
        if( dir.isDirectory() )
            for( File child : dir.listFiles() ){
                if (child.getName().contains("gzip"))
                    compressed = true;
            }

        assertTrue(compressed);

    }

    @Override
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
}
