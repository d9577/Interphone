package com.zbw.interphone;


import android.app.Fragment;

import com.zbw.interphone.base.SingleFragmentActivity;

/**
 * Created by ZBW on 2017/4/5.
 */

public class ChannelListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new ChannelListFragment();
    }
}
