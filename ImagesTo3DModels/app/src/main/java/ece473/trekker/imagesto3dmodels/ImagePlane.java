package ece473.trekker.imagesto3dmodels;

import org.opencv.core.Mat;

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

    public ImagePlane( Mat m, int face )
    {
        noBackgroundImage = m;
        planeFace = face;
        plane = 0;
        findMaxEdges();
    }


    public void findMaxEdges()
    {
        rightEdge = findRightEdgePoints( noBackgroundImage );
        bottomEdge = findBottomEdgePoints( noBackgroundImage );
    }

    public void findMinEdges()
    {
        leftEdge = findLeftEdgePoints( noBackgroundImage );
        topEdge = findTopEdgePoints( noBackgroundImage );
        //   writeXYZ( MainMenuActivity.appDir +"/cube/cube.xyz");
    }

    public int getPlane()
    {
        return plane;
    }

    public int getPlaneFace()
    {
        return planeFace;
    }

    public int getMeanBottom()
    {
        return meanBottom;
    }

    public int getMeanRight()
    {
        return meanRight;
    }

    public void setPlane( int p1, int p2 )
    {
        if( p1 > p2 )
        {
            plane = p1;
        }
        else
        {
            plane = p2;
        }
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
        int total = 0;

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
                        bottomVertices.put( i, rowCounter );
                        total += rowCounter;
                    }
                    break;
                }
                rowCounter--;
            }
            //fill holes in edge with previous edge point if it exists
            if( ! bottomVertices.containsKey( i ) && bottomVertices.containsKey( i - 1 ) )
            {
                bottomVertices.put( i, bottomVertices.get( i - 1 ) );
                total += bottomVertices.get( i - 1 );
            }
        }
        meanBottom = total / bottomVertices.size();

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
        int total = 0;

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
                        leftVertices.put( i, colCounter + plane );
                        total += colCounter;
                    }
                    break;
                }
                colCounter++;
            }
            //fill holes in edge with previous edge point if it exists
            if( ! leftVertices.containsKey( i ) && leftVertices.containsKey( i - 1 ) )
            {
                leftVertices.put( i, leftVertices.get( i - 1 ) );
                total += leftVertices.get( i - 1 );
            }
        }
        meanLeft = total / leftVertices.size();

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
        int total = 0;

        for( int i = 1; i < numRows; i++ )
        {
            colCounter = numCols - 1;
            while( colCounter >= 0 )
            {
                if( input.get( i, colCounter )[0] != 0 )
                {
                    //ensure it's not the first or last point in it's column
                    if( input.get( i - 1, colCounter )[0] != 0 && input.get( i + 1,
                            colCounter )[0] != 0 )
                    {
                        rightVertices.put( i, colCounter );
                        total += colCounter;
                    }
                    break;
                }
                colCounter--;
            }

            //fill holes in edge with previous edge point if it exists
            if( ! rightVertices.containsKey( i ) && rightVertices.containsKey( i - 1 ) )
            {
                rightVertices.put( i, rightVertices.get( i - 1 ) );
            }

        }

        meanRight = total / rightVertices.size();

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
                    if( input.get( rowCounter, i - 1 )[0] != 0 && input.get( rowCounter,
                            i + 1 )[0] != 0 )
                    {
                        topVertices.put( i, rowCounter + plane );
                    }
                    break;
                }
                rowCounter++;
            }

            //fill holes in edge with previous edge point if it exists
            if( ! topVertices.containsKey( i ) && topVertices.containsKey( i - 1 ) )
            {
                topVertices.put( i, topVertices.get( i - 1 ) );
            }
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
                    filepath ), true ) );

            String vertex;

            for( Integer tv : rightEdge.keySet() )
            {
                vertex = rightEdge.get( tv ) + " " + tv + " " + 0 + "\n";
                bos.write( vertex.getBytes() );
            }

            for( Integer tv : leftEdge.keySet() )
            {
                vertex = leftEdge.get( tv ) + " " + tv + " " + 0 + "\n";
                bos.write( vertex.getBytes() );
            }

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

    protected HashMap<Integer, Integer> rightCorrelationEdge;
    protected HashMap<Integer, Integer> leftCorrelationEdge;
    protected HashMap<Integer, Integer> topCorrelationEdge;
    protected HashMap<Integer, Integer> bottomCorrelationEdge;

    private Mat noBackgroundImage;

    private int plane;

    private int planeFace;

    private int meanBottom;

    private int meanRight;

    public int meanLeft;

}
