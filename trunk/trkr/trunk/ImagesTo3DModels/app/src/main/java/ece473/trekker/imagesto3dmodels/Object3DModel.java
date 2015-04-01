package ece473.trekker.imagesto3dmodels;

import android.util.Log;

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
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by wesley on 2/28/2015.
 */
public class Object3DModel
{
    public Object3DModel( String name, String modelImageDirectory )
    {
        ArrayList<Mat> imageArray = initData( name, modelImageDirectory );
        create3DModel( imageArray );
        //  subtractBackground( imageArray.get( 0 ) );
    }

    /**
     * Creates Model through following steps:
     * 1. Subtract background of all images.
     * 2. Find four outer edges for each image.
     * 3. Triangulate each image to generate list of vertices and faces.
     * 4. Write vertices and faces to graphics file.
     */
    public void create3DModel( ArrayList<Mat> imageArray )
    {
        triangulateImage3D( imageArray );
//        writeOBJFile(directoryName +"/" + modelName + ".obj");
//        writePLYFile( directoryName + "/" + modelName + ".ply" );

//        writeXYZ( directoryName + "/" + modelName + ".xyz" );
/*        int imageCounter = 0;
        for( Mat image : imageArray )
        {
            Mat doctoredImage = subtractBackground( image );
            triangulateImage2D( doctoredImage, imageCounter, null );
            imageCounter++;
        }

        Mat doctoredImage = subtractBackground( imageArray.get( 0 ) );
        triangulateImage2D( doctoredImage, FACE_BACK, null );
        triangulateImage2D( doctoredImage, FACE_RIGHT, null );
        triangulateImage2D( doctoredImage, FACE_BOTTOM, null );
        triangulateImage2D( doctoredImage, FACE_FRONT, null );
        triangulateImage2D( doctoredImage, FACE_LEFT, null );
        triangulateImage2D( doctoredImage, FACE_TOP, null );
        //writeOBJFile(directoryName +"/" + modelName + ".obj");
        writePLYFile( directoryName + "/" + modelName + ".ply" );
        */
    }


    private boolean checkTriangleSize( TriangleVertex[] tv, int clusterSize )
    {
        return ((Math.abs( tv[0].x - tv[1].x ) <= clusterSize) && (Math.abs( tv[0].x - tv[2].x )
                <= clusterSize) && (Math.abs( tv[1].x - tv[2].x ) <= clusterSize)
                && (Math.abs( tv[0].y - tv[1].y ) <= clusterSize) && (Math.abs( tv[0].y - tv[2].y
        ) <= clusterSize) && (Math.abs( tv[1].y - tv[2].y ) <= clusterSize)
                && (Math.abs( tv[0].z - tv[1].z ) <= clusterSize) && (Math.abs( tv[0].z - tv[2].z
        ) <= clusterSize) && (Math.abs( tv[1].z - tv[2].z ) <= clusterSize));
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
/*
        for(int i=0; i<height; i++)
        {
            croppedImage = image.submat( i, i+1, 0, width );
            if( Core.countNonZero( croppedImage ) <= minPixels)
            {
                minRow = i;
            }
            else
            {
                break;
            }
        }

        for(int i=0; i<width; i++)
        {
            croppedImage = image.submat( 0, height, i, i+1 );
            if( Core.countNonZero( croppedImage ) <= minPixels)
            {
                minCol = i;
            }
            else
            {
                break;
            }
        }

        for(int i=height; i>0; --i)
        {
            croppedImage = image.submat( i-1, i, 0, width );
            if( Core.countNonZero( croppedImage ) <= minPixels)
            {
                height = i;
            }
            else
            {
                break;
            }
        }

        for(int i=width; i>0; --i)
        {
            croppedImage = image.submat( 0, height, i-1, i );
            if( Core.countNonZero( croppedImage ) <= minPixels)
            {
                width = i;
            }
            else
            {
                break;
            }
        }
*/

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

    public List<MatOfPoint> detectContours( Mat image )
    {
        Mat canny = detectEdges( image );
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        Imgproc.findContours( canny, contours, hierarchy, Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE );

        Mat drawing = Mat.zeros( canny.size(), CvType.CV_8UC3 );
        Imgproc.drawContours( drawing, contours, - 1, new Scalar( 100, 50, 200 ) );

        Highgui.imwrite( directoryName + "/contours.jpg", drawing );

        return contours;
    }

    public MatOfPoint detectCorners( Mat image )
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

    public Mat detectEdges( Mat image )
    {
        Mat grayImg = new Mat();
        Mat edges = new Mat();
        Mat display = new Mat();

        //convert image to grayscale
        Imgproc.cvtColor( image, grayImg, Imgproc.COLOR_BGR2GRAY );

        //detect edges and copy into edges mat
        Imgproc.Canny( grayImg, edges, 15, 45 );

        //apply edge mask to original image and copy it to display Mat
        image.copyTo( display, edges );

        Highgui.imwrite( directoryName + "/cannyEdge.jpg", display );

        return edges;
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

        return new Point( minX, minY );
    }

    private int findDepthPoint( HashMap<Integer, Integer> topEdge,
                                HashMap<Integer, Integer> bottomEdge, HashMap<Integer,
            Integer> rightEdge, HashMap<Integer, Integer> leftEdge, int row, int column, int plane )
    {
        int min = - 1;

        if( topEdge.containsKey( column ) )
        {
            min = topEdge.get( column );
        }

        if( bottomEdge.containsKey( column ) )
        {
            if( bottomEdge.get( column ) < min || min < 0 )
                min = bottomEdge.get( column );
        }

        if( rightEdge.containsKey( row ) )
        {
            if( rightEdge.get( row ) < min || min < 0 )
                min = rightEdge.get( row );
        }

        if( leftEdge.containsKey( row ) )
        {
            if( leftEdge.get( row ) < min || min < 0 )
                min = leftEdge.get( row );
        }

        if( min < 0 )
        {
            min = plane;
        }
//        Log.e( "createModel", "Min: " + min );
        return min;
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
    private ArrayList<Mat> initData( String name, String modelImageDirectory )
    {
        modelName = name;

        directoryName = modelImageDirectory.replace( "images", "" );

        File[] images = new File( modelImageDirectory ).listFiles();
        ArrayList<Mat> imageArray = new ArrayList<>( images.length );
        //create array list of Mat objects for processing
        for( File f : images )
        {
            imageArray.add( Highgui.imread( f.getAbsolutePath() ) );
        }

        triangleFaceArray = new ArrayList<>();
        vertexArray = new ArrayList<>();

        Log.e( "Object3DModel", Integer.toString( imageArray.size() ) );
        return imageArray;

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
        Mat binaryMask = new Mat( image.size(), CvType.CV_8U );

        //threshold for background color similarities
        int threshold = 70;

        double[] cornerColor = image.get( 0, 0 );
        double[] lowerBound = new double[3];
        double[] upperBound = new double[3];

        for( int i = 0; i < cornerColor.length; i++ )
        {
            lowerBound[i] = cornerColor[i] - threshold;
            upperBound[i] = cornerColor[i] - threshold;
        }
        Scalar rangeLow = new Scalar( lowerBound );
        Scalar rangeHigh = new Scalar( upperBound );

        //convert image to grayscale and store result in binaryMask
        Imgproc.cvtColor( image, binaryMask, Imgproc.COLOR_BGR2GRAY );


        int numRows = image.rows();
        int numCols = image.cols();

        //use very first pixel for background color reference
        double[] colors = new double[4];
        colors[0] = image.get( 0, 0 )[0];
        colors[1] = image.get( numRows - 1, 0 )[0];
        colors[2] = image.get( 0, numCols - 1 )[0];
        colors[3] = image.get( numRows - 1, numCols - 1 )[0];

 /*       colors[0] = image.get( 0, 0 )[0];
        colors[1] = colors[0];
        colors[2] = colors[0];
        colors[3] = colors[0];
&*/

        ArrayList<Integer> rowCount = new ArrayList<>();
        ArrayList<Integer> colCount = new ArrayList<>();

        boolean background;

        Log.e( "subtractBackground", "Colors: " + colors[0] + " " + colors[1] + " " + colors[2] +
                " " + colors[3] );

        //     Core.inRange( image, rangeLow, rangeHigh, binaryMask );


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

//        Log.e( "subtractBackground", "rowCount " + rowCount.size() + " colCount " + colCount.size
        //               () );
        //       Log.e( "subtractBackground", "rowCount " + rowCount.get( 0 ) + " colCount " + colCount
        //              .get( 0 ) );
        //apply mask to imageArray Mat and copy result into noBackground
        //       image.copyTo( noBackground, binaryMask );

        int[] cropRect = cropImage( image, rowCount, colCount );
        croppedImage = image.submat( cropRect[0], cropRect[1], cropRect[2], cropRect[3] );
        croppedMask = binaryMask.submat( cropRect[0], cropRect[1], cropRect[2], cropRect[3] );
        croppedImage.copyTo( noBackground, croppedMask );

        //     image.copyTo( noBackground, binaryMask );

        return noBackground;
    }

    public Mat thresholdBackground( Mat image )
    {
        Mat grayImg = new Mat();
        Mat binaryMask = new Mat();
        Imgproc.cvtColor( image, grayImg, Imgproc.COLOR_BGR2GRAY );
        double thresh = grayImg.get( 0, 0 )[0] + 50.0;

        Imgproc.threshold( grayImg, binaryMask, thresh, 255, Imgproc.THRESH_TOZERO );

        return binaryMask;

    }

    /**
     * Triangulates face by connecting nearest image pixel points
     *
     * @param image - background subtracted image to triangulate
     */
    public void triangulateImage2D( Mat image, int face, int plane, HashMap<Integer,
            Integer> topEdge,
                                    HashMap<Integer, Integer> bottomEdge, HashMap<Integer,
            Integer> rightEdge, HashMap<Integer, Integer> leftEdge )
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
        int clusterSize = 16;
        int numCols = image.cols();
        int numRows = image.rows();
        int colIter = numCols - clusterSize;
        int rowIter = numRows * 2 - clusterSize * 2;
        int depth = 0;
        Mat grayImg = new Mat();

        Imgproc.cvtColor( image, grayImg, Imgproc.COLOR_RGB2GRAY );

        //loop through grayscale image
        for( int j = 0; j < colIter; j += clusterSize )
        // for( int j = 0; j < 10; j+=clusterSize )
        {
            row = 0;
            for( TriangleVertex v : tv )
            {
                v.setGrayScale( 0.0 );
            }
            for( int i = 0; i < rowIter; i += clusterSize )
            // for( int i = 0; i < 10; i+=clusterSize )
            {
                column = j + clusterSize - (i % (2 * clusterSize));
                row = row + (i % (2 * clusterSize));

                //Log.e( "triangulateImage2D", "Column " + column + " Row " + row );

                //get grayscale color value of pixel
                grayScale = grayImg.get( row, column )[0];
                //update appropriate vertex
                //tv[i % 3] = new TriangleVertex( column, row, 0 );
                //topEdge(column), bottomEdge(column), rightEdge(row), leftEdge(row)
                depth = findDepthPoint( topEdge, bottomEdge, rightEdge, leftEdge, row, column,
                        plane );

                tv[i % 3] = TriangleVertex.buildTriangleVertex( face, row, column, numRows,
                        numCols, depth );
                tv[i % 3].setGrayScale( grayScale );
                tv[i % 3].setColor( image.get( row, column ) );
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
                        if( checkTriangleSize( tv, clusterSize ) )
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

    public void triangulateImage3D( ArrayList<Mat> imageArray )
    {
        /*
        HashMap<Integer, Integer> rightEdge = findRightEdgePoints( frontImage, FACE_FRONT );
        HashMap<Integer, Integer> leftEdge = findLeftEdgePoints( frontImage );
        HashMap<Integer, Integer> topEdge = findTopEdgePoints( frontImage, FACE_TOP );
        HashMap<Integer, Integer> bottomEdge = findBottomEdgePoints( frontImage );
        planes[1] = planes[0];
        planes[4] = planes[0];
        Log.e("createModel", "front plane " + planes[0]+ " right plane " + planes[1]
                + " back plane " + planes[2]+ " left plane " + planes[3] + " top plane " +
                planes[4] + " bottom plane " + planes[5]);
  */
        int face = 0;
        int rightEdgePlane;
        int topEdgePlane;
        ArrayList<Mat> noBackgroundImages = new ArrayList<>();
        Vector<ImagePlane> imagePlanes = new Vector<>();
        Mat nbi;

        //create array of images without backgrounds
        for( Mat m : imageArray )
        {
            //      nbi = subtractBackground( m );
            //nbi = thresholdBackground( m );
            //write Mat to jpg file and return it
            //    Highgui.imwrite( directoryName + "/noBackground" + face + ".jpg", nbi );
            //      noBackgroundImages.add( nbi );
            noBackgroundImages.add( m );
            //      imagePlanes.add( new ImagePlane( nbi, face ) );
            imagePlanes.add( new ImagePlane( m, face ) );
            face++;
        }
        imageArray = null;
        //set right plane value
        imagePlanes.get( FACE_RIGHT ).setPlane( imagePlanes.get( FACE_FRONT ).getMeanRight(),
                imagePlanes.get( FACE_TOP ).getMeanRight() );
        //set back plane value
        imagePlanes.get( FACE_BACK ).setPlane( imagePlanes.get( FACE_RIGHT ).getMeanRight(),
                imagePlanes.get( FACE_BOTTOM ).getMeanBottom() );
        //set bottom plane value
        imagePlanes.get( FACE_BOTTOM ).setPlane( imagePlanes.get( FACE_FRONT ).getMeanBottom(),
                imagePlanes.get( FACE_RIGHT ).getMeanBottom() );

        for( ImagePlane im : imagePlanes )
        {
            im.findMinEdges();
        }

        Log.e( "createModel", "Right plane " + imagePlanes.get( FACE_RIGHT ).getPlane() + " Back " +
                "plane " + imagePlanes.get( FACE_BACK ).getPlane() + " Bottom Plane " +
                imagePlanes.get( FACE_BOTTOM ).getPlane() );
        Log.e( "createModel", "Left plane " + imagePlanes.get( FACE_LEFT ).getPlane() + " Front " +
                "plane " + imagePlanes.get( FACE_FRONT ).getPlane() + " Top Plane " + imagePlanes
                .get( FACE_TOP ).getPlane() );


/*
        for (ImagePlane im: imagePlanes)
        {
            im.rightCorrelationEdge = imagePlanes.get( im.getPlaneFace()+1 ).leftEdge;
            im.topCorrelationEdge = null;
            im.leftCorrelationEdge = null;
            im.bottomCorrelationEdge = null;
        }

        //assuming order or front, right, back, left, top, bottom
        Vector<HashMap<Integer, Integer>> rightEdges = new Vector<>();
        Vector<HashMap<Integer, Integer>> leftEdges = new Vector<>();
        Vector<HashMap<Integer, Integer>> topEdges = new Vector<>();
        Vector<HashMap<Integer, Integer>> bottomEdges = new Vector<>();

        face=0;
        for(Mat m: noBackgroundImages)
        {
            if(face == FACE_LEFT)
            {
                rightEdgePlane = FACE_FRONT;
                topEdgePlane = FACE_TOP;
            }
            else if( face == FACE_FRONT)
            {
                rightEdgePlane = FACE_RIGHT;
                topEdgePlane = FACE_TOP;
            }
            else
            {
                rightEdgePlane = -1;
                topEdgePlane = -1;
            }

            rightEdges.add( findRightEdgePoints( m, rightEdgePlane ) );
            topEdges.add( findTopEdgePoints( m, topEdgePlane ) );
            leftEdges.add( findLeftEdgePoints( m ) );
            bottomEdges.add( findBottomEdgePoints( m ) );
            face++;
        }*/

        //front plane = min of Right edge of Left face
        //right plane = min of Right edge of Front face
        //back plane = min of Right edge of Right Face (0)
        //left plane = min of Right edge of Back Face (0)
        //top plane = min of top edge of Front Face
        //bottom plane = (0)

        Log.e( "createModel", "plane " + imagePlanes.get( FACE_RIGHT ).getPlane() + " top right "
                + imagePlanes.get( FACE_TOP ).getMeanRight() + " bottom right " +
                imagePlanes.get( FACE_BOTTOM ).getMeanRight() + " back left " + /*imagePlanes.get
                ( FACE_BACK ) +*/ " front right " +
                imagePlanes.get( FACE_FRONT ).getMeanRight() );

        //Front Face:
        //Bottom edge of Top, Top edge of Bottom, Left Edge of Right, Right edge of Left
        //       triangulateImage2D( frontImage, FACE_FRONT, topEdge, bottomEdge, rightEdge,
        // leftEdge );
        triangulateImage2D( noBackgroundImages.get( FACE_FRONT ), FACE_FRONT,
                imagePlanes.get( FACE_FRONT ).getPlane(), imagePlanes.get( FACE_TOP ).bottomEdge,
                imagePlanes.get( FACE_BOTTOM ).topEdge, imagePlanes.get( FACE_RIGHT ).leftEdge,
                imagePlanes.get( FACE_LEFT ).rightEdge );

        //Right Face:
        //Right edge of Top, Right edge of Bottom, Left Edge of Back (right),
        // Right edge of Front (left)
//        triangulateImage2D( frontImage, FACE_RIGHT, topEdge, bottomEdge, rightEdge, leftEdge );
        triangulateImage2D( noBackgroundImages.get( FACE_RIGHT ), FACE_RIGHT,
                imagePlanes.get( FACE_RIGHT ).getPlane(), imagePlanes.get( FACE_TOP ).rightEdge,
                imagePlanes.get( FACE_BOTTOM ).rightEdge, imagePlanes.get( FACE_BACK ).leftEdge,
                imagePlanes.get( FACE_FRONT ).rightEdge );

        //Back Face:
        //Top edge of Top, Bottom edge of Bottom, Left edge of Left, Right Edge of Right
//        triangulateImage2D( frontImage, FACE_BACK, topEdge, bottomEdge, rightEdge, leftEdge );
        triangulateImage2D( noBackgroundImages.get( FACE_BACK ), FACE_BACK,
                imagePlanes.get( FACE_BACK ).getPlane(), imagePlanes.get( FACE_TOP ).topEdge,
                imagePlanes.get( FACE_BOTTOM ).bottomEdge, imagePlanes.get( FACE_LEFT ).leftEdge,
                imagePlanes.get( FACE_RIGHT ).rightEdge );


        //Left Face:
        //Left edge of Top, Left edge of Bottom, Left edge of Front, Right Edge of Back
//        triangulateImage2D( frontImage, FACE_LEFT, topEdge, bottomEdge, rightEdge, leftEdge );
        triangulateImage2D( noBackgroundImages.get( FACE_LEFT ), FACE_LEFT,
                imagePlanes.get( FACE_LEFT ).getPlane(), imagePlanes.get( FACE_TOP ).leftEdge,
                imagePlanes.get( FACE_BOTTOM ).leftEdge, imagePlanes.get( FACE_FRONT ).leftEdge,
                imagePlanes.get( FACE_BACK ).rightEdge );

        //Top Face:
        //Top edge of Back, Top edge of Front, Top edge of Right, Top Edge of Left
//        triangulateImage2D( frontImage, FACE_TOP, topEdge, bottomEdge, rightEdge, leftEdge );
        triangulateImage2D( noBackgroundImages.get( FACE_TOP ), FACE_TOP,
                imagePlanes.get( FACE_TOP ).getPlane(), imagePlanes.get( FACE_BACK ).topEdge,
                imagePlanes.get( FACE_FRONT ).topEdge, imagePlanes.get( FACE_RIGHT ).topEdge,
                imagePlanes.get( FACE_LEFT ).topEdge );

        //Bottom Face:
        //Bottom edge of Front, Bottom edge of Back, Bottom Edge of Right, Bottom edge of Left
//        triangulateImage2D( frontImage, FACE_BOTTOM, topEdge, bottomEdge, rightEdge, leftEdge );
        triangulateImage2D( noBackgroundImages.get( FACE_BOTTOM ), FACE_BOTTOM,
                imagePlanes.get( FACE_BOTTOM ).getPlane(), imagePlanes.get( FACE_FRONT ).bottomEdge,
                imagePlanes.get( FACE_BACK ).bottomEdge, imagePlanes.get( FACE_RIGHT )
                        .bottomEdge, imagePlanes.get( FACE_LEFT ).bottomEdge );

        writePLYFile( directoryName + "/" + modelName + ".ply" );

        //      Log.e( "triangulateImage3D", "Right: " + rightEdge.size() + "Left: " + leftEdge.size() +
        //             "Top: " + topEdge.size() + "Bottom: " + bottomEdge.size() );
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
        catch( IOException e )
        {
            e.printStackTrace();
        }

        Log.e( "triangulateImage2D", "obj written" );
    }

    public void writePLYFile( String filepath )
    {
        try
        {
            //BufferedOutputStream is far more efficient than FileOutputStream in this case
            BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( new File(
                    filepath ) ) );

            String magicNumber = "ply\nformat ascii 1.0\n";
            String numberOfVertices = "element vertex " + Integer.toString( vertexArray.size() )
                    + "\n";
            String vertexProperties = "property float x\nproperty float y\nproperty float " +
                    "z\nproperty uchar red\nproperty uchar green\nproperty uchar blue\n";
            String numberOfFaces = "element face " + triangleFaceArray.size() + "\n";
            String faceProperties = "property list uchar int vertex_index\n";
            String endHeader = "end_header\n";
            String header = magicNumber + numberOfVertices + vertexProperties + numberOfFaces +
                    faceProperties + endHeader;

            bos.write( header.getBytes() );

            //write vertices
            for( TriangleVertex tv : vertexArray )
            {
                tv.writeVertexPLY( bos );
            }

            //write faces
            for( TriangleFace tf : triangleFaceArray )
            {
                tf.writeTriangleFacePLY( bos );
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

    /**
     * List of triangle faces that compose the model
     */
    private ArrayList<TriangleFace> triangleFaceArray;

    /**
     * List of vertices that compose the model
     */
    private ArrayList<TriangleVertex> vertexArray;

    private String directoryName;

    private String modelDirectory;

    /**
     * Name of this model
     */
    private String modelName;



    public static final int FACE_FRONT = 0;

    public static final int FACE_RIGHT = 1;

    public static final int FACE_BACK = 2;

    public static final int FACE_LEFT = 3;

    public static final int FACE_TOP = 4;

    public static final int FACE_BOTTOM = 5;

}
