package com.screenshot.jayashanf.videorec;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by jayashanF on 8/30/17.
 */
public class RecordTextYiew extends TextureView {


    public RecordTextYiew(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }
}
