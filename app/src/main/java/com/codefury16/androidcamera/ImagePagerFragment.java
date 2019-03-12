package com.codefury16.androidcamera;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.aviary.android.feather.sdk.AviaryIntent;
import com.aviary.android.feather.sdk.internal.filters.ToolLoaderFactory;
import com.aviary.android.feather.sdk.internal.headless.utils.MegaPixels;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ImagePagerFragment extends Fragment {

    public static final int INDEX = 2;
    public static final int RESULT_OK = -1;
    //private ImageLoader imageLoader=ImageGridFragment.imageLoader;
    public FragmentPagerAdapter adapterPager;
    ViewPager pager;
    private ToolLoaderFactory.Tools[] tools = {
            ToolLoaderFactory.Tools.EFFECTS,
            ToolLoaderFactory.Tools.LIGHTING,
            ToolLoaderFactory.Tools.CROP,
            ToolLoaderFactory.Tools.ORIENTATION};
    private ArrayList<String> imageList;
    private Uri uri2;

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
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fr_image_pager, container, false);
        pager = rootView.findViewById(R.id.pager);
        setHasOptionsMenu(true);
        imageList = ImageConstant.getImages();
        adapterPager = new FragmentPagerAdapter(getChildFragmentManager(), imageList);

        pager.setAdapter(adapterPager);
        pager.setCurrentItem(getArguments().getInt(ImageConstant.Extra.IMAGE_POSITION, 0));
        ImageButton deleteButton = rootView.findViewById(R.id.imageDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View view) {
                AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(),
                        R.style.AlertDialogCustom));
                alert.setMessage("Delete selected image?");
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int positionOfFile = pager.getCurrentItem();
                        String path = imageList.get(positionOfFile);
                        Uri imageUri = Uri.parse(path);
                        File imageFile = ImageLoader.getInstance().getDiscCache().get(imageUri.toString());
                        path = path.substring(5);
                        File fi = new File(path);

                        if (imageFile.exists() && fi.exists()) {
                            imageFile.delete();
                            fi.delete();
                            galleryAddPic(fi.getAbsolutePath());
                            imageList.remove(positionOfFile);
                        }

                        deleteFileFromMediaStore(getActivity().getContentResolver(), fi);
                        //getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,Uri.parse(path+ Environment.getExternalStorageDirectory())));
                        MemoryCacheUtils.removeFromCache("file://" + imageUri.toString(), ImageLoader.getInstance().getMemoryCache());
                        DiskCacheUtils.removeFromCache("file://" + imageUri.toString(), ImageLoader.getInstance().getDiskCache());
                        imageList = ImageConstant.getImages();
                        adapterPager.deleteImage(positionOfFile);

                        if (pager.getAdapter().getCount() == 0) {
                            getActivity().onBackPressed();
                        }
                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();
            }
        });
        final ImageButton shareImage = rootView.findViewById(R.id.imageShare);
        shareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String path = ImageConstant.getImages().get(pager.getCurrentItem());
                Uri shareUri = Uri.parse(path);
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("image/jpeg");
                sendIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "#CodeFury16");
                startActivity(Intent.createChooser(sendIntent, "share"));
            }
        });
        final ImageButton editImage = rootView.findViewById(R.id.imageEdit);
        editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String path = ImageConstant.getImages().get(pager.getCurrentItem());
                path = path.substring(5);
                Uri uri = Uri.parse(path);
                Uri uri2 = Uri.parse(path + "edit");
                final Intent newIntent = new AviaryIntent.Builder(getActivity())
                        .setData(uri) // input image src
                        .withOutput(uri2) // output file
                        .withOutputFormat(Bitmap.CompressFormat.JPEG) // output format
                        .withOutputQuality(95)// output quality
                        .withOutputSize(MegaPixels.Mp5)
                        .saveWithNoChanges(true)
                        .withNoExitConfirmation(false)
                        .withToolList(tools)
                        .build();
                startActivityForResult(newIntent, 1);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1: // this is the request code we used in this example
                    Uri mImageUri = data.getData(); // generated output file
                    Bundle extra = data.getExtras();
                    if (null != extra) {
                        // image has been changed?
                        boolean changed = extra.getBoolean(com.aviary.android.feather.sdk.internal.Constants.EXTRA_OUT_BITMAP_CHANGED);
                        if (changed) {
                            galleryAddPic(String.valueOf(mImageUri));
                            MemoryCacheUtils.removeFromCache("file://" + mImageUri.toString(), ImageLoader.getInstance().getMemoryCache());
                            DiskCacheUtils.removeFromCache("file://" + mImageUri.toString(), ImageLoader.getInstance().getDiskCache());
                            AbsListViewBaseFragment.gridView.invalidateViews();
                            adapterPager.notifyDataSetChanged();
                            getActivity().onBackPressed();
                        }else {
                            String path = uri2.getPath();
                            File imageFile = ImageLoader.getInstance().getDiscCache().get(path);
                            File fi = new File(path);
                            if (imageFile.exists() && fi.exists()) {
                                imageFile.delete();
                                fi.delete();
                                galleryAddPic(fi.getAbsolutePath());
                            }
                        }
                    }
                    break;
            }
        }
    }

    private void galleryAddPic(String s) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(s);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
        imageList = ImageConstant.getImages();
        adapterPager.notifyDataSetChanged();
    }
}