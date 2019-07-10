package com.niucong.scsystem;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.util.ScanGunKeyEventHelper;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

//import com.umeng.analytics.MobclickAgent;

public abstract class BasicActivity extends AppCompatActivity implements View.OnClickListener, ScanGunKeyEventHelper.OnScanSuccessListener {
    private String TAG = "BasicActivity";

    protected AutoCompleteTextView et_search;
    private ScanGunKeyEventHelper mScanGunKeyEventHelper;

    protected boolean isTablet;

    protected boolean isManualInput = true;// 是否手动输入
    protected int searchType;// 搜索类型：0搜索、1名称、2厂家

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isTablet = isTablet();
        if (isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onCreate(savedInstanceState);

        registerBoradcastReceiver();
        mScanGunKeyEventHelper = new ScanGunKeyEventHelper(this);
    }

    private void registerBoradcastReceiver() {
        IntentFilter filter1 = new IntentFilter(
                BluetoothAdapter.ACTION_STATE_CHANGED);
        IntentFilter filter2 = new IntentFilter(
                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(stateChangeReceiver, filter1);
        registerReceiver(stateChangeReceiver, filter2);
    }

    private BroadcastReceiver stateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Toast.makeText(BasicActivity.this, "蓝牙设备连接状态已变更", Toast.LENGTH_SHORT).show();
            } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                Toast.makeText(BasicActivity.this, "蓝牙设备连接状态已变更", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }

    /**
     * 截获按键事件.发给ScanGunKeyEventHelper
     *
     * @param event
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (mScanGunKeyEventHelper.isScanGunEvent(event)) {
            Log.d(TAG, "dispatchKeyEvent true");
            mScanGunKeyEventHelper.analysisKeyEvent(event);
            return true;
        }
        Log.d(TAG, "dispatchKeyEvent false");
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onScanSuccess(String barcode) {
        Log.d(TAG, "onScanSuccess barcode=" + barcode);
//        et_search.setText(barcode);
        searchDrug(barcode);
    }

    public void onResume() {
        super.onResume();
//        if (!mScanGunKeyEventHelper.hasScanGun()) {
//            Toast.makeText(this, "未检测到扫码枪设备", Toast.LENGTH_SHORT).show();
//        }
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stateChangeReceiver);
        mScanGunKeyEventHelper.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    protected void setSearchBar(Context context, boolean isAuto) {
        et_search = (AutoCompleteTextView) findViewById(R.id.search_et);
        final ImageView iv_delete = (ImageView) findViewById(R.id.search_delete);
        final ImageView iv_scan = (ImageView) findViewById(R.id.search_scan);

        if (isAuto) {
            SearchAdapter searchAdapter = new SearchAdapter(context, App.app.list);
            et_search.setAdapter(searchAdapter);
        }

        et_search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString().trim();
                if (isManualInput) {
                    searchType = 0;
                }
                if (str.length() > 0) {
//                    iv_delete.setVisibility(View.VISIBLE);
//                    iv_scan.setVisibility(View.GONE);
                    try {
                        Long.valueOf(str);
                        if (str.length() > 11) {
                            searchDrug(str);
                        }
                    } catch (NumberFormatException e) {

                    }
                } else {
//                    iv_delete.setVisibility(View.GONE);
//                    iv_scan.setVisibility(View.VISIBLE);
                    et_search.requestFocus();
                }
            }
        });

        iv_delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                iv_delete.setVisibility(View.GONE);
                iv_scan.setVisibility(View.VISIBLE);
                et_search.setText("");
            }
        });
        iv_scan.setOnClickListener(this);
    }

    protected class SearchAdapter extends BaseAdapter implements Filterable {

        Context context;
        List<DrugInfo> list;

        ArrayFilter mFilter;
        ArrayList<DrugInfo> mUnfilteredData;

        public SearchAdapter(Context context, List<DrugInfo> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
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
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(context).inflate(R.layout.item_search, null);
                holder.tv_name = (TextView) convertView.findViewById(R.id.item_search_name);
                holder.tv_bar = (TextView) convertView.findViewById(R.id.item_search_bar);
                holder.tv_factory = (TextView) convertView.findViewById(R.id.item_search_factory);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final DrugInfo di = list.get(position);
            holder.tv_name.setText(di.getName());
            holder.tv_bar.setText("" + di.getBarCode());
            holder.tv_factory.setText(di.getFactory());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (searchDrug("" + di.getBarCode())) {
                        et_search.setText("");
                    }
                }
            });
            return convertView;
        }

        class ViewHolder {
            TextView tv_name, tv_bar, tv_factory;
        }

        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new ArrayFilter();
            }
            return mFilter;
        }

        private class ArrayFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence prefix) {
                FilterResults results = new FilterResults();

                if (mUnfilteredData == null) {
                    mUnfilteredData = new ArrayList<DrugInfo>(list);
                }

                if (prefix == null || prefix.length() == 0) {
                    ArrayList<DrugInfo> list = mUnfilteredData;
                    results.values = list;
                    results.count = list.size();
                } else {
                    String prefixString = prefix.toString().toLowerCase();

                    ArrayList<DrugInfo> unfilteredValues = mUnfilteredData;
                    int count = unfilteredValues.size();

                    ArrayList<DrugInfo> newValues = new ArrayList<DrugInfo>(count);

                    for (int i = 0; i < count; i++) {
                        DrugInfo pc = unfilteredValues.get(i);
                        if (pc != null) {
                            if (pc.getName() != null && pc.getName().startsWith(prefixString)) {
                                newValues.add(pc);
                            } else if (pc.getNamePY() != null && pc.getNamePY().startsWith(prefixString)) {
                                newValues.add(pc);
                            } else if (pc.getNamePYF() != null && pc.getNamePYF().startsWith(prefixString)) {
                                newValues.add(pc);
                            } else if (pc.getFactory() != null && pc.getFactory().startsWith(prefixString)) {
                                newValues.add(pc);
                            }
                        }
                    }

                    results.values = newValues;
                    results.count = newValues.size();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                //noinspection unchecked
                list = (List<DrugInfo>) results.values;
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_scan:
                //扫描操作  
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setCaptureActivity(ScanActivity.class);
                integrator.setCameraId(App.app.share.getIntMessage("SC", "CameraId", 0)); //1前置或者0后置摄像头
                integrator.initiateScan();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String result = scanResult.getContents();
            if (result == null) {
                Snackbar.make(et_search, "扫描取消", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                if (searchDrug(result)) {
                    et_search.setText("");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected abstract boolean searchDrug(String result);
}
