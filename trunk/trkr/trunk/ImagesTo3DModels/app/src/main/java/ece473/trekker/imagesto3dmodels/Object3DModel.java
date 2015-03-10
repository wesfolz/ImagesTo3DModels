package ece473.trekker.imagesto3dmodels;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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

        triangleFaceArray = new ArrayList<>();
        vertexArray = new ArrayList<>();

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

        triangulate2DImage( noBackground );

        writeOBJFile( directoryName + "/" + modelName + ".obj" );

        //write Mat to jpg file and return it
        Highgui.imwrite( directoryName + "/noBackground.jpg", noBackground );
        return new File( directoryName + "/noBackground.jpg" );


        //     return noBackground;
    }

    /**
     * Triangulates face by connecting nearest image pixel points
     *
     * @param image - grayscale image to triangulate
     */
    public void triangulate2DImage( Mat image )
    {
        TriangleVertex[] tv = new TriangleVertex[3];
        tv[0] = new TriangleVertex( 0, 0, 0 );
        tv[1] = new TriangleVertex( 0, 1, 0 );
        tv[2] = new TriangleVertex( 1, 0, 0 );
        double grayScale;
        boolean inBackground;
        boolean recentlyAdded = false;
        int column;
        int row;
        int clusterSize = 2;
        int numColumns = image.cols() - clusterSize;
        int numRows = image.rows() * 2 - clusterSize * 2;

        //loop through grayscale image
        for( int j = 0; j < numColumns; j += clusterSize )
        // for( int j = 0; j < 10; j+=clusterSize )
        {
            row = 0;
            for( TriangleVertex v : tv )
            {
                v.setGrayScale( 0.0 );
            }
            for( int i = 0; i < numRows; i += clusterSize )
            // for( int i = 0; i < 10; i+=clusterSize )
            {
                column = j + clusterSize - (i % (2 * clusterSize));
                row = row + (i % (2 * clusterSize));

                //Log.e( "triangulate2DImage", "Column " + column + " Row " + row );

                //get grayscale color value of pixel
                grayScale = image.get( row, column )[0];
                //update appropriate vertex
                tv[i % 3] = new TriangleVertex( row, column, 0 );
                tv[i % 3].setGrayScale( grayScale );
                //only accept non-zero pixels
                if( grayScale != 0 )
                {
                    inBackground = false;
                    //make sure all current vertices have non-zero values
                    for( TriangleVertex vertex : tv )
                    {
                        if( vertex.getGrayScale() == 0 )
                        {
                            inBackground = true;
                            recentlyAdded = false;
                            break;
                        }
                    }
                    //add new triangle if all three vertices are non-background pixels (have
                    // non-zero grayscale values)
                    if( ! inBackground )
                    {
                        // Log.e( "triangulateImage2D", "tv[0].x " + tv[0].x + " tv[0].y " +
                        // tv[0].y + " tv[1].x " + tv[1].x + " tv[1].y " + tv[1].y + " tv[2].x "
                        // + tv[2].x +   " tv[2].y " + tv[2].y );
                        if( ! recentlyAdded )
                        {
                            vertexArray.add( tv[0] );
                            tv[0].setIndex( vertexArray.size() );

                            vertexArray.add( tv[1] );
                            tv[1].setIndex( vertexArray.size() );

                            vertexArray.add( tv[2] );
                            tv[2].setIndex( vertexArray.size() );

                            recentlyAdded = true;
                        }
                        else
                        {
                            vertexArray.add( tv[i % 3] );
                            tv[i % 3].setIndex( vertexArray.size() );
                        }
                        triangleFaceArray.add( new TriangleFace( tv.clone() ) );
                    }
                }
                else
                {
                    recentlyAdded = false;
                }
            }
        }
        Log.e( "triangulate2DImage", "Number of faces: " + triangleFaceArray.size() );
        Log.e( "triangulate2DImage", "Number of vertices: " + vertexArray.size() );
    }

    /**
     * Writes vertices and triangle faces to an obj file
     *
     * @param filepath - complete path to file including name
     */
    public void writeOBJFile( String filepath )
    {
        try
        {
            //BufferedOutputStream is far more efficient than FileOutputStream in this case
            BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( new File(
                    filepath ) ) );

            //write vertices
            for( TriangleVertex tv : vertexArray )
            {
                tv.writeVertexOBJ( bos );
            }

            //write faces
            for( TriangleFace tf : triangleFaceArray )
            {
                tf.writeTriangleFaceOBJ( bos );
            }

            bos.close();
        }
        catch( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch( IOException e1 )
        {
            e1.printStackTrace();
        }

        Log.e( "triangulate2DImage", "obj written" );
    }


    /**
     * ArrayList storing Mat representations of images
     */
    private ArrayList<Mat> imageArray;

    /**
     * List of triangle faces that compose the model
     */
    private ArrayList<TriangleFace> triangleFaceArray;

    /**
     * List of vertices that compose the model
     */
    private ArrayList<TriangleVertex> vertexArray;

    /**
     * Name of this model
     */
    private String modelName;

    private String directoryName;

}
