package ece473.trekker.imagesto3dmodels;

import android.util.Log;

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
        Mat grayImg =  new Mat();
        Mat edges =  new Mat();
        Mat display =  new Mat();

        //convert image to grayscale
        Imgproc.cvtColor( imageArray.get( 0 ), grayImg, Imgproc.COLOR_BGR2GRAY );

        //detect edges and copy into edges mat
        Imgproc.Canny( grayImg, edges, 15, 45  );

        //apply edge mask to original image and copy it to display Mat
        imageArray.get( 0 ).copyTo( display, edges );

        Highgui.imwrite( directoryName + "/cannyEdge.jpg", display );
        return new File(directoryName + "/cannyEdge.jpg");
    }

    /**
     * Initialize model name and array of Mat objects
     * @param name - name of model
     * @param modelImageDirectory - directory where images are stored
     */
    private void initData(String name, String modelImageDirectory)
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
