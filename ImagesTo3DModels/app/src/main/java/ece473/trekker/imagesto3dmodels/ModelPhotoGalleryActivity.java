package ece473.trekker.imagesto3dmodels;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class ModelPhotoGalleryActivity extends ActionBarActivity
{

    private File modelImageDirectory;
    private String objectName;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_model_photo_gallery );

        //LinearLayout layout = (LinearLayout) findViewById( R.id.photo_gallery_linear_layout );


        modelImageDirectory = new File( getIntent().getStringExtra( "modelImageDirectory" ) );
        objectName = new String( getIntent().getStringExtra( "modelName" ) );

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
        gridview.setAdapter( new ImageAdapter( this ) );

        gridview.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            public void onItemClick( AdapterView<?> parent, View v, int position, long id )
            {
                Toast.makeText( getApplicationContext(), "" + position, Toast.LENGTH_SHORT ).show();

                if( position == parent.getAdapter().getCount() - 1 )
                {
                    initiateCapture( v );
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

        public Object getItem( int position )
        {
            return null;
        }

        public long getItemId( int position )
        {
            return 0;
        }

        public ArrayList<Bitmap> getThumbNails()
        {

            ArrayList<Bitmap> thumbnails = new ArrayList<>();

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
                            Bitmap myBitmap = BitmapFactory.decodeFile( file.getAbsolutePath() );
                            thumbnails.add( myBitmap );
                        }
                    }
                }
            }
            return thumbnails;
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
                imageView.setImageBitmap( mThumbIds.get( position ) );
            }

            return imageView;
        }

        // references to our images
        private ArrayList<Bitmap> mThumbIds = getThumbNails();

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

        return super.onOptionsItemSelected( item );
    }

    /**
     * Called when create model button is pressed creates new 3D model from images
     *
     * @param view - create_model_button
     */
    public void createModel( View view )
    {
        Log.e( "createModel", "Model initiated" );
        Toast.makeText( MyApplication.getAppContext(), "Creating 3D Model...",
                Toast.LENGTH_SHORT ).show();
        Object3DModel model = new Object3DModel( getIntent().getStringExtra( "modelName" ),
                getIntent().getStringExtra( "modelImageDirectory" ) );
        //   File cannyEdge = model.detectEdges();
        //      model.subtractBackground();
        //model.detectContours();
        //LinearLayout layout = (LinearLayout) findViewById( R.id.photo_gallery_linear_layout );
        //ImageView imageView = new ImageView( ModelPhotoGalleryActivity.this );
        //imageView.setImageURI( Uri.parse( noBackground.getAbsolutePath() ) );
        //layout.addView( imageView );
        //  imageView = new ImageView( ModelPhotoGalleryActivity.this );
        //  imageView.setImageURI( Uri.parse( cannyEdge.getAbsolutePath() ) );
        // layout.addView( imageView );

        Toast.makeText( MyApplication.getAppContext(), "3D Model Complete!",
                Toast.LENGTH_SHORT ).show();
        Log.e( "createModel", "Model complete" );
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


}
