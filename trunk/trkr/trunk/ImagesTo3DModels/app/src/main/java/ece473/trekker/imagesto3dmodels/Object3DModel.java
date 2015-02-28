package ece473.trekker.imagesto3dmodels;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by wesley on 2/28/2015.
 */
public class Object3DModel
{
    public Object3DModel( String name, String modelImageDirectory )
    {
        modelName = name;

        File[] images = new File( modelImageDirectory ).listFiles();
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

}
