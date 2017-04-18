package com.thisum.wear.musicplayer;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thisum on 3/28/2017.
 */

public class SongsLoadingTask extends AsyncTask<Object, Object, List<Song>>
{
    private final NotificationListener listener;
    private int imagesSize;
    private final Context context;
    private Bitmap defaultIcon;

    public SongsLoadingTask( Context context, NotificationListener listener )
    {
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected List<Song> doInBackground( Object... params )
    {
        List<Song> songList = new ArrayList<>();
        try
        {
            this.imagesSize = ( int ) this.context.getResources().getDimension( R.dimen.songImageSize );
            this.defaultIcon = BitmapFactory.decodeResource( this.context.getResources(), R.drawable.ic_default_art );

            String path = "";
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            AssetManager assets = this.context.getAssets();
            String[] files = assets.list( path );

            for( String file : files )
            {
                AssetFileDescriptor afd = assets.openFd( file );
                metadataRetriever.setDataSource( afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength() );

                String title = metadataRetriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_TITLE );
                String artist = metadataRetriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_ARTIST );
                String album = metadataRetriever.extractMetadata( MediaMetadataRetriever.METADATA_KEY_ALBUM );

                byte[] imageBytes = metadataRetriever.getEmbeddedPicture();
                Bitmap image = null;
                if( imageBytes != null )
                {
                    image = BitmapFactory.decodeByteArray( imageBytes, 0, imageBytes.length );
                }
                else
                {
                    image = defaultIcon;
                }
                image = getScaledImage( image );
                songList.add( new Song( image, defaultIcon, title, artist, album, file ) );

                afd.close();
            }
        }
        catch( Exception e )
        {

        }
        return songList;
    }

    @Override
    protected void onPostExecute( List<Song> songs )
    {
        listener.notifyResults( songs );
    }

    private Bitmap getScaledImage( Bitmap image )
    {
        return Bitmap.createScaledBitmap( image, imagesSize, imagesSize, true );
    }
}
