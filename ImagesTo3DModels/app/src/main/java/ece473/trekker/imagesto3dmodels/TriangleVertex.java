package ece473.trekker.imagesto3dmodels;

import org.opencv.core.Point3;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wesley on 3/6/2015.
 */
public class TriangleVertex extends Point3
{
    public TriangleVertex()
    {

    }

    public TriangleVertex( double x, double y, double z )
    {
        super( x, y, z );

    }


    /**
     * @return - index of vertex in it's arraylist (used for face to reference in obj file)
     */
    public int getIndex()
    {
        return index;
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
     * @param fos - FileOutputStream to obj file
     */
    public void writeVertexOBJ( FileOutputStream fos )
    {
        try
        {
            String vertex = "v " + x + " " + y + " " + z + "\n";
            fos.write( vertex.getBytes() );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }


    private int index;

}
