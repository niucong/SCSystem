package com.niucong.scsystem;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.SellRecord;
import com.niucong.scsystem.dao.SellRecordDao;
import com.niucong.scsystem.dao.StoreList;
import com.niucong.scsystem.view.DividerItemDecoration;
import com.niucong.scsystem.view.NiftyDialogBuilder;
import com.niucong.scsystem.view.wheel.DateTimeSelectView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class StatisticsActivity extends BasicActivity {
    private static final String TAG = "StatisticsActivity";

    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private TextView tv_warn;
    private RadioGroup rg;

    private List<SellRecord> mDatas, tempDatas;

    private SimpleDateFormat ymdhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat ymdhm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

    private Date startDate, endDate;

    private String startTip;
    private int showType;// 0:按订单查看、1：按药品查看

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Log.d(TAG, "onCreate...");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("销售记录");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setSearchBar(this, false);
        tempDatas = new ArrayList<>();

        rg = (RadioGroup) findViewById(R.id.store_type);
        tv_warn = (TextView) findViewById(R.id.store_warn);
        mRecyclerView = (RecyclerView) findViewById(R.id.store_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        showToday();
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        mRecyclerView.requestFocus();

        et_search.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        et_search.setHint("请输入订单号");
        rg.setVisibility(View.VISIBLE);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.store_order:
                        showType = 0;
                        showToday();
                        et_search.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                        et_search.setHint("请输入订单号");
                        setSearchBar(StatisticsActivity.this, false);
                        break;
                    case R.id.store_drug:
                        showType = 1;
                        showToday();
                        et_search.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                        et_search.setHint("请输入条形码或药品名");
                        setSearchBar(StatisticsActivity.this, true);
                        break;
                }
            }
        });
    }

    private void showToday() {
        try {
            selectData(ymd.parse(ymd.format(new Date())), new Date());
        } catch (ParseException e) {
            mDatas = new ArrayList<>();
        }
        startTip = "今日";
        setStatistics();
        setAdapter(mDatas);
    }

    private void setAdapter(List<SellRecord> srs) {
        if (showType == 0) {
            List<Date> ds = new ArrayList<>();
            LinkedHashMap<Date, List<SellRecord>> map = new LinkedHashMap<>();
            for (SellRecord sr : srs) {
                Date d = sr.getSellDate();
                if (map.containsKey(d)) {
                    List<SellRecord> ss = map.get(d);
                    ss.add(sr);
                    map.put(d, ss);
                } else {
                    List<SellRecord> ss = new ArrayList<>();
                    ss.add(sr);
                    map.put(d, ss);
                    ds.add(d);
                }
            }
            mRecyclerView.setAdapter(new OrderAdapter(ds, map));
        } else {
            mRecyclerView.setAdapter(new StoreAdapter(srs));
        }
    }

    private void setStatistics() {
        int[] totals = setSales();

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(startTip);
        int s = ssb.length();
        ssb.append("销售额：" + App.app.showPrice(totals[0] + totals[1] + totals[2] + totals[3]));
        ssb.setSpan(new ItemClick(-1), s, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append("，其中");
        s = ssb.length();
        ssb.append("现金：" + App.app.showPrice(totals[0]));
        ssb.setSpan(new ItemClick(0), s, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append("、");
        s = ssb.length();
        ssb.append("微信：" + App.app.showPrice(totals[1]));
        ssb.setSpan(new ItemClick(1), s, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append("、");
        s = ssb.length();
        ssb.append("支付宝：" + App.app.showPrice(totals[2]));
        ssb.setSpan(new ItemClick(2), s, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append("、");
        s = ssb.length();
        ssb.append("刷卡：" + App.app.showPrice(totals[3]));
        ssb.setSpan(new ItemClick(3), s, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_warn.setText(ssb);
        tv_warn.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private int[] setSales() {
        int total[] = new int[4];
        for (SellRecord mData : mDatas) {
//            total[0] += mData.getPrice() * mData.getNumber();
            int payType = mData.getPayType();
            if (payType == 0) {
                total[0] += mData.getPrice() * mData.getNumber();
            } else if (payType == 1) {
                total[1] += mData.getPrice() * mData.getNumber();
            } else if (payType == 2) {
                total[2] += mData.getPrice() * mData.getNumber();
            } else if (payType == 3) {
                total[3] += mData.getPrice() * mData.getNumber();
            }
        }
        return total;
    }

    private void selectData(Date st, Date et) {
        toolbar.setTitle("销售记录");
        tempDatas.clear();
        mDatas = DBUtil.getDaoSession().getSellRecordDao().queryBuilder().where(SellRecordDao.Properties.SellDate.ge(st),
                SellRecordDao.Properties.SellDate.le(et)).orderDesc(SellRecordDao.Properties.SellDate).list();
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
        int[] totals = null;
        switch (item.getItemId()) {
            case R.id.action_all:
                toolbar.setTitle("销售记录");
                tempDatas.clear();
                mDatas = DBUtil.getDaoSession().getSellRecordDao().queryBuilder().orderDesc(SellRecordDao.Properties.SellDate).list();
                Log.d(TAG, "onOptionsItemSelected size=" + mDatas.size());
                setAdapter(mDatas);
                startTip = "所有";
                setStatistics();
                break;
            case R.id.action_day:
                try {
                    selectData(ymd.parse(ymd.format(new Date())), new Date());
                    setAdapter(mDatas);
                    startTip = "今日";
                    setStatistics();
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
                    setAdapter(mDatas);
                    startTip = "当月";
                    setStatistics();
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
                        setAdapter(mDatas);
                        startTip = ymdhm.format(startDate) + "到" + ymdhm.format(endDate) + "的";
                        setStatistics();
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

    class ItemClick extends ClickableSpan {

        int type;

        public ItemClick(int type) {
            this.type = type;
        }

        @Override
        public void onClick(View widget) {
            tempDatas.clear();
            if (type == -1) {
                toolbar.setTitle("销售记录");
                setAdapter(mDatas);
            } else {
                if (type == 0) {
                    toolbar.setTitle("销售记录-现金");
                } else if (type == 1) {
                    toolbar.setTitle("销售记录-微信");
                } else if (type == 2) {
                    toolbar.setTitle("销售记录-支付宝");
                } else {
                    toolbar.setTitle("销售记录-刷卡");
                }
                for (SellRecord mData : mDatas) {
                    if (mData.getPayType() == type) {
                        tempDatas.add(mData);
                    }
                }
                setAdapter(tempDatas);
            }
        }

        @Override
        public void updateDrawState(TextPaint tp) {
            super.updateDrawState(tp);
            // 设置超链接字体颜色
            tp.setColor(getResources().getColor(R.color.colorText));
            // 设置取消超链接下划线
            tp.setUnderlineText(false);
        }
    }

    @Override
    protected boolean searchDrug(String result) {
        if (TextUtils.isEmpty(result)) {
//            Snackbar.make(mRecyclerView, "条形码输入错误", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
            return false;
        }
        long code = Long.valueOf(result);
        List<SellRecord> sDatas = null;
        if (showType == 0) {
            sDatas = DBUtil.getDaoSession().getSellRecordDao().queryBuilder().where(SellRecordDao.Properties.SellDate.eq(code)).orderDesc(SellRecordDao.Properties.SellDate).list();
        } else {
            sDatas = DBUtil.getDaoSession().getSellRecordDao().queryBuilder().where(SellRecordDao.Properties.BarCode.eq(code)).orderDesc(SellRecordDao.Properties.SellDate).list();
        }
        if (sDatas != null && sDatas.size() > 0) {// 某个药品or某个订单
            setAdapter(sDatas);
            et_search.setText("");
            return true;
        } else {
            setAdapter(mDatas);
            return false;
        }
    }

    class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.MyViewHolder> {

        List<Date> ds;
        LinkedHashMap<Date, List<SellRecord>> map;

        public OrderAdapter(List<Date> ds, LinkedHashMap<Date, List<SellRecord>> map) {
            this.ds = ds;
            this.map = map;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(StatisticsActivity.this).inflate(R.layout.item_statistics, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Date d = ds.get(position);
            holder.tv_name.setText("" + d.getTime());
            List<SellRecord> srs = map.get(d);
            holder.tv_factory.setText(srs.size() + "种");
            holder.tv_code.setVisibility(View.GONE);
            holder.tv_num.setVisibility(View.GONE);
            holder.tv_time.setText(ymdhms.format(d));
            int payType = 0;
            int allPrice = 0;
            for (SellRecord record : srs) {
                payType = record.getPayType();
                allPrice += record.getPrice() * record.getNumber();
            }
            holder.tv_subPrice.setText(App.app.showPrice(allPrice));
            if (payType == 0) {
                holder.tv_payType.setText("现金");
            } else if (payType == 1) {
                holder.tv_payType.setText("微信");
            } else if (payType == 2) {
                holder.tv_payType.setText("支付宝");
            } else if (payType == 3) {
                holder.tv_payType.setText("刷卡");
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(StatisticsActivity.this, OrderActivity.class).putExtra("Date", d.getTime()));
                }
            });
            holder.iv_delete.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return ds.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout ll_all;
            TextView tv_name, tv_code, tv_factory, tv_num, tv_time, tv_subPrice, tv_payType;
            ImageView iv_delete;

            public MyViewHolder(View view) {
                super(view);
                tv_name = (TextView) view.findViewById(R.id.item_statistics_name);
                tv_code = (TextView) view.findViewById(R.id.item_statistics_code);
                tv_factory = (TextView) view.findViewById(R.id.item_statistics_factory);
                tv_num = (TextView) view.findViewById(R.id.item_statistics_num);
                tv_time = (TextView) view.findViewById(R.id.item_statistics_time);
                tv_subPrice = (TextView) view.findViewById(R.id.item_statistics_subPrice);
                tv_payType = (TextView) view.findViewById(R.id.item_statistics_paytype);
                tv_payType.setVisibility(View.VISIBLE);
                iv_delete = (ImageView) view.findViewById(R.id.item_statistics_delete);
                iv_delete.setVisibility(View.VISIBLE);
            }
        }
    }

    class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.MyViewHolder> {

        List<SellRecord> dis;

        public StoreAdapter(List<SellRecord> dis) {
            this.dis = dis;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(StatisticsActivity.this).inflate(R.layout.item_statistics, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final SellRecord sr = dis.get(position);
            long code = sr.getBarCode();
            holder.tv_code.setText("" + code);
            holder.tv_num.setText(sr.getNumber() + "*" + App.app.showPrice(sr.getPrice()));
            holder.tv_time.setText(ymdhms.format(sr.getSellDate()));
            holder.tv_subPrice.setText(App.app.showPrice(sr.getPrice() * sr.getNumber()));

            final int payType = sr.getPayType();
            if (payType == 0) {
                holder.tv_payType.setText("现金");
            } else if (payType == 1) {
                holder.tv_payType.setText("微信");
            } else if (payType == 2) {
                holder.tv_payType.setText("支付宝");
            } else if (payType == 3) {
                holder.tv_payType.setText("刷卡");
            }

            DrugInfo di = DBUtil.getDaoSession().getDrugInfoDao().load(code);
            holder.tv_name.setText(di.getName());
            holder.tv_factory.setText(di.getFactory());
            holder.iv_delete.setVisibility(View.VISIBLE);
            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final NiftyDialogBuilder delDia = NiftyDialogBuilder.getInstance(StatisticsActivity.this);
                    delDia.withTitle("确定删除该条销售记录？");
                    delDia.withButton1Text("取消", 0).setButton1Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            delDia.dismiss();
                        }
                    });
                    delDia.withButton2Text("确定", 0).setButton2Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                StoreList sl = DBUtil.getDaoSession().getStoreListDao().load(sr.getBarCode());
                                if (sl == null) {
                                    sl = new StoreList();
                                    sl.setPrice(sr.getPrice());
                                    sl.setNumber(0);
                                    sl.setWarnNumber(0);
                                }
                                sl.setNumber(sl.getNumber() + sr.getNumber());
                                DBUtil.getDaoSession().getStoreListDao().insertOrReplace(sl);
                                DBUtil.getDaoSession().getSellRecordDao().delete(sr);
                                if (tempDatas.isEmpty()) {
                                    mDatas.remove(position);
                                } else {
                                    tempDatas.remove(position);
                                    for (int i = 0; i < mDatas.size(); i++) {
                                        if (mDatas.get(i).getId() == sr.getId()) {
                                            mDatas.remove(i);
                                            break;
                                        }
                                    }
                                }
                                setStatistics();
                                notifyDataSetChanged();
                                delDia.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    delDia.withMessage(null).withDuration(400);
                    delDia.isCancelable(false);
                    delDia.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return dis.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_name, tv_code, tv_factory, tv_num, tv_time, tv_subPrice, tv_payType;
            ImageView iv_delete;

            public MyViewHolder(View view) {
                super(view);
                tv_name = (TextView) view.findViewById(R.id.item_statistics_name);
                tv_code = (TextView) view.findViewById(R.id.item_statistics_code);
                tv_factory = (TextView) view.findViewById(R.id.item_statistics_factory);
                tv_num = (TextView) view.findViewById(R.id.item_statistics_num);
                tv_time = (TextView) view.findViewById(R.id.item_statistics_time);
                tv_subPrice = (TextView) view.findViewById(R.id.item_statistics_subPrice);
                tv_payType = (TextView) view.findViewById(R.id.item_statistics_paytype);
                tv_payType.setVisibility(View.VISIBLE);
                iv_delete = (ImageView) view.findViewById(R.id.item_statistics_delete);
                iv_delete.setVisibility(View.VISIBLE);
            }
        }
    }

}
