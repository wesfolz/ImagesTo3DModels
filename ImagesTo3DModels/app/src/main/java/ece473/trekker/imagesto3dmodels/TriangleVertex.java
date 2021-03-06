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

    /**
     * Creates a triangle vertex on the correct plane
     *
     * @param face      - face of object (front, right, back, left, top, bottom) (0,1,2,3,4,5)
     * @param row       - current row image Mat
     * @param column    - current column of image Mat
     * @param maxRow    - height of image Mat
     * @param maxColumn - width of image Mat
     * @param depth     - depth point of vertex
     * @return - a new TriangleVertex
     */
    public static TriangleVertex buildTriangleVertex( int face, int row, int column, int maxRow,
                                                      int maxColumn, double depth )
    {
        switch( face )
        {
            case Object3DModel.FACE_FRONT:
                return new TriangleVertex( column, row, depth ); //return new TriangleVertex(
                // column, row, 0 );

            case Object3DModel.FACE_RIGHT:
                return new TriangleVertex( depth, row, column ); //return new TriangleVertex(
            // 0, row, column );

            case Object3DModel.FACE_BACK:
                return new TriangleVertex( maxColumn - column, row,
                        depth ); //return new TriangleVertex( maxColumn - column, row, 0 );

            case Object3DModel.FACE_LEFT:
                return new TriangleVertex( depth, row, maxColumn - column ); //return new TriangleVertex( 0, row, maxColumn - column );

            case Object3DModel.FACE_TOP:
                return new TriangleVertex( column, depth, maxRow - row ); //return new TriangleVertex( column, 0, row );

            case Object3DModel.FACE_BOTTOM:
                return new TriangleVertex( column, depth, row ); //return new TriangleVertex( column, 0, row );

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
