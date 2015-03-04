package ece473.trekker.imagesto3dmodels;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by wesley on 2/28/2015.
 */
public class Object3DModel
{
    public Object3DModel( String name, String modelImageDirectory )
    {
        initData( name, modelImageDirectory );
        //      detectEdges();
    }

    public File detectEdges()
    {
        Mat grayImg = new Mat();
        Mat edges = new Mat();
        Mat display = new Mat();

        //convert image to grayscale
        Imgproc.cvtColor( imageArray.get( 0 ), grayImg, Imgproc.COLOR_BGR2GRAY );

        //detect edges and copy into edges mat
        Imgproc.Canny( grayImg, edges, 15, 45 );

        //apply edge mask to original image and copy it to display Mat
        imageArray.get( 0 ).copyTo( display, edges );

        Highgui.imwrite( directoryName + "/cannyEdge.jpg", display );
        return new File( directoryName + "/cannyEdge.jpg" );
    }

    /**
     * Initialize model name and array of Mat objects
     *
     * @param name                - name of model
     * @param modelImageDirectory - directory where images are stored
     */
    private void initData( String name, String modelImageDirectory )
    {
        modelName = name;

        directoryName = modelImageDirectory;

        File[] images = new File( directoryName ).listFiles();
        imageArray = new ArrayList<>( images.length );
        //create array list of Mat objects for processing
        for( File f : images )
        {
            imageArray.add( Highgui.imread( f.getAbsolutePath() ) );
        }
        Log.e( "Object3DModel", Integer.toString( imageArray.size() ) );

    }

    public File subtractBackground()
    {
        Mat noBackground = new Mat();
        Mat binaryMask = new Mat( imageArray.get( 0 ).size(), CvType.CV_8U );

        //convert image to grayscale and store result in binaryMask
        Imgproc.cvtColor( imageArray.get( 0 ), binaryMask, Imgproc.COLOR_BGR2GRAY );

        //copy input Mat to mask
        //imageArray.get( 0 ).copyTo( binaryMask );
        //threshold for background color similarities
        int threshold = 50;
        //use very first pixel for background color reference
        double color = imageArray.get( 0 ).get( 0, 0 )[0];
        // Log.e( "subtractBackground", "Color " + input.type() );

        //double[] binaryArray = new double[imageArray.get( 0 ).rows()*imageArray.get( 0 ).cols()];

        //loop through Mat to check for similarly colored pixels
        for( int i = 0; i < imageArray.get( 0 ).rows(); i++ )
        {
            for( int j = 0; j < imageArray.get( 0 ).cols(); j++ )
            {
                //if pixel is within threshold set mask value to 0
                if( imageArray.get( 0 ).get( i, j )[0] <= color + threshold && imageArray.get( 0
                ).get( i, j )[0] >= color - threshold )
                {
                    binaryMask.put( i, j, 0 );
                    //binaryArray[j*imageArray.get( 0 ).rows() + i] = 0;
                    //  Log.e( "SubtractBackground", "0" );
                }
                else
                {
                    binaryMask.put( i, j, 1 );
                    //binaryArray[j*imageArray.get( 0 ).rows() + i] = 1;
                    //   Log.e( "SubtractBackground", "1" );
                }
            }
        }
        //binaryMask.put( 0, 0, binaryArray );
        //apply mask to imageArray Mat and copy result into noBackground
        imageArray.get( 0 ).copyTo( noBackground, binaryMask );

        //write Mat to jpg file and return it
        Highgui.imwrite( directoryName + "/noBackground.jpg", noBackground );
        return new File( directoryName + "/noBackground.jpg" );


        //     return noBackground;
    }


    /**
     * ArrayList storing Mat representations of images
     */
    private ArrayList<Mat> imageArray;

    /**
     * Name of this model
     */
    private String modelName;

    private String directoryName;

}
