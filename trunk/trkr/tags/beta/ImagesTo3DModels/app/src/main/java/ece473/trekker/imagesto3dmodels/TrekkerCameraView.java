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
