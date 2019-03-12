package com.codefury16.androidcamera;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;


public class TextViewRegular extends TextView {
    public TextViewRegular(Context context) {
        super(context);
        setTypeface(Utils.getRobotoRegular(context));
    }

    public TextViewRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(Utils.getRobotoRegular(context));
    }

    public TextViewRegular(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeface(Utils.getRobotoRegular(context));
    }

}
