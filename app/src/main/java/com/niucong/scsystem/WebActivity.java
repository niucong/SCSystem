package com.niucong.scsystem;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebActivity extends BasicActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("使用帮助");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView wView = (WebView) findViewById(R.id.web);
        WebSettings wSet = wView.getSettings();
        wSet.setJavaScriptEnabled(true);

        wView.loadUrl("file:///android_asset/help.html");
    }

    @Override
    protected boolean searchDrug(String result) {
        return false;
    }
}
