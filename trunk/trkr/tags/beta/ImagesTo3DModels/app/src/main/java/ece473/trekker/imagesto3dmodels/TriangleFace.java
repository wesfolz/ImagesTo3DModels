package ece473.trekker.imagesto3dmodels;

import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * Created by wesley on 3/6/2015.
 */
public class TriangleFace
{
    public TriangleFace()
    {
        //initialize vertices to 0
        vertices = new TriangleVertex[3];
    }

    /**
     * @param verts - array of TriangleVertex of size 3
     */
    public TriangleFace( TriangleVertex[] verts )
    {
        //initialize vertices to 0
        vertices = verts;
    }

    /**
     * @return - array of TriangleVertex of size 3
     */
    public TriangleVertex[] getVertices()
    {
        return vertices;
    }

    /**
     * Writes data of face to supplied FileOutputStream (must be obj file)
     *
     * @param bos - BufferedOutputStream to obj file
     */
    public void writeTriangleFaceOBJ( BufferedOutputStream bos )
    {
        try
        {
            String face = "f " + vertices[0].getIndex() + " " + vertices[1].getIndex() + " " +
                    vertices[2].getIndex() + "\n";
            // BufferedOutputStream bos = new BufferedOutputStream( fos );
            bos.write( face.getBytes() );
            //fos.write( face.getBytes() );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Writes data of face to supplied FileOutputStream (must be ply file)
     *
     * @param bos - BufferedOutputStream to ply file
     */
    public void writeTriangleFacePLY( BufferedOutputStream bos )
    {
        try
        {
            String face = "3 " + (vertices[0].getIndex() - 1) + " " + (vertices[1].getIndex() -
                    1) + " " +
                    (vertices[2].getIndex() - 1) + "\n";
            bos.write( face.getBytes() );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }


    /**
     * Array of 3 vertices of the triangle
     */
    private TriangleVertex[] vertices;

    /**
     * Normal vector to face
     */
    private TriangleVertex normal;

}
