package com.codefury16.androidcamera;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import androidx.appcompat.view.ContextThemeWrapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImageGridFragment extends AbsListViewBaseFragment {

    public static final int INDEX = 1;
    public static ImageLoader imageLoader;
    public static ImageView emptyGalleryImage, emptyGalleryImage2;
    public static TextView emptyGalleryText;
    public static ImageAdapter adapterGrid;
    public ArrayList<String> IMAGE_URLS;

    public static void deleteFileFromMediaStore(final ContentResolver contentResolver, final File file) {
        String canonicalPath;
        try {
            canonicalPath = file.getCanonicalPath();
        } catch (IOException e) {
            canonicalPath = file.getAbsolutePath();
        }
        final Uri uri = MediaStore.Files.getContentUri("external");
        final int result = contentResolver.delete(uri,
                MediaStore.Files.FileColumns.DATA + "=?", new String[]{canonicalPath});
        if (result == 0) {
            final String absolutePath = file.getAbsolutePath();
            if (!absolutePath.equals(canonicalPath)) {
                contentResolver.delete(uri,
                        MediaStore.Files.FileColumns.DATA + "=?", new String[]{absolutePath});
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fr_image_grid, container, false);
        emptyGalleryImage = rootView.findViewById(R.id.coffee);
        emptyGalleryImage2 = rootView.findViewById(R.id.arrow);
        emptyGalleryText = rootView.findViewById(R.id.empty);
        gridView = (GridView) rootView.findViewById(R.id.grid);

        IMAGE_URLS = ImageConstant.getImages();
        adapterGrid = new ImageAdapter(getActivity(), IMAGE_URLS);
        gridView.invalidateViews();
        gridView.setAdapter(adapterGrid);
        if (adapterGrid != null) {
            adapterGrid.notifyDataSetChanged();
        }
        gridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        gridView.setDrawSelectorOnTop(true);
        gridView.setMultiChoiceModeListener(new MultiChoiceModeListener());

        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startImagePagerActivity(position);
            }
        });
        final FloatingActionButton fab = rootView.findViewById(R.id.fabgallery);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        setGallery();
        return rootView;
    }

    public void setGallery() {
        if (gridView.getCount() > 0) {
            emptyGalleryImage.setVisibility(View.GONE);
            emptyGalleryImage2.setVisibility(View.GONE);
            emptyGalleryText.setVisibility(View.GONE);
            IMAGE_URLS = ImageConstant.getImages();
            adapterGrid = new ImageAdapter(getActivity(), IMAGE_URLS);
            gridView.setAdapter(adapterGrid);
            adapterGrid.notifyDataSetChanged();
        } else {
            emptyGalleryImage.setVisibility(View.VISIBLE);
            emptyGalleryImage2.setVisibility(View.VISIBLE);
            emptyGalleryText.setVisibility(View.VISIBLE);
            IMAGE_URLS = ImageConstant.getImages();
            adapterGrid = new ImageAdapter(getActivity(), IMAGE_URLS);
            gridView.setAdapter(adapterGrid);
            adapterGrid.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        setGallery();
        super.onResume();
    }

    private static class ImageAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ImageLoaderConfiguration config;
        private DisplayImageOptions options;
        private ArrayList<String> list;

        ImageAdapter(Context context, ArrayList<String> IMAGE_URLS) {
            list = IMAGE_URLS;
            inflater = LayoutInflater.from(context);

            config = new ImageLoaderConfiguration.Builder(context)
                    .memoryCacheExtraOptions(480, 800) // default = device screen dimensions
                    .threadPoolSize(3) // default
                    .threadPriority(Thread.NORM_PRIORITY - 1) // default
                    .denyCacheImageMultipleSizesInMemory()
                    .diskCacheSize(50 * 1024 * 1024)
                    .imageDownloader(new BaseImageDownloader(context)) // default
                    .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                    .build();

            options = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.ic_stub)
                    .showImageForEmptyUri(R.drawable.ic_empty)
                    .showImageOnFail(R.drawable.ic_error)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(R.layout.item_grid_image, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.imageView = view.findViewById(R.id.imageView);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            ImageAware imageAware = new ImageViewAware(holder.imageView, false);

            imageLoader.displayImage(list.get(position), imageAware, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    gridView.invalidateViews();
                    adapterGrid.notifyDataSetChanged();
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                }
            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current, int total) {
                }
            });
            return view;
        }
    }

    static class ViewHolder {
        ImageView imageView;
    }

    public class MultiChoiceModeListener implements GridView.MultiChoiceModeListener {
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(Html.fromHtml("<font color='#9c9c9c'>Select Images</font>"));
            mode.setSubtitle("One item selected");
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.gallerymenu, menu);
            return true;
        }

        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            int i1 = item.getItemId();
            if (i1 == R.id.deleteGrid) {
                AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),
                        R.style.AlertDialogCustom));
                alert.setMessage("Delete selected photos?");
                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SparseBooleanArray sparseBooleanArray1 = gridView.getCheckedItemPositions();
                        for (int i = sparseBooleanArray1.size() - 1; i >= 0; i--) {
                            if (sparseBooleanArray1.valueAt(i)) {
                                String path = ImageConstant.getImages().get(sparseBooleanArray1.keyAt(i));
                                Uri imageUri = Uri.parse(path);
                                File imageFile = ImageLoader.getInstance().getDiskCache().get(imageUri.toString());
                                path = path.substring(5);
                                File fi = new File(path);
                                if (imageFile.exists() && fi.exists()) {
                                    ImageConstant.getImages().remove(sparseBooleanArray1.keyAt(i));
                                    imageFile.delete();
                                    fi.delete();

                                }
                                deleteFileFromMediaStore(getActivity().getContentResolver(), fi);
                                MemoryCacheUtils.removeFromCache("file://" + imageUri.toString(), ImageLoader.getInstance().getMemoryCache());
                                DiskCacheUtils.removeFromCache("file://" + imageUri.toString(), ImageLoader.getInstance().getDiskCache());
                                AbsListViewBaseFragment.gridView.invalidateViews();
                                IMAGE_URLS = ImageConstant.getImages();
                                adapterGrid = new ImageAdapter(getActivity(), IMAGE_URLS);
                                gridView.setAdapter(adapterGrid);
                                gridView.invalidateViews();
                                gridView.clearChoices();
                                setGallery();
                                //Toast.makeText(getActivity(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                        dialog.dismiss();
                        mode.finish();
                    }
                });
                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
                alert.show();


            } else if (i1 == R.id.shareGrid) {
                SparseBooleanArray sparseBooleanArray2 = gridView.getCheckedItemPositions();
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                //intent.putExtra(Intent.EXTRA_SUBJECT, "#CodeFury16");
                intent.setType("image/jpeg"); /* This example is sharing jpeg images. */
                ArrayList<Uri> files = new ArrayList<Uri>();
                for (int i = 0; i <= sparseBooleanArray2.size() - 1; i++) {
                    if (sparseBooleanArray2.valueAt(i)) {
                        String path = ImageConstant.getImages().get(sparseBooleanArray2.keyAt(i));
                        Uri uri = Uri.parse(path);
                        files.add(uri);
                    }
                }
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
                startActivity(intent);
                mode.finish();

            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            //adapterGrid.notifyDataSetChanged();
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position,
                                              long id, boolean checked) {
            int selectCount = gridView.getCheckedItemCount();
            switch (selectCount) {
                case 1:
                    mode.setSubtitle("One item selected");
                    break;
                default:
                    mode.setSubtitle("" + selectCount + " items selected");
                    break;
            }
        }

    }
}