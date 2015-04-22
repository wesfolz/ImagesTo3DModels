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
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.CvSVM;
import org.opencv.ml.CvSVMParams;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Created by wesley on 2/28/2015.
 */
public class Object3DModel
{
    public Object3DModel( String name, String modelImageDirectory )
    {
        triangleFaceArray = new ArrayList<>();
        vertexArray = new ArrayList<>();
        modelName = name;
        directoryName = modelImageDirectory.replace( "images", "" );

        //create3DModel( imageArray );
    }

    /**
     * Creates Model through following steps:
     * 1. Subtract background of all images.
     * 2. Resize each image so edges are all the same length
     * 3. Find four outer edges for each image.
     * 4. Triangulate each image to generate list of vertices and faces.
     * 5. Write vertices and faces to graphics file.
     */
    public void create3DModel()
    {
        ArrayList<Mat> imageArray = initData();
        ArrayList<Mat> noBackgroundImages = new ArrayList<>();
        ArrayList<ImagePlane> imagePlanes = new ArrayList<>();
        // ArrayList<ImagePlane> initialPlanes = new ArrayList<>();

        Mat nbi;
        int face = 0;

        //Mat test = removeBackground( imageArray.get( 0 ) );
        //Mat test = subtractBackgroundHistogram( imageArray.get( 3 ) );
        //Mat test = subtractBackgroundMachineLearning(imageArray.get( 0 ));
        //Highgui.imwrite( directoryName + "/noBackground.jpg", test );
        //ImagePlane testPlane = new ImagePlane( test );
        //testPlane.writeXYZ( directoryName + "/edges.xyz" );

        //create array of images without backgrounds
        for( Mat m : imageArray )
        {
            //subtract background
            //nbi = removeBackground( m );
            nbi = subtractBackgroundHistogram( m );
            int[] rect = cropImage( nbi );

            nbi = nbi.submat( rect[0], rect[1], rect[2], rect[3] );
            //initialPlanes.add( new ImagePlane( nbi ) );
            imagePlanes.add(new ImagePlane(nbi));
            noBackgroundImages.add( nbi );
        }

        //start with smallest face
        face = findMinFace( noBackgroundImages );

        //resize images and find outer edges
        for( Mat m : noBackgroundImages )
        {
            //resize images
//            resizeImages( initialPlanes, noBackgroundImages.get( face % 6 ), face % 6 );
            resizeImages( imagePlanes, noBackgroundImages.get( face % 6 ), face % 6 );
            //int[] rect = cropImage( m );

            //m =  m.submat( rect[0], rect[1], rect[2], rect[3] );
            //create image plane
            //imagePlanes.add(  new ImagePlane( noBackgroundImages.get( face%6 ) ));
            //imagePlanes.add( new ImagePlane( noBackgroundImages.get( face % 6 ) ) );
            imagePlanes.set( face % 6, new ImagePlane( noBackgroundImages.get( face % 6 ) ) );
            //imagePlanes.get( face%6 ).writeXYZ( directoryName + "/" + face%6 + ".xyz" );

            //write Mat to jpg file
            Highgui.imwrite( directoryName + "/noBackground" + face % 6 + ".jpg",
                    noBackgroundImages.get( face % 6 ) );
            face++;
        }

        imageArray = null;


        //Front Face:
        //Bottom edge of Top, Top edge of Bottom, Left Edge of Right, Right edge of Left
        //       triangulateImage2D( frontImage, FACE_FRONT, topEdge, bottomEdge, rightEdge,
        // leftEdge );
        triangulateImage2D( noBackgroundImages.get( FACE_FRONT ), FACE_FRONT,
                false,
                imagePlanes.get( FACE_BOTTOM ).topEdge, imagePlanes.get( FACE_RIGHT ).leftEdge );

        //Right Face:
        //Right edge of Top, Right edge of Bottom, Left Edge of Back (right),
        // Right edge of Front (left)
        triangulateImage2D( noBackgroundImages.get( FACE_RIGHT ), FACE_RIGHT,
                true, imagePlanes.get( FACE_TOP ).rightEdge,
                imagePlanes.get( FACE_FRONT ).rightEdge );

        //Back Face:
        //Top edge of Top, Bottom edge of Bottom, Left edge of Left, Right Edge of Right
        triangulateImage2D( noBackgroundImages.get( FACE_BACK ), FACE_BACK,
                true, imagePlanes.get( FACE_BOTTOM ).bottomEdge,
                imagePlanes.get( FACE_RIGHT ).rightEdge );


        //Left Face:
        //Left edge of Top, Left edge of Bottom, Left edge of Front, Right Edge of Back
        triangulateImage2D( noBackgroundImages.get( FACE_LEFT ), FACE_LEFT,
                false, imagePlanes.get( FACE_TOP ).leftEdge,
                imagePlanes.get( FACE_FRONT ).leftEdge );

        //Top Face:
        //Top edge of Back, Top edge of Front, Top edge of Right, Top Edge of Left
        triangulateImage2D( noBackgroundImages.get( FACE_TOP ), FACE_TOP,
                false, imagePlanes.get( FACE_FRONT ).topEdge,
                imagePlanes.get( FACE_RIGHT ).topEdge );

        //Bottom Face:
        //Bottom edge of Front, Bottom edge of Back, Bottom Edge of Right, Bottom edge of Left
        triangulateImage2D( noBackgroundImages.get( FACE_BOTTOM ), FACE_BOTTOM,
                true, imagePlanes.get( FACE_FRONT ).bottomEdge, imagePlanes.get( FACE_LEFT )
                        .bottomEdge );

        writePLYFile( directoryName + "/" + modelName + ".ply" );

    }


    private boolean checkTriangleSize( TriangleVertex[] tv, int clusterSize )
    {
        int multiplier = 16;
        return ((Math.abs( tv[0].x - tv[1].x ) <= clusterSize * multiplier) && (Math.abs( tv[0].x
                - tv[2].x )
                <= clusterSize * multiplier) && (Math.abs( tv[1].x - tv[2].x ) <= clusterSize *
                multiplier)
                && (Math.abs( tv[0].y - tv[1].y ) <= clusterSize * multiplier) && (Math.abs(
                tv[0].y - tv[2].y
        ) <= clusterSize * multiplier) && (Math.abs( tv[1].y - tv[2].y ) <= clusterSize *
                multiplier)
                && (Math.abs( tv[0].z - tv[1].z ) <= clusterSize * multiplier) && (Math.abs(
                tv[0].z - tv[2].z
        ) <= clusterSize * multiplier) && (Math.abs( tv[1].z - tv[2].z ) <= clusterSize *
                multiplier));
    }

    private int findMinFace( ArrayList<Mat> images )
    {
        int face = 0;
        int minFace = 0;
        int min = - 1;
        for( Mat m : images )
        {
            if( min < 0 )
                min = m.rows();

            if( m.cols() < min )
            {
                min = m.cols();
                minFace = face;
            }
            if( m.rows() < min )
            {
                min = m.rows();
                minFace = face;
            }
            face++;
        }
        return minFace;
    }

    /**
     *
     */
    private Mat removeBackground( Mat image ) throws IndexOutOfBoundsException {

        int threshold = 50;
        int j = 0;
        double diffRed = 0;
        double diffBlue = 0;
        double diffGreen = 0;
        double[] prevPixel;
        double[] pixel;
        double[] blackPixel = {0,0,0};

        for (int i=0; i < image.rows() ; i++){

            pixel = image.get(i, 0);

            for (j=1; j < image.cols() ; j++){

                prevPixel = pixel;
                pixel = image.get(i, j);

                diffRed = Math.abs(pixel[0] - prevPixel[0]);
                diffBlue = Math.abs(pixel[1] - prevPixel[1]);
                diffGreen = Math.abs(pixel[2] - prevPixel[2]);

                image.put(i,j-1,blackPixel);

                if ((diffRed + diffBlue + diffGreen) > threshold){
                    break;
                }
            }

            if (j == image.cols()){
                image.put(i,j-1,blackPixel);
            }
            else{
                pixel = image.get(i, image.cols()-1);

                for (j=image.cols()-2; j > 1 ; j--){

                    prevPixel = pixel;
                    pixel = image.get(i, j);

                    diffRed = Math.abs(pixel[0] - prevPixel[0]);
                    diffBlue = Math.abs(pixel[1] - prevPixel[1]);
                    diffGreen = Math.abs(pixel[2] - prevPixel[2]);

                    image.put(i,j+1,blackPixel);

                    if ((diffRed + diffBlue + diffGreen) > threshold){
                        break;
                    }
                }
                if (j == 0) {
                    throw new IndexOutOfBoundsException();
                }
            }
        }

        return image;
    }

    /**
     * Finds minimum rectangle to crop the image with
     *
     * @param image - input Mat image to crop
     * @return - int array giving crop rectangle as follows: {minRow, height, minCol, width}
     */
    private int[] cropImage( Mat image )
    {
        int minRow = 0;
        int minCol = 0;
        int width = image.width() - 1;
        int height = image.height() - 1;
        int numRows = image.rows();
        int numCols = image.cols();
        double minPixels = 20.0;
        int pixelValue = 255;
        Scalar sum;
        //Mat gray = new Mat();
        //Imgproc.cvtColor( image, gray, Imgproc.COLOR_BGR2GRAY );

        //remove top rows
        for( int i = 0; i < numRows; i++ )
        {
            sum = Core.sumElems( image.submat( i, i + 1, 0, width ) );
            if( sum.val[0] <= minPixels * pixelValue )
            {
                minRow++;
            }
            else
            {
                break;
            }
        }

        //Remove bottom rows
        for( int i = height; i >= 0; i-- )
        {
            sum = Core.sumElems( image.submat( i - 1, i, 0, width ) );
            if( sum.val[0] <= minPixels * pixelValue )
            {
                height--;
            }
            else
            {
                break;
            }
        }
        //remove left columns
        for( int i = 0; i < numCols; i++ )
        {
            sum = Core.sumElems( image.submat( 0, height, i, i + 1 ) );
            if( sum.val[0] <= minPixels * pixelValue )
            {
                minCol++;
            }
            else
            {
                break;
            }
        }
        //remove right columns
        for( int i = width; i >= 0; i-- )
        {
            sum = Core.sumElems( image.submat( 0, height, i - 1, i ) );
            if( sum.val[0] <= minPixels * pixelValue )
            {
                width--;
            }
            else
            {
                break;
            }
        }

        Log.e( "cropImage", "minRow " + minRow + " height " + height + " minCol " + minCol + " " +
                "width " + width );

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

        //Core.circle( cornerImage, findTopLeftCorner( cornerPoints ), 50, new Scalar( 255, 0,
        // 0 ) );

        Highgui.imwrite( directoryName + "/harrisCorner.jpg", cornerImage );

        return cornerPoints;
    }

    public static Mat detectEdges( Mat image )
    {
        Mat grayImg = new Mat();
        Mat edges = new Mat();
        Mat display = new Mat();

        //convert image to grayscale
        Imgproc.cvtColor( image, grayImg, Imgproc.COLOR_BGR2GRAY );

        //detect edges and copy into edges mat
        Imgproc.Canny( grayImg, edges, 100, 200 );

        //apply edge mask to original image and copy it to display Mat
        //image.copyTo( display, edges );

        //Highgui.imwrite( directoryName + "/cannyEdge.jpg", display );

        return edges;
    }

    /**
     * Finds coordinate of point in 3rd dimension
     * The point comes from the minimum or maximum edge point depending on the face
     *
     * @param horizontalEdge - HashMap of edge points in the horizontal direction
     * @param verticalEdge   - HashMap of edge points in the vertical direction
     * @param row            - current row of face
     * @param column         - current column of face
     * @param minOrMax       - true for min (back, right, and bottom faces) false for max (front,
     *                       left and top faces)
     * @param lastDepth      - the value of the previous depth point found
     * @return - integer value of depth of this point
     */
    private int findDepthPoint( HashMap<Integer, Integer> horizontalEdge, HashMap<Integer,
            Integer> verticalEdge, int row, int column, boolean minOrMax, int lastDepth )
    {
        int depth = - 1;

        if( horizontalEdge.containsKey( column ) )
        {
            depth = horizontalEdge.get( column );
        }

        if( verticalEdge.containsKey( row ) )
        {
            if( minOrMax )
            {
                if( verticalEdge.get( row ) < depth || depth < 0 )
                    depth = verticalEdge.get( row );
            }
            else
            {
                if( verticalEdge.get( row ) > depth || depth < 0 )
                    depth = verticalEdge.get( row );
            }
        }
        if( depth < 0 )
        {
            depth = lastDepth;
        }

        return depth;
    }

    public ArrayList<TriangleVertex> getVertexArray()
    {
        return vertexArray;
    }

    public ArrayList<TriangleFace> getTriangleFaceArray()
    {
        return triangleFaceArray;
    }

    /**
     * Calculates histogram and back projection
     *
     * @param image - input Mat image
     * @return - back projection
     */
    public Mat histogramBackProjection( Mat image )
    {
        Mat hist = new Mat();
        int h_bins = 2;
        int s_bins = 2;
        int rows = image.rows() - 1;
        int cols = image.cols() - 1;

        //      Mat tlHSV = new Mat();
        Mat brHSV = new Mat();
        Mat iHSV = new Mat();
        //     Mat topLeft = image.submat( 0, 50, 0, 100 );
        //    Mat bottomRight = image.submat( rows-50, rows, cols-100, cols );

        Mat top = image.submat( 0, 100, 0, cols );
        Mat bottom = image.submat( rows - 100, rows, 0, cols );
        Mat right = image.submat( 0, rows, cols - 200, cols );
        Mat left = image.submat( 0, rows, 0, 200 );
        image = image.submat( 100, rows - 100, 200, cols - 200 );

//        Imgproc.cvtColor( topLeft, tlHSV, Imgproc.COLOR_BGR2HSV );
//        Imgproc.cvtColor( bottomRight, brHSV, Imgproc.COLOR_BGR2HSV );

        Imgproc.cvtColor( top, top, Imgproc.COLOR_BGR2HSV );
        Imgproc.cvtColor( bottom, bottom, Imgproc.COLOR_BGR2HSV );
        Imgproc.cvtColor( right, right, Imgproc.COLOR_BGR2HSV );
        Imgproc.cvtColor( left, left, Imgproc.COLOR_BGR2HSV );
        Imgproc.cvtColor( image, iHSV, Imgproc.COLOR_BGR2HSV );

        // C++:
        //int histSize[] = { h_bins, s_bins };
        MatOfInt mHistSize = new MatOfInt( h_bins, s_bins );

        // C++:
        //float h_range[] = { 0, 179 };
        //float s_range[] = { 0, 255 };
        //const float* ranges[] = { h_range, s_range };
        //int channels[] = { 0, 1 };

        MatOfFloat mRanges = new MatOfFloat( 0, 179, 0, 255 );
        MatOfInt mChannels = new MatOfInt( 0, 1 );

        // C++:
        // calcHist( &hsv, 1, channels, mask, hist, 2, histSize, ranges, true, false );

        boolean accumulate = true;
        //  Imgproc.calcHist( Arrays.asList( tlHSV, brHSV ), mChannels, new Mat(), hist,
        // mHistSize, mRanges,
        //          accumulate );
        Imgproc.calcHist( Arrays.asList( top, bottom, left, right ), mChannels, new Mat(), hist,
                mHistSize, mRanges,
                accumulate );

        // C++:
        // normalize( hist, hist, 0, 255, NORM_MINMAX, -1, Mat() );
        Core.normalize( hist, hist, 0, 255, Core.NORM_MINMAX, - 1, new Mat() );

        // C++:
        // calcBackProject( &hsv, 1, channels, hist, backProjection, ranges, 1, true );
        Mat backProjection = new Mat();
        Imgproc.calcBackProject( Arrays.asList( iHSV ), mChannels, hist, backProjection, mRanges,
                1 );

        Mat disc = Imgproc.getStructuringElement( Imgproc.MORPH_ELLIPSE, new Size( 5, 5 ) );

        Imgproc.filter2D( backProjection, backProjection, - 1, disc );

        Imgproc.threshold( backProjection, backProjection, 50, 255, Imgproc.THRESH_BINARY );
        Core.merge( Arrays.asList( backProjection, backProjection, backProjection ),
                backProjection );
        Core.bitwise_and( image, backProjection, backProjection );


        //Core.inRange( backProjection, new Scalar( 255 ), new Scalar( 255 ), backProjection );
        Core.inRange( backProjection, new Scalar( 0 ), new Scalar( 0 ), backProjection );

        return backProjection;
    }

    /**
     * Initialize model name and array of Mat objects
     *
     * @return - arraylist of all images in images folder
     */
    public ArrayList<Mat> initData()
    {
        Mat bgr = null;
        File[] images = new File( directoryName + "/images" ).listFiles();
        ArrayList<Mat> imageArray = new ArrayList<>( images.length );
        //create array list of Mat objects for processing
        for( File f : images )
        {
            Mat rgb = Highgui.imread( f.getAbsolutePath() );
            //Imgproc.cvtColor( rgb, bgr, Imgproc.COLOR_RGB2BGR );
            imageArray.add( rgb );
        }

        Log.e( "Object3DModel", Integer.toString( imageArray.size() ) );

        return imageArray;
    }

    private void resizeImages( ArrayList<ImagePlane> planes, Mat image, int face )
    {
        switch( face )
        {
            case FACE_FRONT:
                applyResizeFactor( image, planes, face, planes.get( FACE_BOTTOM ).topEdge.size(),
                        planes.get( FACE_RIGHT ).leftEdge.size(),
                        planes.get( FACE_TOP ).bottomEdge.size(),
                        planes.get( FACE_LEFT ).rightEdge.size() );
                break;

            case FACE_RIGHT:
                applyResizeFactor( image, planes, face, planes.get( FACE_BOTTOM ).rightEdge.size
                                (), planes.get( FACE_BACK ).leftEdge.size(),
                        planes.get( FACE_TOP ).rightEdge.size(),
                        planes.get( FACE_FRONT ).rightEdge.size() );
                break;

            case FACE_BACK:
                applyResizeFactor( image, planes, face, planes.get( FACE_BOTTOM ).bottomEdge.size
                                (), planes.get( FACE_LEFT ).leftEdge.size(),
                        planes.get( FACE_TOP ).topEdge.size(),
                        planes.get( FACE_RIGHT ).rightEdge.size() );
                break;

            case FACE_LEFT:
                applyResizeFactor( image, planes, face, planes.get( FACE_BOTTOM ).leftEdge.size()
                        , planes.get( FACE_FRONT ).leftEdge.size(),
                        planes.get( FACE_TOP ).leftEdge.size(),
                        planes.get( FACE_BACK ).rightEdge.size() );
                break;

            case FACE_TOP:
                applyResizeFactor( image, planes, face, planes.get( FACE_FRONT ).topEdge.size(),
                        planes.get( FACE_RIGHT ).topEdge.size(),
                        planes.get( FACE_BACK ).topEdge.size(),
                        planes.get( FACE_LEFT ).topEdge.size() );
                break;

            case FACE_BOTTOM:
                applyResizeFactor( image, planes, face, planes.get( FACE_BACK ).bottomEdge.size()
                        , planes.get( FACE_RIGHT ).bottomEdge.size(),
                        planes.get( FACE_FRONT ).bottomEdge.size(),
                        planes.get( FACE_LEFT ).bottomEdge.size() );
                break;
        }
    }

    private float findMinimum( float a, float b, float c, float d )
    {
        float min = a;
        if( b < min )
            min = b;
        if( c < min )
            min = c;
        if( d < min )
            min = d;
        return min;
    }

    private void applyResizeFactor( Mat image, ArrayList<ImagePlane> planes, int face,
                                    float bottom, float right, float top, float left )
    {
    /*    float horizontal = planes.get( f1 ).cols();
        float bottom = image.cols();
        float vertical = planes.get( f2 ).rows();
        float right = image.rows();
        float horizontal2 = planes.get(f3).cols();
        float vertical2 = planes.get( f4 ).rows();
        float minHor = findMinimum( horizontal, horizontal2 );
        float minVert = findMinimum( vertical, vertical2 );
*/

        float currentBottom = planes.get( face ).bottomEdge.size();
        float currentRight = planes.get( face ).rightEdge.size();
        float currentTop = planes.get( face ).topEdge.size();
        float currentLeft = planes.get( face ).leftEdge.size();
        float minRatio = findMinimum( bottom / currentBottom, right / currentRight,
                top / currentTop, left / currentLeft );

        if( minRatio < 1 && minRatio > 0 )
        {
            Imgproc.resize( image, image, new Size(), minRatio, minRatio, Imgproc.INTER_AREA );
            Log.e( "createModel", "image size " + image.size() );
        }
    }

    /**
     * Removes and crops image based on histogram
     *
     * @param image - Mat image to remove background from
     * @return - Mat image with background removed and cropped
     */
    public Mat subtractBackgroundHistogram( Mat image )
    {
        Mat noBackground = new Mat();
        Mat backProjection = histogramBackProjection( image );
        //Mat backProjection = subtractBackgroundMachineLearning( image );

        image = image.submat( 100, image.rows() - 101, 200, image.cols() - 201 );

        int[] rect = cropImage( backProjection );

        image = image.submat( rect[0], rect[1], rect[2], rect[3] );

        backProjection = backProjection.submat( rect[0], rect[1], rect[2], rect[3] );

        image.copyTo( noBackground, backProjection );

        return noBackground;
        //     return backProjection;
    }


    public Mat subtractBackgroundMachineLearning( Mat inputImage )
    {
        Mat image = new Mat();
        Imgproc.cvtColor( inputImage, image, Imgproc.COLOR_BGR2HSV );

        Mat backgroundHorizontal = image.submat( 0, 100, 0, image.cols() );
        Mat backgroundVertical = image.submat( 0, image.rows(), 0, 200 );
        Mat right = image.submat( 0, image.rows(), image.cols() - 200, image.cols() );
        Mat bottom = image.submat( image.rows() - 100, image.rows(), 0,
                image.cols() );

        backgroundHorizontal.push_back( bottom );
        backgroundVertical.push_back( right );

        Mat object = image.submat( image.rows() / 2 - 100, image.rows() / 2 + 100,
                image.cols() / 2 - 200, image.cols() / 2 + 200 );
        Mat trainData = new Mat( backgroundHorizontal.rows() * backgroundHorizontal.cols() +
                backgroundVertical.rows() * backgroundVertical.cols() + object.rows() + object
                .cols(), 3, CvType.CV_32FC1 );
        Mat responses = new Mat( backgroundHorizontal.rows() * backgroundHorizontal.cols() +
                backgroundVertical.rows() * backgroundVertical.cols() + object.rows() + object
                .cols(), 1, CvType.CV_32FC1 );

        for( int i = 0; i < backgroundHorizontal.rows(); i++ )
        {
            for( int j = 0; j < backgroundHorizontal.cols(); j++ )
            {
                trainData.put( i * backgroundHorizontal.rows() + j, 0,
                        backgroundHorizontal.get( i, j ) );
                responses.put( i * backgroundHorizontal.rows() + j, 0, - 1.0 );
            }
        }

        for( int i = 0; i < backgroundVertical.rows(); i++ )
        {
            for( int j = 0; j < backgroundVertical.cols(); j++ )
            {
                trainData.put( backgroundHorizontal.rows() * backgroundHorizontal.cols() + i *
                        backgroundVertical.rows() + j, 0, backgroundVertical.get( i, j ) );

                responses.put( backgroundHorizontal.rows() * backgroundHorizontal.cols() + i *
                        backgroundVertical.rows() + j, 0, - 1.0 );
            }
        }

        for( int i = 0; i < object.rows(); i++ )
        {
            for( int j = 0; j < object.cols(); j++ )
            {
                trainData.put( backgroundHorizontal.rows() * backgroundHorizontal.cols() +
                        backgroundVertical.rows() * backgroundVertical.cols() + i *
                        object.rows() + j, 0, object.get( i, j ) );

                responses.put( backgroundHorizontal.rows() * backgroundHorizontal.cols() +
                        backgroundVertical.rows() * backgroundVertical.cols() + i *
                        object.rows() + j, 0, 1.0 );
            }
        }
        CvSVMParams params = new CvSVMParams();
        params.set_svm_type( CvSVM.C_SVC );
        //params.set_C( 0.1 );
        params.set_kernel_type( CvSVM.LINEAR );
        params.set_term_crit( new TermCriteria( TermCriteria.COUNT + TermCriteria.EPS, 10000,
                1e-6 ) );
        CvSVM svm = new CvSVM();
        svm.train( trainData, responses, new Mat(), new Mat(), params );

        image = image.submat( 100, image.rows() - 100, 200, image.cols() - 200 );
        Mat output = new Mat( image.size(), CvType.CV_8UC1 );
        for( int i = 0; i < image.rows(); i++ )
        {
            for( int j = 0; j < image.cols(); j++ )
            {
                Mat input = new Mat( 1, 3, CvType.CV_32FC1 );
                input.put( 0, 0, image.get( i, j ) );
                float response = svm.predict( input );
                if( response > 0 )
                {
                    output.put( i, j, 255 );
                }
                else
                {
                    output.put( i, j, 0 );
                    //output.put( i, j, -1 );
                }
            }
        }

        return output;
    }


    /**
     * Triangulates face by connecting nearest image pixel points
     *
     * @param image          - input image with background removed
     * @param face           - current face (front, right, back, left, top, or botoom) (0-5)
     * @param minOrMax       - true for min (back, right, and bottom faces) false for max (front,
     *                       left and top faces)
     * @param horizontalEdge - HashMap of edge points in the horizontal direction
     * @param verticalEdge   - HashMap of edge points in the vertical direction
     */
    public void triangulateImage2D( Mat image, int face, boolean minOrMax, HashMap<Integer,
            Integer> horizontalEdge, HashMap<Integer, Integer> verticalEdge )
    {
        // triangleFaceArray = new ArrayList<>();
        // vertexArray = new ArrayList<>();
        TriangleVertex[] tv = new TriangleVertex[3];
        tv[0] = new TriangleVertex( 0, 0, 0 );
        tv[1] = new TriangleVertex( 0, 1, 0 );
        tv[2] = new TriangleVertex( 1, 0, 0 );
        double grayScale;
        boolean inBackground;
        boolean recentlyAdded = false;
        int column;
        int row;
        int clusterSize = 4;
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
//                depth = findDepthPoint( topEdge, bottomEdge, rightEdge, leftEdge, row, column,
//                        plane, depth );

                depth = findDepthPoint( horizontalEdge, verticalEdge, row, column,
                        minOrMax, depth );

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
            compressFile( filepath );
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

            compressFile( filepath );

        }
        catch( IOException e )
        {
            e.printStackTrace();
        }

        Log.e( "triangulateImage2D", "obj written" );
    }

    /**
     * Gunzips the file located at filepath
     *
     * @param filepath - path to file
     */
    public static void compressFile( String filepath )
    {
        File outputFile = null;
        try
        {
            outputFile = new File( filepath );
            FileInputStream fis = new FileInputStream( outputFile );
            long length = outputFile.length();
            byte[] byteBuffer = new byte[(int) length];
            fis.read( byteBuffer );
            GZIPOutputStream gos = new GZIPOutputStream( new FileOutputStream( new File( filepath
                    + ".gzip" ) ) );
            gos.write( byteBuffer );
            gos.close();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }

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
