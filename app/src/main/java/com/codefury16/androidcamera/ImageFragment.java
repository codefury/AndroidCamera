package com.codefury16.androidcamera;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by nightmare on 06/10/15.
 */
public class ImageFragment extends Fragment {

    private DisplayImageOptions options;

    public static ImageFragment getInstance(String s) {
        Bundle bundle=new Bundle();
        bundle.putString("IMAGE",s);
        ImageFragment imageFragment=new ImageFragment();
        imageFragment.setArguments(bundle);
        return imageFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View imageLayout = inflater.inflate(R.layout.item_pager_image, container, false);
        ImageView imageView= imageLayout.findViewById(R.id.imageView);
        Bundle bundle =getArguments();
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .resetViewBeforeLoading(true)
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .displayer(new FadeInBitmapDisplayer(300))
                .build();
        final ProgressBar spinner = imageLayout.findViewById(R.id.loading);

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ImageLoader.getInstance().displayImage(String.valueOf(Uri.parse(bundle.getString("IMAGE"))), imageView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                spinner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                spinner.setVisibility(View.GONE);
            }
        });
        //imageView.setImageURI(Uri.parse(bundle.getString("IMAGE")));
        return imageLayout;
    }
}
