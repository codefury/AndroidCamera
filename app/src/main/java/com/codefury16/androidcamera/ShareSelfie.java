package com.codefury16.androidcamera;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class ShareSelfie extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shareselfie);
        ImageView imageView = findViewById(R.id.imageView3);
        Uri shareUri = getIntent().getData();
        /*BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(shareUri.getPath(), options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        imageView.getLayoutParams().height = imageHeight;
        imageView.getLayoutParams().width = imageWidth;
        */
        imageView.setImageURI(shareUri);
        Toolbar toolbar = findViewById(R.id.app_bar);
        toolbar.setTitle("Share");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.sharewhatsapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri shareUri = getIntent().getData();
                Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
                whatsappIntent.setType("image/jpeg");
                whatsappIntent.setPackage("com.whatsapp");
                whatsappIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
                whatsappIntent.putExtra(Intent.EXTRA_TEXT,
                        "#CodeFury16");
                try {
                    startActivity(whatsappIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Utils.showSnackBar(ShareSelfie.this, "Whatsapp is not installed.", "OK");
                }
            }
        });
        findViewById(R.id.sharemore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri shareUri = getIntent().getData();
                String s = "file://"+shareUri.toString();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("image/jpeg");
                sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(s));
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "#CodeFury16");
                startActivity(Intent.createChooser(sendIntent, "Share to"));
            }
        });
        findViewById(R.id.sharefb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri shareUri = getIntent().getData();
                String s = "file://"+shareUri.toString();
                Intent fbIntent = new Intent(Intent.ACTION_SEND);
                fbIntent.setType("image/jpeg");
                fbIntent.setPackage("com.facebook.katana");
                fbIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(s));
                fbIntent.putExtra(Intent.EXTRA_TEXT,
                        "#CodeFury16");
                try {
                    startActivity(fbIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Utils.showSnackBar(ShareSelfie.this, "Facebook is not installed.", "OK");
                }
            }
        });
        findViewById(R.id.shareinsta).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri shareUri = getIntent().getData();
                String s = "file://"+shareUri.toString();
                Intent instaIntent = new Intent(Intent.ACTION_SEND);
                instaIntent.setType("image/*");
                instaIntent.setPackage("com.instagram.android");
                instaIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(s));
                instaIntent.putExtra(Intent.EXTRA_TEXT,
                        "#CodeFury16");
                Log.d("INSTAGRAM", shareUri.toString());
                try {
                    startActivity(instaIntent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Utils.showSnackBar(ShareSelfie.this, "Instagram is not installed.", "OK");
                }
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
