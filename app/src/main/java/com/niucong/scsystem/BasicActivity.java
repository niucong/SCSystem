package com.niucong.scsystem;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.niucong.scsystem.app.App;
import com.umeng.analytics.MobclickAgent;

public abstract class BasicActivity extends AppCompatActivity implements View.OnClickListener {

    protected AutoCompleteTextView et_search;

    protected boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isTablet = isTablet();
        if (isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        super.onCreate(savedInstanceState);
    }

    private boolean isTablet(){
        return getResources().getBoolean(R.bool.isTablet);
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }

    protected void setSearchBar() {
        et_search = (AutoCompleteTextView) findViewById(R.id.search_et);
        final ImageView iv_delete = (ImageView) findViewById(R.id.search_delete);
        final ImageView iv_scan = (ImageView) findViewById(R.id.search_scan);

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
                if (str.length() > 0) {
                    iv_delete.setVisibility(View.VISIBLE);
                    iv_scan.setVisibility(View.GONE);
                    if (str.length() > 12) {
                        searchDrug(str);
//                        if () {
////                            et_search.setText("");
//                        }
                    }
                } else {
                    iv_delete.setVisibility(View.GONE);
                    iv_scan.setVisibility(View.VISIBLE);
                    et_search.requestFocus();
                }
            }
        });
//        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId == KeyEvent.ACTION_DOWN
//                        || actionId == EditorInfo.IME_ACTION_DONE) {
//                    if (searchDrug(et_search.getText().toString().trim())) {
////                        et_search.setText("");
//                    }
//                }
//                return false;
//            }
//        });
        iv_delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                iv_delete.setVisibility(View.GONE);
                iv_scan.setVisibility(View.VISIBLE);
                et_search.setText("");
            }
        });
        iv_scan.setOnClickListener(this);
//        String[] arr = App.app.share.getBarCodes();
//        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr);
//        et_search.setAdapter(arrayAdapter);
//    }
//
//    protected void setAutoComplete() {
//        String[] arr = App.app.share.getBarCodes();
//        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr);
//        et_search.setAdapter(arrayAdapter);
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
//                    et_search.setText("");
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected abstract boolean searchDrug(String result);
}
