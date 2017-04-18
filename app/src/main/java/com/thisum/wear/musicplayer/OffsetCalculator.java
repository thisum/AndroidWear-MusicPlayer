package com.thisum.wear.musicplayer;

import android.support.wearable.view.DefaultOffsettingHelper;
import android.support.wearable.view.WearableRecyclerView;
import android.view.View;

/**
 * Created by thisum on 3/28/2017.
 */

public class OffsetCalculator extends DefaultOffsettingHelper
{
    private static final float MAX_ICON_PROGRESS = 1.f;

    private float mProgressToCenter;

    public OffsetCalculator()
    {
    }

    @Override
    public void updateChild( View child, WearableRecyclerView parent )
    {
        super.updateChild( child, parent );

        float centerOffset = ( ( float ) child.getHeight() / 2.0f ) / ( float ) parent.getHeight();
        float yRelativeToCenterOffset = ( child.getY() / parent.getHeight() ) + centerOffset;

        mProgressToCenter = Math.abs( 0.5f - yRelativeToCenterOffset );
        mProgressToCenter = Math.min( mProgressToCenter, MAX_ICON_PROGRESS );

        child.setScaleX( 1 - mProgressToCenter );
        child.setScaleY( 1 - mProgressToCenter );
    }
}