package com.codefury16.androidcamera;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

/**
 * Created by nightmare on 06/10/15.
 */
public class FragmentPagerAdapter extends FragmentStatePagerAdapter {


    ArrayList<String> arrayList;


    public FragmentPagerAdapter(FragmentManager fm, ArrayList<String> imageFragments) {
        super(fm);
        arrayList=imageFragments;
    }

    @Override
    public Fragment getItem(int position) {
        return ImageFragment.getInstance(arrayList.get(position));
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    public void add(String uri)
    {
        arrayList.add(0,uri);
    }

    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }

    public void deleteImage(int position){
        arrayList.remove(position);
        notifyDataSetChanged();
    }
}
