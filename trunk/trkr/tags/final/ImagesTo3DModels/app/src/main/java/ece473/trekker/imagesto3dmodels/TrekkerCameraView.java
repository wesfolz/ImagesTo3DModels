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

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

/**
 * Created by wesley on 3/15/2015.
 */
public class TrekkerCameraView extends JavaCameraView
{
    public TrekkerCameraView(Context context, AttributeSet attrs)
    {
        super( context, attrs );
    }

    public void setFlashMode()
    {
        Camera.Parameters param = mCamera.getParameters();
        if(flash)
        {
            param.setFlashMode( Camera.Parameters.FLASH_MODE_OFF );
            flash = false;
        }
        else
        {
            param.setFlashMode( Camera.Parameters.FLASH_MODE_TORCH );
            flash = true;
        }
        mCamera.setParameters( param );
        mCamera.startPreview();
    }

    public void setCameraOrientation()
    {
        //mCamera.stopPreview();
        mCamera.setDisplayOrientation(180);
       // mCamera.startPreview();
    }

    private boolean flash;


}
