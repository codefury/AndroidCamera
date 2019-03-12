package com.codefury16.androidcamera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewThin extends TextView {
    public TextViewThin(Context context) {
        super(context);
        setTypeface(Utils.getRobotoThin(context));
    }

    public TextViewThin(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Utils.getRobotoThin(context));
    }

    public TextViewThin(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(Utils.getRobotoThin(context));
    }

}
