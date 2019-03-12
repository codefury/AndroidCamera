/**
 * ****************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package com.codefury16.androidcamera;

import android.content.Intent;
import android.widget.AbsListView;
import androidx.fragment.app.Fragment;

public abstract class AbsListViewBaseFragment extends Fragment {

    public static AbsListView gridView;

    @Override
    public void onResume() {
        gridView.invalidateViews();
        super.onResume();
    }

    void startImagePagerActivity(int position) {
        Intent intent = new Intent(getActivity(), SimpleImageActivity.class);
        intent.putExtra(ImageConstant.Extra.FRAGMENT_INDEX, ImagePagerFragment.INDEX);
        intent.putExtra(ImageConstant.Extra.IMAGE_POSITION, position);
        startActivity(intent);
    }
}
