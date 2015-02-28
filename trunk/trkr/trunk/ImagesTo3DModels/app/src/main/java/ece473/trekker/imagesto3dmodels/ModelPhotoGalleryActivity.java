package ece473.trekker.imagesto3dmodels;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;


public class ModelPhotoGalleryActivity extends ActionBarActivity
{

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_model_photo_gallery );
        LinearLayout layout = (LinearLayout) findViewById( R.id.photo_gallery_linear_layout );
        File modelImageDirectory = new File( getIntent().getStringExtra( "modelImageDirectory" ) );
        File[] images = modelImageDirectory.listFiles();
        for( File f : images )
        {
            ImageView imageView = new ImageView( ModelPhotoGalleryActivity.this );
            imageView.setImageURI( Uri.parse( f.getAbsolutePath() ) );
            layout.addView( imageView );
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

        return super.onOptionsItemSelected( item );
    }

    /**
     * Called when create model button is pressed creates new 3D model from images
     *
     * @param view - create_model_button
     */
    public void createModel( View view )
    {
        new Object3DModel( getIntent().getStringExtra( "modelName" ),
                getIntent().getStringExtra( "modelImageDirectory" ) );
    }


}
