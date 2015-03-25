package ece473.trekker.imagesto3dmodels;

import org.opencv.core.Point3;

import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * Created by wesley on 3/6/2015.
 */
public class TriangleVertex extends Point3
{
    public TriangleVertex()
    {
        this( 0, 0, 0 );
    }

    public TriangleVertex( double x, double y, double z )
    {
        super( x, y, z );
        setGrayScale( 0 );
        setIndex( 0 );
    }

    public static TriangleVertex buildTriangleVertex( int face, int row, int column, int maxRow,
                                                      int maxColumn )
    {
        switch( face )
        {
            case Object3DModel.FACE_FRONT:
                return new TriangleVertex( column, row, maxColumn ); //return new TriangleVertex( column, row, 0 );

            case Object3DModel.FACE_RIGHT:
                return new TriangleVertex( maxColumn, row, column ); //return new TriangleVertex(
                // 0, row, column );

            case Object3DModel.FACE_BACK:
                return new TriangleVertex( maxColumn - column, row, 0 );

            case Object3DModel.FACE_LEFT:
                return new TriangleVertex( 0, row, maxColumn - column );

            case Object3DModel.FACE_TOP:
                return new TriangleVertex( column, maxColumn, row ); //return new TriangleVertex( column, 0, row );

            case Object3DModel.FACE_BOTTOM:
                return new TriangleVertex( column, 0, row );

            default:
                return new TriangleVertex();
        }
    }

    /**
     * @return - Grayscale color value of vertex
     */
    public double getGrayScale()
    {
        return grayScale;
    }

    /**
     * @return - index of vertex in it's arraylist (used for face to reference in obj file)
     */
    public int getIndex()
    {
        return index;
    }


    public void setColor( double[] c )
    {
        this.color = c;
    }

    /**
     * @param grayScale - Grayscale color value of vertex
     */
    public void setGrayScale( double grayScale )
    {
        this.grayScale = grayScale;
    }

    /**
     * @param index - index of vertex in it's arraylist (used for face to reference in obj file)
     */
    public void setIndex( int index )
    {
        this.index = index;
    }

    /**
     * Writes data of vertex to supplied FileOutputStream (must be obj file)
     *
     * @param bos - BufferedOutputStream to obj file
     */
    public void writeVertexOBJ( BufferedOutputStream bos )
    {
        try
        {
            String vertex = "v " + x + " " + y + " " + z + "\n";
            // BufferedOutputStream bos = new BufferedOutputStream( fos );
            bos.write( vertex.getBytes() );
            // fos.write( vertex.getBytes() );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Writes data of vertex to supplied FileOutputStream (must be ply file)
     *
     * @param bos - BufferedOutputStream to ply file
     */
    public void writeVertexPLY( BufferedOutputStream bos )
    {
        try
        {
            String vertex = x + " " + y + " " + z + " " + (int) color[0] + " " + (int) color[1] +
                    " " + (int) color[2] + "\n";
            bos.write( vertex.getBytes() );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    private double[] color;

    /**
     * Grayscale color value of vertex
     */
    private double grayScale;

    /**
     * index of vertex in it's arraylist (used for face to reference in obj file)
     */
    private int index;

}
