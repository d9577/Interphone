package com.zbw.interphone;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonRectangle;
import com.zbw.interphone.model.DeviceSetting;
import com.zbw.interphone.model.InterphoneGlobal;
import com.zbw.interphone.model.InterphoneInfo;
import com.zbw.interphone.model.InterphoneSetting;
import com.zbw.interphone.serialport.SerialPortHelper;
import com.zbw.interphone.util.FileUtil;
import com.zbw.interphone.util.InterphoneJSONSerializerUtil;
import com.zbw.interphone.util.ResourceUtil;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zbw on 2017/4/26.
 */

public class MainFragment extends Fragment {

    private DeviceSetting mDeviceSetting;
    private AppCompatActivity mAppActivity;
    private SerialPortHelper mPortHelper;


    private ResourceUtil RUtil;


    @BindView(R.id.textViewDevice)
    public TextView textViewDevice;
    @BindView(R.id.textViewConnectState)
    public TextView textViewConnectState;

    @BindView(R.id.buttonSelectDevice)
    public ButtonRectangle buttonSelectDevice;
    @BindView(R.id.buttonConnectDevice)
    public ButtonRectangle buttonConnectDevice;
    @BindView(R.id.buttonWriteDevice)
    public ButtonRectangle buttonWriteDevice;
    @BindView(R.id.buttonReadDevice)
    public ButtonRectangle buttonReadDevice;
    @BindView(R.id.buttonStopDevice)
    public ButtonRectangle buttonStopDevice;


    @BindView(R.id.toolBarSetting)
    public Toolbar toolbar;
    @BindView(R.id.drawerLayoutLeft)
    public DrawerLayout mDrawerLayout;
    @BindView(R.id.listViewLeftMenu)
    public ListView lvLeftMenu;

    private ActionBarDrawerToggle mDrawerToggle;

    private String[] lvs;
    private ArrayAdapter arrayAdapter;

    private final static int MAIN_INFO = R.string.main_list_info;
    private final static int MAIN_SETTINGS = R.string.main_list_settings;
    private final static int MAIN_CHANNEL = R.string.main_list_channel;
    private final static int MAIN_DTMF = R.string.main_list_DTMF;
    private final static int MAIN_SCAN = R.string.main_list_scan;
    private final static int MAIN_SAVE = R.string.main_list_save;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppActivity = (AppCompatActivity) getActivity();
        mDeviceSetting = DeviceSetting.get(mAppActivity);
        mPortHelper = new SerialPortHelper(mAppActivity);
        RUtil = new ResourceUtil(mAppActivity);
        InterphoneGlobal.get(mAppActivity);//初始化global
        int[] resId = {MAIN_INFO, MAIN_SETTINGS, MAIN_CHANNEL, MAIN_DTMF, MAIN_SCAN, MAIN_SAVE};
        lvs = RUtil.getStringRescourceList(resId);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        //设置侧边栏以及toolbar
        toolbar.setTitle(R.string.app_name);//设置Toolbar标题
        toolbar.setTitleTextColor(ContextCompat.getColor(mAppActivity, R.color.colorWhite)); //设置标题颜色
        if (mAppActivity.getSupportActionBar() == null) {
            mAppActivity.setSupportActionBar(toolbar);
        } else {
            mAppActivity.getSupportActionBar().hide();
        }
        mAppActivity.getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mAppActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);//创建返回键，并实现打开关/闭监听
        mDrawerToggle = new ActionBarDrawerToggle(mAppActivity, mDrawerLayout, toolbar, R.string.global_ok, R.string.global_ok);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        //设置菜单列表
        arrayAdapter = new ArrayAdapter(mAppActivity, android.R.layout.simple_list_item_1, lvs);
        lvLeftMenu.setAdapter(arrayAdapter);
        lvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        createInfoDialog();
                        break;
                    case 1:
                        goSettingsActivity();
                        break;
                    case 2:
                        goChannelListActivity();
                        break;
                    case 3:
                        goDTMFSettingActivity();
                        break;
                    case 4:
                        goScanActivity();
                        break;
                    case 5:
                        saveData();
                        break;
                    default:
                        Toast.makeText(mAppActivity, "没有此选项", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        return view;
    }


    @OnClick(R.id.buttonSelectDevice)
    public void onClickButtonSelectDevice() {
        int index = -1;
        ArrayList<String> deviceList;
        try {
            deviceList = FileUtil.getFiles("/dev", "tty");
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(mAppActivity, "无法读取文件权限，请给予root权限", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialDialog.Builder(mAppActivity)
                .title(R.string.device)
                .items(deviceList)
                .itemsCallbackSingleChoice(index, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        if (text == null) {
                            return false;
                        }
                        mDeviceSetting.setDevice(text.toString());
                        textViewDevice.setText(text);
                        textViewDevice.setTextColor(ContextCompat.getColor(mAppActivity, R.color.colorWhite));
                        return true;
                    }
                })
                .positiveText(R.string.global_ok)
                .show();

    }

    @OnClick(R.id.buttonConnectDevice)
    public void onClickButtonConnectDevice() {
        if (mDeviceSetting.getDevice().equals("")) {
            Toast.makeText(mAppActivity, "请先选择串口", Toast.LENGTH_SHORT).show();
            return;
        }

        int portState = mPortHelper.setupPort();
        if (portState == mPortHelper.SUCCESS) {
            textViewConnectState.setText(R.string.deviceConnectStateSuccess);
            textViewConnectState.setTextColor(ContextCompat.getColor(mAppActivity, R.color.colorWhite));
            Toast.makeText(mAppActivity, "连接串口成功", Toast.LENGTH_SHORT).show();
        } else if (portState == mPortHelper.ERROR) {
            textViewConnectState.setText(R.string.deviceConnectStateNone);
            textViewConnectState.setTextColor(ContextCompat.getColor(mAppActivity, R.color.colorAccent));
            Toast.makeText(mAppActivity, "连接串口失败", Toast.LENGTH_SHORT).show();
        } else if (portState == mPortHelper.IO_ERROR) {
            textViewConnectState.setText(R.string.deviceConnectStateFailed);
            textViewConnectState.setTextColor(ContextCompat.getColor(mAppActivity, R.color.colorAccent));
            Toast.makeText(mAppActivity, "连接串口失败,IO错误,请检查串口是否设置正确", Toast.LENGTH_SHORT).show();
        } else if (portState == mPortHelper.SECURITY_ERROR) {
            textViewConnectState.setText(R.string.deviceConnectStateFailed);
            textViewConnectState.setTextColor(ContextCompat.getColor(mAppActivity, R.color.colorAccent));
            Toast.makeText(mAppActivity, "连接串口失败，权限错误，请给予root权限", Toast.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.buttonWriteDevice)
    public void writeDevice() {
        mPortHelper.listenPort(true);
    }

    @OnClick(R.id.buttonReadDevice)
    public void readDevice() {

    }

    @OnClick(R.id.buttonStopDevice)
    public void stopDevice() {
        mPortHelper.stopListen();
    }


    private void createInfoDialog() {

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(getActivity().getString(R.string.main_list_info))
                .customView(R.layout.list_info, true)
                .negativeText(android.R.string.cancel)
                .build();
        View view = dialog.getCustomView();
        InterphoneInfo info = InterphoneGlobal.get(mAppActivity).getInfo();
        ((TextView) view.findViewById(R.id.InterphoneInfo_type)).setText(info.getType());
        ((TextView) view.findViewById(R.id.InterphoneInfo_channel)).setText(info.getChannel());
        ((TextView) view.findViewById(R.id.InterphoneInfo_version)).setText(info.getVersion());
        dialog.show();

    }

    private void saveData() {
        if (InterphoneGlobal.get(mAppActivity).saveInterphone()) {
            Toast.makeText(mAppActivity, "保存成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mAppActivity, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void goSettingsActivity() {
        Intent intent = new Intent(mAppActivity, SettingsActivity.class);
        startActivity(intent);
    }

    private void goChannelListActivity() {
        Intent intent = new Intent(mAppActivity, ChannelListActivity.class);
        startActivity(intent);
    }

    private void goDTMFSettingActivity() {
        Intent intent = new Intent(mAppActivity, DTMFSettingActivity.class);
        startActivity(intent);
    }

    private void goScanActivity() {
        Intent intent = new Intent(mAppActivity, ScanActivity.class);
        startActivity(intent);
    }

}
