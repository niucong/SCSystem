package com.niucong.scsystem;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.niucong.scsystem.adapter.StoreAdapter;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.StoreList;
import com.niucong.scsystem.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class StoreActivity extends BasicActivity {

    private RecyclerView mRecyclerView;
    private TextView tv_warn;
    private CheckBox cb_warn;

    private List<StoreList> mDatas, wDatas;
    private StoreAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("查看库存");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSearchBar(this, true);

        tv_warn = (TextView) findViewById(R.id.store_warn);
        cb_warn = (CheckBox) findViewById(R.id.store_checkBox);
        mRecyclerView = (RecyclerView) findViewById(R.id.store_rv);

        setData();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new StoreAdapter(this, mDatas));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        mAdapter.setOnItemListener(new StoreAdapter.OnItemListener() {
            @Override
            public void onClick(View v, int pos) {
                mAdapter.setDefSelect(pos);
            }

            @Override
            public void onDelete(StoreList sl) {
                mDatas.remove(sl);
            }
        });

        cb_warn.setVisibility(View.VISIBLE);
        cb_warn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mRecyclerView.setAdapter(mAdapter = new StoreAdapter(StoreActivity.this, wDatas));
                } else {
                    mRecyclerView.setAdapter(mAdapter = new StoreAdapter(StoreActivity.this, mDatas));
                }
            }
        });

        mRecyclerView.requestFocus();
    }

    private void setData() {
        mDatas = DBUtil.getDaoSession().getStoreListDao().loadAll();
        wDatas = new ArrayList<>();
        for (StoreList mData : mDatas) {
            try {
                if (mData.getNumber() < mData.getWarnNumber()) {
                    wDatas.add(mData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        tv_warn.setText(wDatas.size() + " 种需要进货");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mAdapter.isRefersh) {
            mAdapter.isRefersh = false;
            setData();
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected boolean searchDrug(String result) {
        if (TextUtils.isEmpty(result)) {
//            Snackbar.make(tv_warn, "条形码输入错误", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
            return false;
        }
        long code = Long.valueOf(result);
        if (cb_warn.isChecked()) {
            for (int i = 0; i < wDatas.size(); i++) {
                if (wDatas.get(i).getBarCode() == code) {
                    mRecyclerView.scrollToPosition(i);
                    mAdapter.setDefSelect(i);
                    et_search.setText("");
                    return true;
                }
            }
        } else {
            for (int i = 0; i < mDatas.size(); i++) {
                if (mDatas.get(i).getBarCode() == code) {
                    mRecyclerView.scrollToPosition(i);
                    mAdapter.setDefSelect(i);
                    return true;
                }
            }
        }
        return false;
    }

}
