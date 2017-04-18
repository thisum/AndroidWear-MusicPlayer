package com.thisum.wear.musicplayer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thisum on 3/27/2017.
 */

public class PlayListAdaptor extends RecyclerView.Adapter
{
    private final LayoutInflater layoutInflater;
    private List<Song> songs = new ArrayList<>();
    private MusicActivity activity;
    private int currentPosition = 0;

    public PlayListAdaptor( MusicActivity activity )
    {
        this.activity = activity;
        this.layoutInflater = LayoutInflater.from( activity);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup viewGroup, int viewType )
    {
        return new ViewHolder( layoutInflater.inflate( R.layout.play_list_item, null ) );
    }

    @Override
    public void onBindViewHolder( RecyclerView.ViewHolder holder, int position )
    {
        ViewHolder viewHolder = ((ViewHolder)holder);
        Song song = songs.get( position );
        viewHolder.song = song;
        viewHolder.id = position;
        viewHolder.iconView.setImageBitmap( song.getImage() );
        viewHolder.titleView.setText( song.getTitle() );
        viewHolder.artistView.setText( song.getArtist() );
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount()
    {
        return songs.size();
    }

    public void setSongs( List<Song> songs )
    {
        this.songs = songs;
    }

    public int getCurrentPosition()
    {
        return this.currentPosition;
    }

    public void setCurrentPosition( int currentPosition )
    {
        this.currentPosition = currentPosition;
    }

    public Song getSong( int position )
    {
        return this.songs.get( position );
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public final ImageView iconView;
        public final TextView titleView;
        public final TextView artistView;

        private int id = -1;
        private Song song = null;

        public ViewHolder( View view )
        {
            super(view);

            view.setOnClickListener( this );
            iconView = ( ImageView ) view.findViewById( R.id.list_item_icon );
            titleView = ( TextView ) view.findViewById( R.id.list_item_title );
            artistView = ( TextView ) view.findViewById( R.id.list_item_artist );
        }

        @Override
        public void onClick( View v )
        {
            currentPosition = id;
            activity.playSong( v, song );
        }
    }
}
