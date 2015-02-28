package ece473.trekker.imagesto3dmodels;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;


public class MainMenuActivity extends ActionBarActivity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main_menu );

        //creates folder to store application files
        createApplicationDirectory();
    }


    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_main_menu, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if( id == R.id.action_settings )
        {
            return true;
        }

        return super.onOptionsItemSelected( item );
    }

    /**
     * Creates application directory if it doesn't already exist
     */
    private void createApplicationDirectory()
    {
        //ensure external storage is mounted
        if( Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() ) )
        {
            //create new folder in the external storage directory
            File applicationDirectory = new File( Environment.getExternalStorageDirectory()
                    .toString() + "/" + APPLICATION_DIRECTORY_NAME );
            //make the directory if one doesn't already exist
            if( ! applicationDirectory.exists() )
            {
                applicationDirectory.mkdir();
            }
        }
        //indicate external storage is not mounted
        else
        {
            Toast.makeText( getApplicationContext(), "Error! External Storage Not Mounted.",
                    Toast.LENGTH_LONG ).show();
        }
    }

    /**
     * Creates directory if it doesn't already exist
     *
     * @param directoryName name of the directory
     */
    public static File createDirectory( String directoryName )
    {
        //ensure external storage is mounted
        if( Environment.MEDIA_MOUNTED.equals( Environment.getExternalStorageState() ) )
        {
            //create new folder in the external storage directory
            File directory = new File( Environment.getExternalStorageDirectory().toString() + "/"
                    + APPLICATION_DIRECTORY_NAME + "/" + directoryName );
            //make the directory if one doesn't already exist
            if( ! directory.exists() )
            {
                directory.mkdir();
            }
            return directory;
        }
        //indicate external storage is not mounted
        else
        {
            //    Toast.makeText( getApplicationContext(), "Error! External Storage Not Mounted.",
            //           Toast.LENGTH_LONG ).show();
            return null;
        }
    }

    /**
     * Creates separate directory for model, then starts image capture activity
     *
     * @param view - initiate_capture_button
     */
    public void initiateCapture( View view )
    {
        //create directory for model
        EditText modelNameText = (EditText) findViewById( R.id.model_name );
        createDirectory( modelNameText.getText().toString() );

        Intent captureIntent = new Intent( this, ImageCaptureActivity.class );
        //sends name of model to image capture activity
        captureIntent.putExtra( "modelName", modelNameText.getText().toString() );
        startActivity( captureIntent );
    }

    /**
     * String storing application file name
     */
    public static final String APPLICATION_DIRECTORY_NAME = "Images_To_3D_Models";
}
