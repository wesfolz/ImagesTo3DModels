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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.util.Log;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;


public class MainMenuActivity extends ActionBarActivity
{


    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        //creates folder to store application files

        setContentView( R.layout.activity_main_menu );

        gridview = (GridView) findViewById( R.id.buttonGrid );
        imgAdapter = new ImageAdapter( this );
        gridview.setAdapter( imgAdapter );

        gridview.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            public void onItemClick( AdapterView<?> parent, View v, int position, long id )
            {
                //Toast.makeText( getApplicationContext(), "" + position,
                // Toast.LENGTH_SHORT ).show();

                if( v.getTag() == "plus" )
                {
                    final View view = v;
                    AlertDialog.Builder builder;
                    builder = new AlertDialog.Builder( MainMenuActivity.this );
                    builder.setTitle( "Model Name" );

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
                else if( share )
                {
                    shareModel( (String) imgAdapter.getItem( position ).keySet().toArray()[0] );
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

        public View getViewByPosition( int position )
        {
            return views.get( position );
        }

        public List<Map<String, Bitmap>> getThumbNails()
        {

//            ArrayList<Bitmap> thumbnails = new ArrayList<>();
            List<Map<String, Bitmap>> thumbnails = new ArrayList<Map<String, Bitmap>>();

            if( thmNailDir.exists() )
            {
                //options.inSampleSize = 2;
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
                imageView.setTag( "plus" );

            }
            else if( position < mThumbIds.size() )
            {
                imageView.setImageBitmap( mThumbIds.get( position ).get( mThumbIds.get( position
                ).keySet().toArray()[0] ) );
                imageView.setTag( "image" );
            }
            views.add( position, imageView );
            return imageView;
        }

        public void updateAdapter()
        {
            mThumbIds = getThumbNails();
            notifyDataSetChanged();
        }


        // references to our images
        //private ArrayList<Bitmap> mThumbIds = getThumbNails();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        private List<Map<String, Bitmap>> mThumbIds = getThumbNails();
        private ArrayList<View> views = new ArrayList<>();


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
        if( id == R.id.action_import )
        {
            importModel();
            share = false;
            delete = false;
            return true;
        }
        if( id == R.id.action_share )
        {
            Toast.makeText( getApplicationContext(), "Select model to share.",
                    Toast.LENGTH_SHORT ).show();
            share = true;
            delete = false;
            return true;
        }
        else if( id == R.id.action_delete )
        {
            Toast.makeText( getApplicationContext(), "Select model to delete.",
                    Toast.LENGTH_SHORT ).show();
            share = false;
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

    public static void DeleteRecursive( File fileOrDirectory )
    {
        if( fileOrDirectory.isDirectory() )
            for( File child : fileOrDirectory.listFiles() )
                DeleteRecursive( child );

        fileOrDirectory.delete();
    }

    /**
     * Starts an action send intent so that user can share models
     *
     * @param filename
     */
    public void shareModel( String filename )
    {
        share = false;
        String outputFilePath = appDir + "/" + filename + "/" + filename;
        //create intent to send multiple items
        emailDataIntent = new Intent( Intent.ACTION_SEND_MULTIPLE );
        //set mime type to email messages
        emailDataIntent.setType( "message/rfc822" );
        //add a subject to the email
        emailDataIntent.putExtra( Intent.EXTRA_SUBJECT, filename );
        ArrayList<Uri> uris = new ArrayList<>();
        File plyFile = new File( outputFilePath + ".ply" + ".gzip" );
        File objFile = new File( outputFilePath + ".obj" + ".gzip" );

        //attach ply file if it exists
        if( plyFile.exists() )
            uris.add( Uri.parse( "file://" + outputFilePath + ".ply" + ".gzip" ) );

        //attach obj file if it exists
        if( objFile.exists() )
            uris.add( Uri.parse( "file://" + outputFilePath + ".obj" + ".gzip" ) );
        //attach files to email if one or both exist
        if( uris.size() > 0 )
        {
            emailDataIntent.putParcelableArrayListExtra( Intent.EXTRA_STREAM, uris );
            //start email client
            startActivity( emailDataIntent );
        }
    }

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder( this )
                .setTitle( "Really Exit?" )
                .setMessage( "Are you sure you want to exit?" )
                .setNegativeButton( android.R.string.no, null )
                .setPositiveButton( android.R.string.yes, new DialogInterface.OnClickListener()
                {

                    public void onClick( DialogInterface arg0, int arg1 )
                    {
                        Intent intent = new Intent( Intent.ACTION_MAIN );
                        intent.addCategory( Intent.CATEGORY_HOME );
                        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                        startActivity( intent );
                    }
                } ).create().show();

    }

    private void importModel()
    {

        Dialog dialog = null;
        loadFileList();

        AlertDialog.Builder builder = new AlertDialog.Builder( this );

        builder.setTitle( "Choose your file" );
        if( mFileList == null )
        {
            Log.e( "WES IS COOL", "Showing file picker before loading the file list" );
            dialog = builder.create();
            return;
        }
        builder.setItems( mFileList, new DialogInterface.OnClickListener()
        {
            public void onClick( DialogInterface dialog, int which )
            {

                byte[] buffer = new byte[1024];
                int len;

                mChosenFile = downloadDir + "/" + mFileList[which];
                Log.e( "FileName", mChosenFile );

                try
                {
                    GZIPInputStream file2unzip = new GZIPInputStream( new FileInputStream(
                            mChosenFile ) );
                    FileOutputStream outfile = new FileOutputStream( mChosenFile.replace( "" +
                            ".gzip", "" ) );
                    while( (len = file2unzip.read( buffer )) > 0 )
                    {
                        outfile.write( buffer, 0, len );
                    }

                    file2unzip.close();
                    outfile.close();

                }
                catch( IOException e )
                {
                    e.printStackTrace();
                }

                Toast.makeText( MyApplication.getAppContext(), "Opening 3D Model...",
                        Toast.LENGTH_SHORT ).show();

                try
                {
                    Intent myIntent = new Intent( android.content.Intent.ACTION_VIEW );
                    File file = new File( mChosenFile.replace( ".gzip", "" ) );
                    String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl( Uri
                            .fromFile(
                            file ).toString() );
                    String mimetype = android.webkit.MimeTypeMap.getSingleton()
                            .getMimeTypeFromExtension(
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
        } ).create().show();


    }

    public ImageAdapter getImgAdapter()
    {
        return imgAdapter;
    }

    public GridView getGridview()
    {
        return gridview;
    }

    /**
     * String storing application file name
     */
    public static final String APPLICATION_DIRECTORY_NAME = "Images_To_3D_Models";
    public static final File appDir = createApplicationDirectory();
    public static final File thmNailDir = createDirectory( "thumbNails" );
    private GridView gridview;
    private String objectName;
    private ImageAdapter imgAdapter;

    Intent emailDataIntent;
    boolean delete = false;
    boolean share = false;

    //In an Activity
    private String[] mFileList;
    private String downloadDir = Environment.getExternalStoragePublicDirectory( Environment
            .DIRECTORY_DOWNLOADS ).getPath();
    private File mPath = new File( downloadDir );
    private String mChosenFile;
    private static final String FTYPE = ".gzip";

    private void loadFileList()
    {
        try
        {
            mPath.mkdirs();
        }
        catch( SecurityException e )
        {
            Log.e( "WES IS COOL", "unable to write on the sd card " + e.toString() );
        }
        if( mPath.exists() )
        {
            FilenameFilter filter = new FilenameFilter()
            {

                @Override
                public boolean accept( File dir, String filename )
                {
                    File sel = new File( dir, filename );
                    return filename.contains( FTYPE ) || sel.isDirectory();
                }

            };
            mFileList = mPath.list( filter );
        }
        else
        {
            mFileList = new String[0];
        }
    }
/*
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id) {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose your file");
                if(mFileList == null) {
                    Log.e("WES IS COOL", "Showing file picker before loading the file list");
                    dialog = builder.create();
                    return dialog;
                }
                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mChosenFile = mFileList[which];
                        //you can do stuff with the file here too
                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }*/

}
