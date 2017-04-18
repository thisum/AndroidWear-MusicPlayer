package com.thisum.wear.musicplayer;

import android.graphics.Bitmap;

/**
 * Created by thisum on 3/28/2017.
 */

public class Song
{
    private final Bitmap image;
    private final Bitmap defaultIcon;
    private final String title;
    private final String artist;
    private final String album;
    private final String file;

    public Song( Bitmap image, Bitmap defaultIcon, String title, String artist, String album, String file )
    {
        this.image = image;
        this.defaultIcon = defaultIcon;
        this.title = title;
        this.artist = artist;
        this.album =album;
        this.file = file;
    }

    public Bitmap getImage()
    {
        return image;
    }

    public Bitmap getDefaultIcon()
    {
        return defaultIcon;
    }

    public String getTitle()
    {
        return title;
    }

    public String getArtist()
    {
        return artist;
    }

    public String getAlbum()
    {
        return album;
    }

    public String getFile()
    {
        return file;
    }
}
