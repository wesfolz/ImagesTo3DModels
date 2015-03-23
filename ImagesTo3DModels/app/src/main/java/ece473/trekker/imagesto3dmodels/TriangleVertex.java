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
        setExists( false );
    }

    public static TriangleVertex buildTriangleVertex( int face, int row, int column )
    {
        switch( face )
        {
            case Object3DModel.FACE_FRONT:
                return new TriangleVertex( column, row, 0 );

            case Object3DModel.FACE_RIGHT:
                return new TriangleVertex( 0, row, column );

            case Object3DModel.FACE_TOP:
                return new TriangleVertex( column, 0, row );

            default:
                return new TriangleVertex( 0, 0, 0 );
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

    public boolean isExists()
    {
        return exists;
    }

    public void setExists( boolean exists )
    {
        this.exists = exists;
    }

    /**
     * True if it already exists in the vertex array, false otherwise
     */
    private boolean exists;

    /**
     * Grayscale color value of vertex
     */
    private double grayScale;

    /**
     * index of vertex in it's arraylist (used for face to reference in obj file)
     */
    private int index;

}
