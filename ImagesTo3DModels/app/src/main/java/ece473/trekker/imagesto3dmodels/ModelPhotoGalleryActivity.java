package ece473.trekker.imagesto3dmodels;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ModelPhotoGalleryActivity extends ActionBarActivity
{

    private File modelImageDirectory;
    private String objectName;
    boolean delete = false;
    private ImageAdapter imgAdapter;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_model_photo_gallery );

        //LinearLayout layout = (LinearLayout) findViewById( R.id.photo_gallery_linear_layout );

        final Button openModelButton = (Button) findViewById( R.id.open_3D_model );

        modelImageDirectory = new File( getIntent().getStringExtra( "modelImageDirectory" ) );
        objectName = new String( getIntent().getStringExtra( "modelName" ) );
        String filePath = modelImageDirectory.getAbsolutePath().replace( "images",
                "" ) + "/" + objectName + ".ply";
        File objectFile = new File( filePath );
        if( objectFile.exists() )
        {
            openModelButton.setVisibility( View.VISIBLE );
            openModelButton.setEnabled( true );
        }
        /*
        File[] images = modelImageDirectory.listFiles();
        for( File f : images )
        {
            ImageView imageView = new ImageView( ModelPhotoGalleryActivity.this );
            imageView.setImageURI( Uri.parse( f.getAbsolutePath() ) );
            layout.addView( imageView );
        }
        */
        final GridView gridview = (GridView) findViewById( R.id.buttonGrid );
        imgAdapter = new ImageAdapter( this );
        gridview.setAdapter( imgAdapter );

        gridview.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            public void onItemClick( AdapterView<?> parent, View v, int position, long id )
            {
                Toast.makeText( getApplicationContext(), "" + position, Toast.LENGTH_SHORT ).show();

                //if( (position == imgAdapter.getCount() - 1) && (imgAdapter.getCount() != 6)  )
                if( v.getTag() == "plus" )
                {
                    initiateCapture( v );
                }
                else if( delete )
                {
                    deleteImage( (String) imgAdapter.getItem( position ).keySet().toArray()[0] );
                    delete = false;
                    imgAdapter.updateAdapter();
                }
                else{
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    String filename = (String) imgAdapter.getItem( position ).keySet().toArray()[0];
                    i.setDataAndType(Uri.parse("file://"+modelImageDirectory.getAbsolutePath()+"/"+filename),"image/jpeg");
                    startActivity(i);
                }
            }
        } );

    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync( OpenCVLoader.OPENCV_VERSION_2_4_6, this, loaderCallback );
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
            if( mThumbIds.size() < 6 )
            {
                return mThumbIds.size() + 1;
            }
            else
            {
                return 6;
            }
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
            options.inSampleSize = 2;
            List<Map<String, Bitmap>> thumbnails = new ArrayList<>();

            if( modelImageDirectory.exists() )
            {
                File[] files = modelImageDirectory.listFiles();
                for( File file : files )
                {
                    if( file.exists() )
                    {
                        String fileName = file.getName();
                        if( fileName.contains( "capture" ) )
                        {
                            Bitmap myBitmap = BitmapFactory.decodeFile( file.getAbsolutePath(),
                                    options );
                            String bitmapName = file.getName();
                            thumbnails.add( createBitmap( bitmapName, myBitmap ) );
                        }
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
//                imageView = new ImageView( mContext );


                imageView = new ImageView( mContext );
                imageView.setLayoutParams( new GridView.LayoutParams( 256, 256 ) );
                imageView.setScaleType( ImageView.ScaleType.CENTER_CROP );
                imageView.setPadding( 8, 8, 8, 8 );
            }
            else
            {
                imageView = (ImageView) convertView;
            }

            if( position == mThumbIds.size() && position < 6 )
            {
                imageView.setImageResource( R.drawable.plus );
                imageView.setTag( "plus" );
            }
            else if( position < mThumbIds.size() && position < 6 )
            {
                imageView.setImageBitmap( mThumbIds.get( position ).get( mThumbIds.get( position
                ).keySet().toArray()[0] ) );
                imageView.setTag( "image" );

            }
            return imageView;
        }

        public void updateAdapter()
        {
            mThumbIds = getThumbNails();
            notifyDataSetChanged();
        }

        // references to our images
        // private ArrayList<Bitmap> mThumbIds = getThumbNails();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        private List<Map<String, Bitmap>> mThumbIds = getThumbNails();


    }

    public void deleteImage( String filename )
    {

        File imageFile = new File( modelImageDirectory, filename );

        if( imageFile.exists() )
        {

            imageFile.delete();
        }
        else
        {

            Toast.makeText( getApplicationContext(), "Error Deleting!", Toast.LENGTH_SHORT ).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_model_photo_gallery, menu );
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
     * Called when create model button is pressed creates new 3D model from images
     *
     * @param view - create_model_button
     */
    public boolean createModel( View view )
    {
        if( imgAdapter.getThumbNails().size() == 6 )
        {
            final Button createButton = (Button) findViewById( R.id.create_model_button );
            final Button openModelButton = (Button) findViewById( R.id.open_3D_model );
            final ProgressBar progressBar = (ProgressBar) findViewById( R.id.progressBar );
            createButton.setEnabled( false );
            openModelButton.setEnabled( false );
            progressBar.setVisibility( View.VISIBLE );
            progressBar.setEnabled( true );
            Log.e( "createModel", "Model initiated" );
            Toast.makeText( MyApplication.getAppContext(), "Creating 3D Model...",
                    Toast.LENGTH_SHORT ).show();

            //create 3D model on separate thread to keep ui responsive
            new Thread( new Runnable()
            {
                @Override
                public void run()
                {
                    String name = getIntent().getStringExtra( "modelName" );
                    String directory = getIntent().getStringExtra( "modelImageDirectory" );
                    Object3DModel model = new Object3DModel( name, directory );
                    model.create3DModel();

                    //toast has to be run on the ui thread
                    runOnUiThread( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText( MyApplication.getAppContext(), "3D Model Complete!",
                                    Toast.LENGTH_LONG ).show();
                            Log.e( "createModel", "Model complete" );
                            createButton.setEnabled( true );
                            openModelButton.setVisibility( View.VISIBLE );
                            openModelButton.setEnabled( true );
                            progressBar.setVisibility( View.INVISIBLE );
                            progressBar.setEnabled( false );

                        }
                    } );
                }
            } ).start();

            return true;

        }
        else
        {
            int numImagesNeeded = 6 - imgAdapter.getCount() + 1;

            String imgString;
            if( numImagesNeeded > 1 )
            {
                imgString = " more Images.";
            }
            else
            {
                imgString = " more Image.";
            }

            Toast.makeText( MyApplication.getAppContext(), "Need " + Integer.toString(
                            numImagesNeeded ) + imgString,
                    Toast.LENGTH_LONG ).show();

            return false;
        }
    }

    public void open3DModel( View view )
    {
        Toast.makeText( MyApplication.getAppContext(), "Opening 3D Model...",
                Toast.LENGTH_SHORT ).show();

        String filePath = modelImageDirectory.getAbsolutePath().replace( "images",
                "" ) + "/" + objectName + ".ply";

        try
        {
            Intent myIntent = new Intent( android.content.Intent.ACTION_VIEW );
            File file = new File( filePath );
            String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl( Uri.fromFile(
                    file ).toString() );
            String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    extension );
            myIntent.setDataAndType( Uri.fromFile( file ), mimetype );
            startActivity( myIntent );
        }
        catch( Exception e )
        {
            // TODO: handle exception
            String data = e.getMessage();
        }

    }

    /**
     * Creates separate directory for model, then starts image capture activity
     *
     * @param view - initiate_capture_button
     */
    public void initiateCapture( View view )
    {

        Intent captureIntent = new Intent( this, ImageCaptureActivity.class );
        //sends name of model to image capture activity
        captureIntent.putExtra( "modelName", objectName );
        startActivity( captureIntent );
    }

    public void updateImgAdapter()
    {
        imgAdapter.updateAdapter();
    }


    /**
     * Callback that enables camera view
     */
    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback( this )
    {
        @Override
        public void onManagerConnected( int status )
        {
            switch( status )
            {
                default:
                {
                    super.onManagerConnected( status );
                }
                break;
            }
        }
    };

    public void onBackPressed()
    {
        Intent mainIntent = new Intent( this, MainMenuActivity.class );
        startActivity( mainIntent );
    }
}
