package com.thisum.wear.musicplayer.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;

/**
 * Created by thisum on 3/27/2017.
 */

public class MusicPlayerUtil
{
    public static String getFormattedTime( int milliseconds )
    {
        String ret = "";
        int seconds = ( milliseconds / 1000 ) % 60;
        int minutes = ( ( milliseconds / ( 1000 * 60 ) ) % 60 );
        ret += minutes < 10 ? "0" + minutes + ":" : minutes + ":";
        ret += seconds < 10 ? "0" + seconds : seconds + "";
        return ret;
    }

    public static boolean hasSoundIntegrated( Context context )
    {
        PackageManager packageManager = context.getPackageManager();
        AudioManager audioManager = ( AudioManager ) context.getSystemService( Context.AUDIO_SERVICE );

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
        {
            if( !packageManager.hasSystemFeature( PackageManager.FEATURE_AUDIO_OUTPUT ) )
            {
                return false;
            }

            AudioDeviceInfo[] devices = audioManager.getDevices( AudioManager.GET_DEVICES_OUTPUTS );
            for( AudioDeviceInfo device : devices )
            {
                if( device.getType() == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER )
                {
                    return true;
                }
            }
        }
        return false;
    }
}
