package ece473.trekker.imagesto3dmodels;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainMenuActivity extends ActionBarActivity
{


    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        //creates folder to store application files

        setContentView( R.layout.activity_main_menu );

        final GridView gridview = (GridView) findViewById( R.id.buttonGrid );
        imgAdapter = new ImageAdapter( this );
        gridview.setAdapter( imgAdapter );

        gridview.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            public void onItemClick( AdapterView<?> parent, View v, int position, long id )
            {
                Toast.makeText( getApplicationContext(), "" + position, Toast.LENGTH_SHORT ).show();

                if( position == parent.getAdapter().getCount() - 1 )
                {
                    final View view = v;
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder( MainMenuActivity.this );
                    builder.setTitle( "Name Object" );

                    // Set up the input
                    final EditText input = new EditText( MainMenuActivity.this );
                    // Specify the type of input expected; this, for example,
                    // sets the input as a password, and will mask the text
                    input.setInputType( InputType.TYPE_CLASS_TEXT );
                    builder.setView( input );

                    // Set up the buttons
                    builder.setPositiveButton( "OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialog, int which )
                        {
                            objectName = input.getText().toString();
                            initiateCapture( view );
                        }
                    } );
                    builder.setNegativeButton( "Cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialog, int which )
                        {
                            dialog.cancel();
                        }
                    } );
                    builder.show();
                }
                else if( delete )
                {
                    deleteObject( (String) imgAdapter.getItem( position ).keySet().toArray()[0] );
                    delete = false;
                    imgAdapter.updateAdapter();
                }
                else
                {
                    openPhotoGallery( v, (String) imgAdapter.getItem( position ).keySet().toArray
                            ()[0] );
                }
            }
        } );

    }

    /**
     *
     */
    public class ImageAdapter extends BaseAdapter
    {
        private Context mContext;

        public ImageAdapter( Context c )
        {
            mContext = c;
        }

        public int getCount()
        {
            return mThumbIds.size() + 1;
        }

        @Override
        public Map<String, Bitmap> getItem( int position )
        {

            return mThumbIds.get( position );
        }

        public long getItemId( int position )
        {
            return 0;
        }

        public List<Map<String, Bitmap>> getThumbNails()
        {

//            ArrayList<Bitmap> thumbnails = new ArrayList<>();
            List<Map<String, Bitmap>> thumbnails = new ArrayList<Map<String, Bitmap>>();

            if( thmNailDir.exists() )
            {
                File[] files = thmNailDir.listFiles();
                for( File file : files )
                {
                    if( file.exists() )
                    {
                        Bitmap myBitmap = BitmapFactory.decodeFile( file.getAbsolutePath() );
                        String bitmapName = file.getName().replace( ".png", "" );
                        //thumbnails.add(myBitmap);
                        thumbnails.add( createBitmap( bitmapName, myBitmap ) );

                    }
                }
            }
            return thumbnails;
        }

        public HashMap<String, Bitmap> createBitmap( String name, Bitmap bitmap )
        {
            HashMap<String, Bitmap> bitmapHash = new HashMap<String, Bitmap>();
            bitmapHash.put( name, bitmap );
            return bitmapHash;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView( int position, View convertView, ViewGroup parent )
        {
            ImageView imageView;
            if( convertView == null )
            {  // if it's not recycled, initialize some attributes
                imageView = new ImageView( mContext );
                imageView.setLayoutParams( new GridView.LayoutParams( 256, 256 ) );
                imageView.setScaleType( ImageView.ScaleType.CENTER_CROP );
                imageView.setPadding( 8, 8, 8, 8 );
            }
            else
            {
                imageView = (ImageView) convertView;
            }

            if( position == mThumbIds.size() )
            {
                imageView.setImageResource( R.drawable.plus );
            }
            else if( position < mThumbIds.size() )
            {
                //imageView.setImageBitmap(mThumbIds.get(position));
                imageView.setImageBitmap( mThumbIds.get( position ).get( mThumbIds.get( position
                ).keySet().toArray()[0] ) );
            }

            return imageView;
        }

        public void updateAdapter()
        {
            mThumbIds = getThumbNails();
            notifyDataSetChanged();
        }

        // references to our images
        //private ArrayList<Bitmap> mThumbIds = getThumbNails();
        private List<Map<String, Bitmap>> mThumbIds = getThumbNails();


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
        else if( id == R.id.action_delete )
        {

            delete = true;
            return true;
        }
        return super.onOptionsItemSelected( item );
    }


    /**
     * Creates application directory if it doesn't already exist
     */
    private static File createApplicationDirectory()
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
            return applicationDirectory;
        }
        //indicate external storage is not mounted
        else
        {
            Toast.makeText( MyApplication.getAppContext(), "Error! External Storage Not Mounted.",
                    Toast.LENGTH_LONG ).show();
            return null;
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
            File directory = new File( appDir.getPath() + "/" + directoryName );
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
            Toast.makeText( MyApplication.getAppContext(), "Error! External Storage Not Mounted.",
                    Toast.LENGTH_LONG ).show();
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
        createDirectory( objectName );

        Intent captureIntent = new Intent( this, ImageCaptureActivity.class );
        //sends name of model to image capture activity
        captureIntent.putExtra( "modelName", objectName );
        startActivity( captureIntent );
    }

    public void openPhotoGallery( View view, String modelName )
    {
        Intent galleryIntent = new Intent( this, ModelPhotoGalleryActivity.class );
        galleryIntent.putExtra( "modelName", modelName );
        galleryIntent.putExtra( "modelImageDirectory", getImageDirectory( modelName ) );
        startActivity( galleryIntent );
    }

    public String getImageDirectory( String modelName )
    {

        File dir = new File( appDir, modelName );
        try
        {
            return dir.getAbsolutePath() + "/images";
        }
        catch( Exception ex )
        {
            throw ex;
        }
    }


    public void deleteObject( String filename )
    {

        File dir = new File( appDir, filename );
        File thmFile = new File( thmNailDir, filename + ".png" );

        if( dir.exists() )
        {

            DeleteRecursive( dir );
            thmFile.delete();
        }
        else
        {
            Toast.makeText( getApplicationContext(), "Error Deleting!", Toast.LENGTH_SHORT ).show();
        }

    }

    void DeleteRecursive( File fileOrDirectory )
    {
        if( fileOrDirectory.isDirectory() )
            for( File child : fileOrDirectory.listFiles() )
                DeleteRecursive( child );

        fileOrDirectory.delete();
    }

    /**
     * String storing application file name
     */
    public static final String APPLICATION_DIRECTORY_NAME = "Images_To_3D_Models";
    public static final File appDir = createApplicationDirectory();
    public static final File thmNailDir = createDirectory( "thumbNails" );
    private String objectName;
    private ImageAdapter imgAdapter;

    boolean delete = false;

}