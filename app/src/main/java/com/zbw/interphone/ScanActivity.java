package com.zbw.interphone;

import android.app.Fragment;

import com.zbw.interphone.base.SingleFragmentActivity;

/**
 * Created by ZBW on 2017/4/29.
 */

public class ScanActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new ScanFragment();
    }
}
