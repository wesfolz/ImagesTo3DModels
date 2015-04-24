package ece473.trekker.imagesto3dmodels;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by wesley on 3/27/2015.
 */
public class ImagePlane
{

    public ImagePlane( Mat m )
    {
        //noBackgroundImage = Object3DModel.detectEdges( m );
        noBackgroundImage = new Mat();
        Imgproc.cvtColor( m, noBackgroundImage, Imgproc.COLOR_BGR2GRAY );

        int rows = noBackgroundImage.rows() - 1;
        int cols = noBackgroundImage.cols() - 1;

/*
        rightEdge = findMaxEdgePoints( noBackgroundImage, rows, cols );
        bottomEdge = findMaxEdgePoints( noBackgroundImage, cols, rows );
        leftEdge = findMinEdgePoints( noBackgroundImage, rows, cols );
        topEdge = findMinEdgePoints( noBackgroundImage, cols, rows );
*/

        rightEdge = findRightEdgePoints( noBackgroundImage );
        bottomEdge = findBottomEdgePoints( noBackgroundImage );
        leftEdge = findLeftEdgePoints( noBackgroundImage );
        topEdge = findTopEdgePoints( noBackgroundImage );

    }


    private HashMap<Integer, Integer> findMaxEdgePoints( Mat input, int outerLoop, int innerLoop )
    {
        HashMap<Integer, Integer> vertices = new HashMap<>();
        int innerCounter;

        for( int i = 1; i < outerLoop; i++ )
        {
            innerCounter = innerLoop - 1;
            while( innerCounter >= 0 )
            {
                if( input.get( innerCounter, i )[0] != 0 )
                {
                    //ensure it's not the first or last point in it's column
                    //  if( input.get( innerCounter, i - 1 )[0] != 0 && input.get( innerCounter,
                    //          i + 1 )[0] != 0 )
                    //  {
                    vertices.put( i, innerCounter );
                    // }
                    break;
                }
                innerCounter--;
            }
            //fill holes in edge with previous edge point if it exists
        /*    if( ! vertices.containsKey( i ) && vertices.containsKey( i - 1 ) )
            {
                vertices.put( i, vertices.get( i - 1 ) );
            }
       */
        }
        return vertices;
    }

    private HashMap<Integer, Integer> findMinEdgePoints( Mat input, int outerLoop, int innerLoop )
    {
        HashMap<Integer, Integer> vertices = new HashMap<>();
        int innerCounter;

        for( int i = 1; i < outerLoop; i++ )
        {
            innerCounter = 0;
            while( innerCounter < innerLoop )
            {
                if( input.get( innerCounter, i )[0] != 0 )
                {
                    //ensure it's not the first or last point in it's column
                    //  if( input.get( innerCounter, i - 1 )[0] != 0 && input.get( innerCounter,
                    //          i + 1 )[0] != 0 )
                    //  {
                    vertices.put( i, innerCounter );
                    // }
                    break;
                }
                innerCounter++;
            }
            //fill holes in edge with previous edge point if it exists
        /*    if( ! vertices.containsKey( i ) && vertices.containsKey( i - 1 ) )
            {
                vertices.put( i, vertices.get( i - 1 ) );
            }
       */
        }
        return vertices;
    }

    /**
     * Finds bottom edge of points in background subtracted image
     *
     * @param input - Mat with it's background subtracted by subtractBackground()
     * @return - HashMap of Row values each being a bottom edge point with a key corresponding to
     * it's column
     */
    private HashMap<Integer, Integer> findBottomEdgePoints( Mat input )
    {
        HashMap<Integer, Integer> bottomVertices = new HashMap<>();
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
                    //    if( input.get( rowCounter, i - 1 )[0] != 0 && input.get( rowCounter,
                    //            i + 1 )[0] != 0 )
                    //    {
                    bottomVertices.put( i, rowCounter );
                    //     }
                    break;
                }
                rowCounter--;
            }
            //fill holes in edge with previous edge point if it exists
          /*  if( ! bottomVertices.containsKey( i ) && bottomVertices.containsKey( i - 1 ) )
            {
                bottomVertices.put( i, bottomVertices.get( i - 1 ) );
            }
       */
        }

        return bottomVertices;
    }

    /**
     * Finds left edge of points in background subtracted image
     *
     * @param input - Mat with it's background subtracted by subtractBackground()
     * @return - HashMap of Column values each being a left edge point with a key corresponding
     * to it's row
     */
    private HashMap<Integer, Integer> findLeftEdgePoints( Mat input )
    {
        HashMap<Integer, Integer> leftVertices = new HashMap<>();
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
                    //    if( input.get( i - 1, colCounter )[0] != 0 && input.get( i + 1,
                    //            colCounter )[0] != 0 )
                    //    {
                    leftVertices.put( i, colCounter );
                    //   }
                    break;
                }
                colCounter++;
            }
            //fill holes in edge with previous edge point if it exists
          /*  if( ! leftVertices.containsKey( i ) && leftVertices.containsKey( i - 1 ) )
            {
                leftVertices.put( i, leftVertices.get( i - 1 ) );
            }
            */
        }

        return leftVertices;
    }


    /**
     * Finds right edge of points in background subtracted image
     *
     * @param input - Mat with it's background subtracted by subtractBackground()
     * @return - HashMap of Column values each being a right edge point with a key corresponding
     * to it's row
     */
    private HashMap<Integer, Integer> findRightEdgePoints( Mat input )
    {
        HashMap<Integer, Integer> rightVertices = new HashMap<>();
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
                    //       if( input.get( i - 1, colCounter )[0] != 0 && input.get( i + 1,
                    //               colCounter )[0] != 0 )
                    //       {
                    rightVertices.put( i, colCounter );
                    //      }
                    break;
                }
                colCounter--;
            }

            //fill holes in edge with previous edge point if it exists
   /*         if( ! rightVertices.containsKey( i ) && rightVertices.containsKey( i - 1 ) )
            {
                rightVertices.put( i, rightVertices.get( i - 1 ) );
            }
            */
        }

        return rightVertices;
    }

    /**
     * Finds bottom edge of points in background subtracted image
     *
     * @param input - Mat with it's background subtracted by subtractBackground()
     * @return - HashMap of Row values each being a bottom edge point with a key corresponding to
     * it's column
     */
    private HashMap<Integer, Integer> findTopEdgePoints( Mat input )
    {
        HashMap<Integer, Integer> topVertices = new HashMap<>();
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
                    //      if( input.get( rowCounter, i - 1 )[0] != 0 && input.get( rowCounter,
                    //              i + 1 )[0] != 0 )
                    //      {
                    topVertices.put( i, rowCounter );
                    //      }
                    break;
                }
                rowCounter++;
            }

            //fill holes in edge with previous edge point if it exists
           /* if( ! topVertices.containsKey( i ) && topVertices.containsKey( i - 1 ) )
            {
                topVertices.put( i, topVertices.get( i - 1 ) );
            }*/
        }

        return topVertices;
    }


    public void writeXYZ( String filepath )
    {
    /*
        HashMap<Integer, Integer> topEdge = findTopEdgePoints( noBackgroundImage );
        HashMap<Integer, Integer> bottomEdge = findBottomEdgePoints( noBackgroundImage );
        HashMap<Integer, Integer> rightEdge = findRightEdgePoints( noBackgroundImage );
        HashMap<Integer, Integer> leftEdge = findLeftEdgePoints( noBackgroundImage );
*/

        try
        {
            BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( new File(
                    filepath ) ) );

            String vertex;

/*            for( Integer tv : rightEdge.keySet() )
            {
                vertex = rightEdge.get( tv ) + " " + tv + " " + 0 + "\n";
                bos.write( vertex.getBytes() );
            }

            for( Integer tv : leftEdge.keySet() )
            {
                vertex = leftEdge.get( tv ) + " " + tv + " " + 0 + "\n";
                bos.write( vertex.getBytes() );
            }
*/
            for( Integer tv : topEdge.keySet() )
            {
                vertex = tv + " " + topEdge.get( tv ) + " " + 0 + "\n";
                bos.write( vertex.getBytes() );
            }

            for( Integer tv : bottomEdge.keySet() )
            {
                vertex = tv + " " + bottomEdge.get( tv ) + " " + 0 + "\n";
                bos.write( vertex.getBytes() );
            }


            bos.close();
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    protected HashMap<Integer, Integer> rightEdge;
    protected HashMap<Integer, Integer> leftEdge;
    protected HashMap<Integer, Integer> topEdge;
    protected HashMap<Integer, Integer> bottomEdge;

    private Mat noBackgroundImage;
}
