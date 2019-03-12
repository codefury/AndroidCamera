package com.codefury16.androidcamera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewLight extends TextView {
    public TextViewLight(Context context) {
        super(context);
        setTypeface(Utils.getRobotoLight(context));
    }

    public TextViewLight(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Utils.getRobotoLight(context));
    }

    public TextViewLight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(Utils.getRobotoLight(context));
    }

}
