package com.niucong.scsystem;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.EnterRecord;
import com.niucong.scsystem.dao.EnterRecordDao;
import com.niucong.scsystem.view.DividerItemDecoration;
import com.niucong.scsystem.view.NiftyDialogBuilder;
import com.niucong.scsystem.view.wheel.DateTimeSelectView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EnterRecordActivity extends BasicActivity {
    private static final String TAG = "EnterRecordActivity";

    private RecyclerView mRecyclerView;
    private TextView tv_warn;

    private List<EnterRecord> mDatas;
    private StoreAdapter mAdapter;

    private SimpleDateFormat ymdhm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

    private Date startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Log.d(TAG, "onCreate...");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("进货记录");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setSearchBar(this, true);

        if (isTablet) {
            findViewById(R.id.store_title).setVisibility(View.VISIBLE);
        }

        tv_warn = (TextView) findViewById(R.id.store_warn);
        mRecyclerView = (RecyclerView) findViewById(R.id.store_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        try {
            selectData(ymd.parse(ymd.format(new Date())), new Date());
        } catch (ParseException e) {
            mDatas = new ArrayList<>();
        }
        tv_warn.setText("今日进货量：" + mDatas.size() + " 种药品");
        mRecyclerView.setAdapter(mAdapter = new StoreAdapter(mDatas));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        mRecyclerView.requestFocus();
    }

    private void selectData(Date st, Date et) {
        mDatas = DBUtil.getDaoSession().getEnterRecordDao().queryBuilder().where(EnterRecordDao.Properties.EnterDate.ge(st), EnterRecordDao.Properties.EnterDate.le(et)).orderDesc(EnterRecordDao.Properties.EnterDate).list();
        Log.d(TAG, "selectData size=" + mDatas.size());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_all:
                mDatas = DBUtil.getDaoSession().getEnterRecordDao().queryBuilder().orderDesc(EnterRecordDao.Properties.EnterDate).list();
                Log.d(TAG, "onOptionsItemSelected size=" + mDatas.size());
                mRecyclerView.setAdapter(mAdapter = new StoreAdapter(mDatas));
                tv_warn.setText("所有进货量：" + mDatas.size() + " 种药品");
                break;
            case R.id.action_day:
                try {
                    selectData(ymd.parse(ymd.format(new Date())), new Date());
                    mRecyclerView.setAdapter(mAdapter = new StoreAdapter(mDatas));
                    tv_warn.setText("今日进货量：" + mDatas.size() + " 种药品");
                } catch (ParseException e) {
                    mDatas = new ArrayList<>();
                }
                break;
            case R.id.action_mouth:
                String startDate = "";
                try {
                    Calendar c = Calendar.getInstance();
                    if (c.get(Calendar.MONTH) < 9) {
                        startDate = c.get(Calendar.YEAR) + "-0" + (c.get(Calendar.MONTH) + 1) + "-01";
                    } else {
                        startDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-01";
                    }
                    selectData(ymd.parse(startDate), new Date());
                    mRecyclerView.setAdapter(mAdapter = new StoreAdapter(mDatas));
                    tv_warn.setText("当月进货量：" + mDatas.size() + " 种药品");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_select:
                showSubmitDia();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 选择日期对话框
     */
    private void showSubmitDia() {
        final NiftyDialogBuilder submitDia = NiftyDialogBuilder.getInstance(this);
        View selectDateView = LayoutInflater.from(this).inflate(R.layout.dialog_select_date, null);
        final DateTimeSelectView ds = (DateTimeSelectView) selectDateView.findViewById(R.id.date_start);
        final DateTimeSelectView de = (DateTimeSelectView) selectDateView.findViewById(R.id.date_end);

        final Calendar c = Calendar.getInstance();
        try {
            startDate = ymdhm.parse(ymdhm.format(new Date()));// 当日00：00：00
        } catch (ParseException e) {
            e.printStackTrace();
        }
        endDate = new Date();

        submitDia.withTitle("选择查询日期");
        submitDia.withButton1Text("取消", 0).setButton1Click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitDia.dismiss();
            }
        });
        submitDia.withButton2Text("确定", 0).setButton2Click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startDate = ymdhm.parse(ds.getDate());
                    Log.d(TAG, "开始：" + ds.getDate() + "，结束：" + de.getDate() + "，当前：" + ymdhm.format(new Date()));
//                    if (ymd.format(new Date()).equals(de.getDate())) {// 结束日期是今天
//                        endDate = new Date();// 当前时间
//                    } else {
//                        endDate = new Date(ymd.parse(de.getDate()).getTime() + 1000 * 60 * 60 * 24 - 1);// 当日23：59：59
//                    }
                    endDate = ymdhm.parse(de.getDate());
                    if (endDate.before(startDate)) {
                        Snackbar.make(mRecyclerView, "开始日期不能大于结束日期", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        selectData(startDate, endDate);
                        mRecyclerView.setAdapter(mAdapter = new StoreAdapter(mDatas));
                        tv_warn.setText(ymdhm.format(startDate) + "到" + ymdhm.format(endDate) + "的进货量：" + mDatas.size() + " 种药品");
                        submitDia.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        submitDia.setCustomView(selectDateView, this);// "请选择查询日期"
        submitDia.withMessage(null).withDuration(400);
        submitDia.isCancelable(false);
        submitDia.show();
    }

    @Override
    protected boolean searchDrug(String result) {
        if (TextUtils.isEmpty(result)) {
//            Snackbar.make(mRecyclerView, "条形码输入错误", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
            return false;
        }
        long code = Long.valueOf(result);
        List<EnterRecord> sDatas = DBUtil.getDaoSession().getEnterRecordDao().queryBuilder().where(EnterRecordDao.Properties.BarCode.eq(code)).orderDesc(EnterRecordDao.Properties.EnterDate).list();
        if (sDatas.size() > 0) {
            mRecyclerView.setAdapter(mAdapter = new StoreAdapter(sDatas));
            et_search.setText("");
            return true;
        } else {
            mRecyclerView.setAdapter(mAdapter = new StoreAdapter(mDatas));
            return false;
        }
    }

    class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.MyViewHolder> {

        List<EnterRecord> dis;

        public StoreAdapter(List<EnterRecord> dis) {
            this.dis = dis;
        }

        @Override
        public StoreAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            StoreAdapter.MyViewHolder holder = new StoreAdapter.MyViewHolder(LayoutInflater.from(EnterRecordActivity.this).inflate(R.layout.item_statistics, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(StoreAdapter.MyViewHolder holder, int position) {
            final EnterRecord sr = dis.get(position);
            long code = sr.getBarCode();
            holder.tv_code.setText("" + code);
            holder.tv_num.setText(App.app.showPrice(sr.getPrice()));
            holder.tv_time.setText(ymdhm.format(sr.getEnterDate()));
            holder.tv_price.setText("" + sr.getNumber());

            DrugInfo di = DBUtil.getDaoSession().getDrugInfoDao().load(code);
            holder.tv_name.setText(di.getName());
            holder.tv_factory.setText(di.getFactory());
        }

        @Override
        public int getItemCount() {
            return dis.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_name, tv_code, tv_factory, tv_num, tv_time, tv_price;

            public MyViewHolder(View view) {
                super(view);
                tv_name = (TextView) view.findViewById(R.id.item_statistics_name);
                tv_code = (TextView) view.findViewById(R.id.item_statistics_code);
                tv_factory = (TextView) view.findViewById(R.id.item_statistics_factory);
                tv_num = (TextView) view.findViewById(R.id.item_statistics_num);
                tv_time = (TextView) view.findViewById(R.id.item_statistics_time);
                tv_price = (TextView) view.findViewById(R.id.item_statistics_subPrice);
            }
        }
    }
}
