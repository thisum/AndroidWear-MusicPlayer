package com.thisum.wear.musicplayer;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WearableRecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.thisum.wear.musicplayer.util.MusicPlayerUtil;

import java.util.List;


public class MusicActivity extends WearableActivity implements View.OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, NotificationListener
{
    public static final String TAG = MusicActivity.class.getSimpleName();
    public static final int EAD_EXTERNAL_STORAGE_PERMISSION_CODE = 10000;

    private WearableRecyclerView playListView;
    private ImageView mPlayPause;
    private ImageView mSkipNext;
    private ImageView mSkipPrev;
    private ImageButton volBtn;
    private TextView mClockView;
    private TextView mStart;
    private TextView mEnd;
    private SeekBar mSeekbar;
    private PlayListAdaptor playListAdaptor;
    private Bitmap play;
    private Bitmap pause;

    private AudioManager am;
    private MediaPlayer mediaPlayer;
    private View playingSong = null;
    private boolean userSeeked = false;
    private boolean userVolumeSeeked = false;
    private final Handler mHandler = new Handler();
    private Dialog volumeDialog;
    private SeekBar volumeSeekBar;


    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.play_main );
        setAmbientEnabled();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType( AudioManager.STREAM_MUSIC );

        am = ( AudioManager ) getSystemService( Context.AUDIO_SERVICE );

        playListView = ( WearableRecyclerView ) findViewById( R.id.play_list );
        playListAdaptor = new PlayListAdaptor( this );
        playListView.setAdapter( playListAdaptor );
        playListView.setCenterEdgeItems( true );
        playListView.setOffsettingHelper( new OffsetCalculator() );
        playListView.setCircularScrollingGestureEnabled( true );
        playListView.setBezelWidth( 0.8f );
        playListView.setScrollDegreesPerScreen( 60 );

        mPlayPause = ( ImageView ) findViewById( R.id.play_pause );
        mSkipNext = ( ImageView ) findViewById( R.id.next );
        mSkipPrev = ( ImageView ) findViewById( R.id.prev );
        mStart = ( TextView ) findViewById( R.id.startText );
        mEnd = ( TextView ) findViewById( R.id.endText );
        mSeekbar = ( SeekBar ) findViewById( R.id.seekBar );
        volBtn = ( ImageButton ) findViewById( R.id.volButton );

        play = BitmapFactory.decodeResource( getResources(), R.drawable.ic_play );
        pause = BitmapFactory.decodeResource( getResources(), R.drawable.ic_pause );

        setListeners();
        setupVolumeDialog();
    }

    private void setupVolumeDialog()
    {
        volumeDialog = new Dialog( this );
        volumeDialog.setContentView( R.layout.volume_change );
        volumeSeekBar = ( SeekBar ) volumeDialog.findViewById( R.id.volume_changer );
        volumeSeekBar.setMax( am.getStreamMaxVolume( AudioManager.STREAM_MUSIC ) );
        Window window = volumeDialog.getWindow();
        window.setLayout( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT );
        volumeSeekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
            {
                if( userVolumeSeeked )
                {
                    am.setStreamVolume( AudioManager.STREAM_MUSIC, progress, 0 );
                }
            }

            @Override
            public void onStartTrackingTouch( SeekBar seekBar )
            {
                userVolumeSeeked = true;
            }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar )
            {
                userVolumeSeeked = false;
            }
        } );
    }

    private void setListeners()
    {
        mPlayPause.setOnClickListener( this );
        mSkipNext.setOnClickListener( this );
        mSkipPrev.setOnClickListener( this );
        volBtn.setOnClickListener( this );

        mediaPlayer.setOnCompletionListener( this );
        mediaPlayer.setOnPreparedListener( this );

        setSeekbarListener();
    }

    private void setSeekbarListener()
    {
        mSeekbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
            {
                if( userSeeked )
                {
                    mediaPlayer.seekTo( progress );
                }
            }

            @Override
            public void onStartTrackingTouch( SeekBar seekBar )
            {
                userSeeked = true;
            }

            @Override
            public void onStopTrackingTouch( SeekBar seekBar )
            {
                userSeeked = false;
            }
        } );
    }

    @Override
    public void onClick( View v )
    {
        if( v == mPlayPause )
        {
            playPauseSong();
        }
        else if( v == mSkipNext )
        {
            findAndPlaySong( true );
        }
        else if( v == mSkipPrev )
        {
            findAndPlaySong( false );
        }
        if( v == volBtn )
        {
            showVolumeChange();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        playingSong = null;
        boolean openNotification = getIntent().getBooleanExtra( Constants.EXTRA_OPEN_MUSIC, false );
        if( openNotification )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON );
        }
        if( hasPermissionGranted() )
        {
            SongsLoadingTask loadingTask = new SongsLoadingTask( this, this );
            loadingTask.execute();
        }
    }

    @Override
    protected void onDestroy()
    {
        mediaPlayer.release();
        mediaPlayer = null;
        mHandler.removeCallbacks( seekBarUpdator );
        super.onDestroy();
    }

    private boolean hasPermissionGranted()
    {
        if( checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE ) != PackageManager.PERMISSION_GRANTED )
        {
            requestPermissions( new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EAD_EXTERNAL_STORAGE_PERMISSION_CODE );
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults )
    {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        if( requestCode == EAD_EXTERNAL_STORAGE_PERMISSION_CODE && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED )
        {
            SongsLoadingTask loadingTask = new SongsLoadingTask( this, this );
            loadingTask.execute();
        }
    }

    private void playPauseSong()
    {
        if( mediaPlayer.isPlaying() )
        {
            mediaPlayer.pause();
            mPlayPause.setImageBitmap( play );
        }
        else
        {
            if( playingSong == null )
            {
                startSong( playListAdaptor.getSong( playListAdaptor.getCurrentPosition() ) );
            }
            else
            {
                mediaPlayer.start();
                mPlayPause.setImageBitmap( pause );
            }
        }
    }

    private void findAndPlaySong( boolean next )
    {
        int item = 0;
        if( next )
        {
            item = ( ( playListAdaptor.getCurrentPosition() + 1 ) == playListAdaptor.getItemCount() ) ? 0 : playListAdaptor.getCurrentPosition() + 1;
        }
        else
        {
            item = ( playListAdaptor.getCurrentPosition() == 0 ) ? playListAdaptor.getItemCount() - 1 : playListAdaptor.getCurrentPosition() - 1;
        }
        if( mediaPlayer.isPlaying() )
        {
            mediaPlayer.reset();
        }
        playListAdaptor.setCurrentPosition( item );
        startSong( playListAdaptor.getSong( item ) );
        final int playableItem = item;
        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                playListView.scrollToPosition( playableItem );
            }
        } );
    }


    @Override
    public void onPrepared( MediaPlayer mp )
    {
        mSeekbar.setMax( mp.getDuration() );
        mStart.setText( "00.00" );
        mEnd.setText( MusicPlayerUtil.getFormattedTime( mp.getDuration() ) );
    }

    @Override
    public void notifyResults( final List<Song> songs )
    {
        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                playListAdaptor.setSongs( songs );
                playListAdaptor.notifyDataSetChanged();
            }
        } );
    }

    public void playSong( View view, Song song )
    {
        updateUI( view );
        startSong( song );
    }

    private void updateUI( final View view )
    {
        if( playingSong == null )
        {
            runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    playingSong = view;
                }
            } );
        }
        else
        {
            runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    mediaPlayer.reset();
                    playingSong = view;
                }
            } );
        }
    }

    private void startSong( Song song )
    {
        try
        {
            AssetFileDescriptor afd = getAssets().openFd( song.getFile() );
            mediaPlayer.setDataSource( afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength() );
            mediaPlayer.prepare();
            mediaPlayer.start();
            mHandler.postDelayed( seekBarUpdator, 1000 );
            runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    mPlayPause.setImageBitmap( pause );
                }
            } );
        }
        catch( Exception e )
        {
            runOnUiThread( new Runnable()
            {
                @Override
                public void run()
                {
                    mPlayPause.setImageBitmap( play );
                }
            } );
        }
    }

    private Runnable seekBarUpdator = new Runnable()
    {
        public void run()
        {
            if( mSeekbar != null && mediaPlayer != null )
            {
                final int mCurrentPosition = mediaPlayer.getCurrentPosition();
                mStart.setText( MusicPlayerUtil.getFormattedTime( mCurrentPosition ) );
                mEnd.setText( MusicPlayerUtil.getFormattedTime( mediaPlayer.getDuration() - mCurrentPosition ) );
                mSeekbar.setProgress( mCurrentPosition );

                mHandler.postDelayed( this, 1000 );
            }
        }
    };

    @Override
    public void onCompletion( MediaPlayer mp )
    {
        findAndPlaySong( true );
    }


    private void showVolumeChange()
    {
        volumeSeekBar.setProgress( am.getStreamVolume( AudioManager.STREAM_MUSIC ) );
        volumeDialog.show();
    }

    private void seekBarUpdate()
    {
        runOnUiThread( new Runnable()
        {
            @Override
            public void run()
            {
                mediaPlayer.seekTo( mediaPlayer.getCurrentPosition() + 5000 );
                mSeekbar.setProgress( mediaPlayer.getCurrentPosition() );
            }
        } );
    }
}
