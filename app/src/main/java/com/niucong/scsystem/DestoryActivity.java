package com.niucong.scsystem;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.StoreList;
import com.niucong.scsystem.view.DividerItemDecoration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DestoryActivity extends BasicActivity {
    private String TAG = "DestoryActivity";

    private RecyclerView mRecyclerView;
    private Button btn;

    private LinkedHashMap<Long, Integer> map;
    private List<StoreList> mDatas;
    private HomeAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destory);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("报损处理");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSearchBar();

        map = new LinkedHashMap<>();
        mDatas = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.main_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new HomeAdapter());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));
        findViewById(R.id.main_total).setVisibility(View.INVISIBLE);
        btn = (Button) findViewById(R.id.main_btn);
        btn.setOnClickListener(this);
        btn.setText("确定");

        mRecyclerView.requestFocus();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.main_btn:
                // 结算
                Log.d(TAG, "onClick size=" + mDatas.size());
                if (mDatas.size() == 0) {
                    return;
                }
                List<StoreList> uDatas = new ArrayList<>();
                long time = System.currentTimeMillis();
                for (StoreList di : mDatas) {
                    int sellNum = map.get(di.getBarCode());
                    di.setNumber(di.getNumber() - sellNum);
                    uDatas.add(di);
                }
                DBUtil.getDaoSession().getStoreListDao().insertOrReplaceInTx(uDatas);

                map.clear();
                mDatas.clear();
                mAdapter.notifyDataSetChanged();
                Snackbar.make(mRecyclerView, "报损成功", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;
        }
    }

    @Override
    protected boolean searchDrug(String result) {
        Log.d(TAG, "searchDrug code=" + result);
        if (TextUtils.isEmpty(result)) {
//            Snackbar.make(mRecyclerView, "条形码输入错误", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
            return false;
        }
        long code = Long.valueOf(result);
        StoreList di = DBUtil.getDaoSession().getStoreListDao().load(code);
        if (di != null) {
            if (map.get(code) == null) {
                map.put(code, 1);
            } else {
                map.put(code, map.get(code) + 1);
            }
            mDatas.remove(di);
            mDatas.add(0, di);
            mAdapter.notifyDataSetChanged();

            et_search.setText("");
            return true;
        } else {
            Snackbar.make(mRecyclerView, "该药品不在库存中,请先添加入库", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return false;
        }
    }

    class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(DestoryActivity.this).inflate(R.layout.item_home, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final StoreList sl = mDatas.get(position);
            final long code = sl.getBarCode();
            holder.tv_code.setText("" + code);
            holder.tv_price.setText("" + sl.getPrice());
            holder.tv_num.setText("" + map.get(code));
            holder.tv_subPrice.setText("小计：" + sl.getPrice() * map.get(code));

            DrugInfo di = DBUtil.getDaoSession().getDrugInfoDao().load(code);
            holder.tv_name.setText(di.getName());
            holder.tv_factory.setText(di.getFactory());

            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    map.remove(code);
                    mDatas.remove(sl);
                    notifyDataSetChanged();
                }
            });

            holder.tv_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int num = map.get(code);
                    if (num > 1) {
                        map.put(code, num - 1);
                        notifyDataSetChanged();
                    }
                }
            });

            holder.tv_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    map.put(code, map.get(code) + 1);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_name, tv_code, tv_factory, tv_price, tv_num, tv_remove, tv_add, tv_subPrice;
            ImageView iv_delete;

            public MyViewHolder(View view) {
                super(view);
                tv_name = (TextView) view.findViewById(R.id.item_home_name);
                tv_code = (TextView) view.findViewById(R.id.item_home_code);
                tv_factory = (TextView) view.findViewById(R.id.item_home_factory);
                tv_price = (TextView) view.findViewById(R.id.item_home_pirce);
                tv_num = (TextView) view.findViewById(R.id.item_home_num);
                tv_remove = (TextView) view.findViewById(R.id.item_home_remove);
                tv_add = (TextView) view.findViewById(R.id.item_home_add);
                tv_subPrice = (TextView) view.findViewById(R.id.item_home_subPrice);
                iv_delete = (ImageView) view.findViewById(R.id.item_home_delete);

                tv_price.setVisibility(View.GONE);
                tv_subPrice.setVisibility(View.GONE);
            }
        }
    }

}
