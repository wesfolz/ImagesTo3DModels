/*
 * Copyright (c) 2015 Wesley Folz, Ryan Hoefferle
 *
 * This file is part of Images to 3D Models.
 *
 * Images to 3D Models is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Images to 3D Models is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Images to 3D Models.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
