package com.codefury16.androidcamera;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;

class Utils {
    private static final String FONT_ROBOTO_LIGHT = "fonts/Roboto-Light.ttf";
    private static final String FONT_ROBOTO_REG = "fonts/Roboto-Regular.ttf";
    private static final String FONT_ROBOTO_THIN = "fonts/Roboto-Thin.ttf";

    static Typeface getRobotoLight(Context context) {
        return Typeface.createFromAsset(context.getAssets(), FONT_ROBOTO_LIGHT);
    }

    static Typeface getRobotoRegular(Context context) {
        return Typeface.createFromAsset(context.getAssets(), FONT_ROBOTO_REG);
    }

    static Typeface getRobotoThin(Context context) {
        return Typeface.createFromAsset(context.getAssets(), FONT_ROBOTO_THIN);
    }

    static Snackbar showSnackBar(AppCompatActivity appCompatActivity, String message) {
        return showSnackbar(appCompatActivity, message, "CLOSE", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    static Snackbar showSnackBar(AppCompatActivity appCompatActivity, String message, String action) {
        return showSnackbar(appCompatActivity, message, action, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private static Snackbar showSnackbar(AppCompatActivity appCompatActivity, String message, String action, View.OnClickListener listener) {
        Snackbar snackbar = Snackbar.make(appCompatActivity.getWindow().getDecorView().findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.setAction(action, listener);
        snackbar.setText(message);
        snackbar.setActionTextColor(Color.WHITE);
        snackbar.show();
        return snackbar;
    }

}
