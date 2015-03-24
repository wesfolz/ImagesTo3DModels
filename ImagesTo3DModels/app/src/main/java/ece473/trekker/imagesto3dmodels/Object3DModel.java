package ece473.trekker.imagesto3dmodels;

import android.util.Log;

import org.opencv.calib3d.StereoBM;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wesley on 2/28/2015.
 */
public class Object3DModel
{
    public Object3DModel( String name, String modelImageDirectory )
    {
        initData( name, modelImageDirectory );
        //  create3DModel();

        writeXYZ( directoryName + "/" + modelName + ".xyz" );
 /*       Mat doctoredImage = subtractBackground( imageArray.get( 0 ) );
        triangulateImage2D( doctoredImage, 0, null );
        writeOBJFile(directoryName +"/" + modelName + ".obj");
        */
    }

    public void create3DModel()
    {
        triangulateImage3D();
        //   writeOBJFile( directoryName + "/" + modelName + ".obj" );
    }

    /**
     * find minimum rectangle to crop the image with
     *
     * @param image    - input Mat image to crop
     * @param rowCount - ArrayList of Integers containing the number of non-blank pixels in each row
     * @param colCount - ArrayList of Integers containing the number of non-blank pixels in each
     *                 column
     * @return - int array giving crop rectangle as follows: {minRow, height, minCol, width}
     */
    private int[] cropImage( Mat image, ArrayList<Integer> rowCount, ArrayList<Integer> colCount )
    {
        Mat croppedImage;
        int minRow = 0;
        int minCol = 0;
        int width = image.width() - 1;
        int height = image.height() - 1;
        int minPixels = 1;
        int counter = 0;

        //determines minimum row value
        for( Integer count : rowCount )
        {
            counter++;
            if( count <= minPixels )
            {
                minRow = counter;
            }
            else
            {
                break;
            }
        }

        counter = 0;
        //determines minimum column value
        for( Integer count : colCount )
        {
            counter++;
            if( count <= minPixels )
            {
                minCol = counter;
            }
            else
            {
                break;
            }
        }

        //find height of object
        for( int i = height; i > 0; -- i )
        {
            if( rowCount.get( i ) <= minPixels )
            {
                height = i;
            }
            else
            {
                break;
            }
        }

        //find width of object
        for( int i = width; i > 0; -- i )
        {
            if( colCount.get( i ) <= minPixels )
            {
                width = i;
            }
            else
            {
                break;
            }
        }

        Log.e( "cropImage", "minCol " + minCol + " minRow " + minRow + " width " + width + " " +
                "height " + height );

        int[] rectangle = {minRow, height, minCol, width};
        // Rect r = new Rect( minRow, height, minCol, width );
        return rectangle;
    }

    public List<MatOfPoint> detectContours()
    {
        Mat canny = detectEdges();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours( canny, contours, hierarchy, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE );

        Mat drawing = Mat.zeros( canny.size(), CvType.CV_8UC3 );
        Imgproc.drawContours( drawing, contours, - 1, new Scalar( 100, 50, 200 ) );

        Highgui.imwrite( directoryName + "/contours.jpg", drawing );

        return contours;
    }

    public MatOfPoint detectCorners(Mat image)
    {
        Mat grayImg = new Mat();
        Mat cornerImage = new Mat();
        MatOfPoint cornerPoints = new MatOfPoint();

        int blockSize = 2;
        double k = 0.04;
        int maxCorners = 100;

        double qualityLevel = 0.01;
        double minDistance = 10;
        boolean useHarrisDetector = false;

        //get rgb image
        Imgproc.cvtColor( image, cornerImage, Imgproc.COLOR_BGR2RGB );

        //convert image to grayscale
        Imgproc.cvtColor( image, grayImg, Imgproc.COLOR_BGR2GRAY );

        Imgproc.goodFeaturesToTrack( grayImg, cornerPoints, maxCorners, qualityLevel,
                minDistance, new Mat(), blockSize, useHarrisDetector, k );

        Point[] cPoints = cornerPoints.toArray();

        for( Point p : cPoints )
        {
            Core.circle( cornerImage, p, 5, new Scalar( 0 ) );
        }

        Core.circle( cornerImage, findTopLeftCorner( cornerPoints ), 50, new Scalar( 255, 0, 0 ) );

        Highgui.imwrite( directoryName + "/harrisCorner.jpg", cornerImage );

        return cornerPoints;
    }

    public Mat detectEdges()
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

        return edges;
    }

    /**
     * Finds bottom edge of points in background subtracted image
     *
     * @param input - Mat with it's background subtracted by subtractBackground()
     * @return - ArrayList of TriangleVertex each being a bottom edge point
     */
    private ArrayList<TriangleVertex> findBottomEdgePoints( Mat input )
    {
        ArrayList<TriangleVertex> bottomVerts = new ArrayList<>();
        int numRows = input.rows();
        int numCols = input.cols() - 1;
        int rowCounter;

        for( int i = 1; i < numCols; i++ )
        {
            rowCounter = numRows - 1;
            while( rowCounter >= 0 )
            {
                if( input.get( rowCounter, i )[0] != 0 )
                {
                    //ensure it's not the first or last point in it's column
                    if( input.get( rowCounter, i - 1 )[0] != 0 && input.get( rowCounter,
                            i + 1 )[0] != 0 )
                    {
                        bottomVerts.add( new TriangleVertex( i, rowCounter, 0 ) );
                    }
                    break;
                }
                rowCounter--;
            }
        }

        return bottomVerts;
    }

    /**
     * Finds left edge of points in background subtracted image
     *
     * @param input - Mat with it's background subtracted by subtractBackground()
     * @return - ArrayList of TriangleVertex each being a left edge point
     */
    private ArrayList<TriangleVertex> findLeftEdgePoints( Mat input )
    {
        ArrayList<TriangleVertex> leftVerts = new ArrayList<>();
        int numRows = input.rows() - 1;
        int numCols = input.cols();
        int colCounter;

        for( int i = 1; i < numRows; i++ )
        {
            colCounter = 0;
            while( colCounter < numCols )
            {
                if( input.get( i, colCounter )[0] != 0 )
                {
                    //ensure it's not the first or last point in it's column
                    if( input.get( i - 1, colCounter )[0] != 0 && input.get( i + 1,
                            colCounter )[0] != 0 )
                    {
                        leftVerts.add( new TriangleVertex( colCounter, i, 0 ) );
                    }
                    break;
                }
                colCounter++;
            }

        }

        return leftVerts;
    }


    /**
     * Finds right edge of points in background subtracted image
     *
     * @param input - Mat with it's background subtracted by subtractBackground()
     * @return - ArrayList of TriangleVertex each being a right edge point
     */
    private ArrayList<TriangleVertex> findRightEdgePoints( Mat input )
    {
        ArrayList<TriangleVertex> rightVerts = new ArrayList<>();
        int numRows = input.rows() - 1;
        int numCols = input.cols();
        int colCounter;

        for( int i = 1; i < numRows; i++ )
        {
            colCounter = numCols - 1;
            while( colCounter >= 0 )
            {
                if( input.get( i, colCounter )[0] != 0 )
                {
                    //ensure it's not the first or last point in it's column
                    if( input.get( i - 1, colCounter )[0] != 0 && input.get( i + 1, colCounter )[0] != 0 )
                    {
                        rightVerts.add( new TriangleVertex( colCounter, i, 0 ) );
                    }
                    break;
                }
                colCounter--;
            }

        }

        return rightVerts;
    }

    /**
     * Finds bottom edge of points in background subtracted image
     *
     * @param input - Mat with it's background subtracted by subtractBackground()
     * @return - ArrayList of TriangleVertex each being a bottom edge point
     */
    private ArrayList<TriangleVertex> findTopEdgePoints( Mat input )
    {
        ArrayList<TriangleVertex> topVerts = new ArrayList<>();
        int numRows = input.rows();
        int numCols = input.cols() - 1;
        int rowCounter;

        for( int i = 1; i < numCols; i++ )
        {
            rowCounter = 0;
            while( rowCounter < numRows )
            {
                if( input.get( rowCounter, i )[0] != 0 )
                {
                    //ensure it's not the first or last point in it's column
                    if( input.get( rowCounter, i - 1 )[0] != 0 && input.get( rowCounter,
                            i + 1 )[0] != 0 )
                    {
                        topVerts.add( new TriangleVertex( i, rowCounter, 0 ) );
                    }
                    break;
                }
                rowCounter++;
            }
        }

        return topVerts;
    }


    public Point findTopLeftCorner( MatOfPoint corners )
    {
        Point[] cPoints = corners.toArray();
        double minX = 1500;
        double minY = 1500;

        for( Point p : cPoints )
        {
            if( p.x < minX && p.y < minY )
            {
                minX = p.x;
                minY = p.y;
            }
        }

        return new Point( minX, minY);
    }

    public void histogramBackProjection( Mat image )
    {
        Mat hsv = new Mat();
        Mat hue = new Mat();
        Mat hist = new Mat();
        Mat backProj = new Mat();
        int[] hSize = {12};
        MatOfInt histSize = new MatOfInt( hSize );
        float[] ranges = {0, 180};
        MatOfFloat hueRanges = new MatOfFloat( ranges );


        List<Mat> hsvList = new ArrayList<>();
        List<Mat> hueList = new ArrayList<>();
        int[] ch = {0, 0};
        MatOfInt channels = new MatOfInt( ch );
        Imgproc.cvtColor( image, hsv, Imgproc.COLOR_BGR2HSV );
        hue.create( hsv.size(), hsv.depth() );

        hsvList.add( hsv );
        hueList.add( hue );
        Core.mixChannels( hsvList, hueList, channels );


        Imgproc.calcHist( hueList, channels, new Mat(), hist, histSize, hueRanges );
        Core.normalize( hist, hist, 0, 255, Core.NORM_MINMAX, - 1, new Mat() );
        Imgproc.calcBackProject( hueList, channels, hist, backProj, hueRanges, 1 );

        Highgui.imwrite( directoryName + "/backProjection.jpg", backProj );
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

    /**
     * calls subtract background twice to improve background subtraction
     *
     * @param image - Mat image to remove background
     * @return - Mat image with background removed
     */
    public Mat iterativeBackgroundSubtraction( Mat image )
    {
        return subtractBackground( subtractBackground( image ) );
    }


    public void stereoImage()
    {
        Mat imgDisparity16S = new Mat( imageArray.get( 0 ).rows(), imageArray.get( 0 ).cols(),
                CvType.CV_16S );
        Mat imgDisparity8U = new Mat( imageArray.get( 0 ).rows(), imageArray.get( 0 ).cols(),
                CvType.CV_8UC1 );


        Mat grayImg0 = new Mat();
        Mat grayImg1 = new Mat();

        Mat maskedImage = new Mat();

        Imgproc.cvtColor( imageArray.get( 0 ), grayImg0, Imgproc.COLOR_BGR2GRAY );

        Imgproc.cvtColor( imageArray.get( 1 ), grayImg1, Imgproc.COLOR_BGR2GRAY );


        int ndisparities = 16 * 5;

        int SADWindowSize = 21;

        StereoBM sbm = new StereoBM( StereoBM.BASIC_PRESET, ndisparities, SADWindowSize );
        sbm.compute( grayImg0, grayImg1, imgDisparity16S, CvType.CV_16S );

        Core.MinMaxLocResult minMax = Core.minMaxLoc( imgDisparity16S );

        imgDisparity16S.convertTo( imgDisparity8U, CvType.CV_8UC1, 255 / (minMax.maxVal - minMax
                .maxVal) );

        imageArray.get( 0 ).copyTo( maskedImage, imgDisparity8U );

        Highgui.imwrite( directoryName + "/stereo.jpg", maskedImage );
    }

    /**
     * Removes the background of an image by selecting four corners and removing similarly
     * colored pixels
     *
     * @param image - Mat to remove background of
     * @return - returns Mat without background
     */
    public Mat subtractBackground( Mat image )
    {
        Mat noBackground = new Mat();
        Mat croppedImage;
        Mat croppedMask;
        Mat binaryMask = new Mat( imageArray.get( 0 ).size(), CvType.CV_8U );
        Mat cutBackground = new Mat();

        //convert image to grayscale and store result in binaryMask
        Imgproc.cvtColor( image, binaryMask, Imgproc.COLOR_BGR2GRAY );

        //threshold for background color similarities
        int threshold = 70;
        int numRows = image.rows();
        int numCols = image.cols();

        //use very first pixel for background color reference
        double[] colors = new double[4];
        colors[0] = image.get( 0, 0 )[0];
        colors[1] = image.get( numRows - 1, 0 )[0];
        colors[2] = image.get( 0, numCols - 1 )[0];
        colors[3] = image.get( numRows - 1, numCols - 1 )[0];


        ArrayList<Integer> rowCount = new ArrayList<>();
        ArrayList<Integer> colCount = new ArrayList<>(  );

        boolean background;

        Log.e( "subtractBackground", "Colors: " + colors[0] + " " + colors[1] + " " + colors[2] +
                " " + colors[3] );

        //loop through Mat to check for similarly colored pixels
        for( int i = 0; i < numRows; i++ )
        {
            rowCount.add( 0 );
            for( int j = 0; j < numCols; j++ )
            {
                if( i == 0 )
                {
                    colCount.add( 0 );
                }
                background = false;
                for( double color : colors )
                {
                    //if pixel is within threshold set mask value to 0
                    if( image.get( i, j )[0] <= color + threshold && image.get( i,
                            j )[0] >= color - threshold )
                    {
                        binaryMask.put( i, j, 0 );
                        background = true;
                        break;
                    }
                }
                if( ! background )
                {
                    binaryMask.put( i, j, 1 );
                    rowCount.set( i, rowCount.get( i ) + 1 );
                    colCount.set( j, colCount.get( j ) + 1 );
                    //   Log.e( "SubtractBackground", "1" );
                }
            }
        }

        Log.e( "subtractBackground", "rowCount " + rowCount.size() + " colCount " + colCount.size
                () );
        Log.e( "subtractBackground", "rowCount " + rowCount.get( 0 ) + " colCount " + colCount
                .get( 0 ) );
        //apply mask to imageArray Mat and copy result into noBackground
        //       image.copyTo( noBackground, binaryMask );

        int[] cropRect = cropImage( image, rowCount, colCount );
        croppedImage = image.submat( cropRect[0], cropRect[1], cropRect[2], cropRect[3] );
        croppedMask = binaryMask.submat( cropRect[0], cropRect[1], cropRect[2], cropRect[3] );
        croppedImage.copyTo( noBackground, croppedMask );

        //write Mat to jpg file and return it
        Highgui.imwrite( directoryName + "/noBackground.jpg", noBackground );

        return noBackground;
    }

    /**
     * Triangulates face by connecting nearest image pixel points
     *
     * @param image - background subtracted image to triangulate
     */
    public void triangulateImage2D( Mat image, int face, ArrayList<TriangleVertex> edgePoints )
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

                //Log.e( "triangulateImage2D", "Column " + column + " Row " + row );

                //get grayscale color value of pixel
                grayScale = image.get( row, column )[0];
                //update appropriate vertex
                tv[i % 3] = new TriangleVertex( column, row, 0 );
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
        Log.e( "triangulateImage2D", "Number of faces: " + triangleFaceArray.size() );
        Log.e( "triangulateImage2D", "Number of vertices: " + vertexArray.size() );
    }

    public void triangulateImage3D()
    {
        Mat leftImage = imageArray.get( 0 ).clone();
        Mat rightImage = imageArray.get( 1 ).clone();

        ArrayList<TriangleVertex> rightEdge = findRightEdgePoints( leftImage );
        ArrayList<TriangleVertex> leftEdge = findLeftEdgePoints( rightImage );

        triangulateImage2D( subtractBackground( imageArray.get( 0 ) ), FACE_FRONT, null );
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

        Log.e( "triangulateImage2D", "obj written" );
    }


    public void writeXYZ( String filepath )
    {
        Mat leftImage = subtractBackground( imageArray.get( 0 ) );

        ArrayList<TriangleVertex> topEdge = findTopEdgePoints( leftImage );
        ArrayList<TriangleVertex> bottomEdge = findBottomEdgePoints( leftImage );
        ArrayList<TriangleVertex> rightEdge = findRightEdgePoints( leftImage );
        ArrayList<TriangleVertex> leftEdge = findLeftEdgePoints( leftImage );


        try
        {
            BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( new File(
                    filepath ) ) );

            String vertex;

            for( TriangleVertex tv : rightEdge )
            {
                vertex = tv.x + " " + tv.y + " " + tv.z + "\n";
                bos.write( vertex.getBytes() );
            }

            for( TriangleVertex tv : leftEdge )
            {
                vertex = tv.x + " " + tv.y + " " + tv.z + "\n";
                bos.write( vertex.getBytes() );
            }

            for( TriangleVertex tv : topEdge )
            {
                vertex = tv.x + " " + tv.y + " " + tv.z + "\n";
                bos.write( vertex.getBytes() );
            }

            for( TriangleVertex tv : bottomEdge )
            {
                vertex = tv.x + " " + tv.y + " " + tv.z + "\n";
                bos.write( vertex.getBytes() );
            }


            bos.close();
        }
        catch( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
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


    public static final int FACE_FRONT = 0;

    public static final int FACE_RIGHT = 1;

    public static final int FACE_BACK = 2;

    public static final int FACE_LEFT = 3;

    public static final int FACE_TOP = 4;

    public static final int FACE_BOTTOM = 5;

}
