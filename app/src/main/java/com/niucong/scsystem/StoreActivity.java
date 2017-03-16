package com.niucong.scsystem;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
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
        mDatas = DBUtil.getDaoSession().getStoreListDao().loadAll();
        wDatas = new ArrayList<>();
        for (StoreList mData : mDatas) {
            if (mData.getNumber() < mData.getWarnNumber()) {
                wDatas.add(mData);
            }
        }
        tv_warn.setText(wDatas.size() + " 种需要进货");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new StoreAdapter(mDatas));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        cb_warn.setVisibility(View.VISIBLE);
        cb_warn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mRecyclerView.setAdapter(mAdapter = new StoreAdapter(wDatas));
                } else {
                    mRecyclerView.setAdapter(mAdapter = new StoreAdapter(mDatas));
                }
            }
        });

        mRecyclerView.requestFocus();
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
                    et_search.setText("");
                    return true;
                }
            }
        } else {
            for (int i = 0; i < mDatas.size(); i++) {
                if (mDatas.get(i).getBarCode() == code) {
                    mRecyclerView.scrollToPosition(i);
                    return true;
                }
            }
        }
        return false;
    }

    class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.MyViewHolder> {

        List<StoreList> sls;

        public StoreAdapter(List<StoreList> sls) {
            this.sls = sls;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(StoreActivity.this).inflate(R.layout.item_store, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final StoreList sl = sls.get(position);
            holder.tv_code.setText("" + sl.getBarCode());
            holder.tv_num.setText("库存：" + sl.getNumber() + " 售价：" + App.app.showPrice(sl.getPrice()));

            DrugInfo di = DBUtil.getDaoSession().getDrugInfoDao().load(sl.getBarCode());
            holder.tv_name.setText(di.getName());
            holder.tv_factory.setText(di.getFactory());

            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DBUtil.getDaoSession().getStoreListDao().delete(sl);
                    mDatas.remove(sl);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return sls.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_name, tv_code, tv_factory, tv_num;
            ImageView iv_delete;

            public MyViewHolder(View view) {
                super(view);
                tv_name = (TextView) view.findViewById(R.id.item_store_name);
                tv_code = (TextView) view.findViewById(R.id.item_store_code);
                tv_factory = (TextView) view.findViewById(R.id.item_store_factory);
                tv_num = (TextView) view.findViewById(R.id.item_store_num);

                iv_delete = (ImageView) view.findViewById(R.id.item_store_delete);
                iv_delete.setVisibility(View.GONE);
            }
        }
    }

}
