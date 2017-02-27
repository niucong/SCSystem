package com.niucong.scsystem;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.SellRecord;
import com.niucong.scsystem.dao.SellRecordDao;
import com.niucong.scsystem.dao.StoreList;
import com.niucong.scsystem.util.FileUtil;
import com.niucong.scsystem.view.DividerItemDecoration;
import com.niucong.scsystem.view.NiftyDialogBuilder;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends BasicActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private TextView tv_total, nav_total, nav_warn;
    private Spinner sp;
    private RadioGroup rg;

    private List<SellRecord> uRecords;
    private HomeAdapter mAdapter;

    private int payType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        MobclickAgent.onEvent(this, "0");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        nav_total = (TextView) headerView.findViewById(R.id.nav_total);
        nav_warn = (TextView) headerView.findViewById(R.id.nav_warn);

        uRecords = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.main_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new HomeAdapter());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        setSearchBar();

        sp = (Spinner) findViewById(R.id.main_spinner);
        rg = (RadioGroup) findViewById(R.id.main_pay);
        setPayType();
        tv_total = (TextView) findViewById(R.id.main_total);
        findViewById(R.id.main_btn).setOnClickListener(this);
        mRecyclerView.requestFocus();

    }

    private void setPayType() {
        if (isTablet) {
            rg.setVisibility(View.VISIBLE);
            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.main_cash:
                            payType = 0;
                            break;
                        case R.id.main_wechat:
                            payType = 1;
                            break;
                        case R.id.main_alipay:
                            payType = 2;
                            break;
                        case R.id.main_card:
                            payType = 3;
                            break;
                    }
                }
            });
        } else {
            sp.setVisibility(View.VISIBLE);
            List<String> list = new ArrayList<String>();
            list.add("现金");
            list.add("微信");
            list.add("支付宝");
            list.add("刷卡");
            ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp.setAdapter(adapter);
            sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    payType = position;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        setNavTip();
//        setAutoComplete();
    }

    private void setNavTip() {
        try {
            SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
            List<SellRecord> tDatas = DBUtil.getDaoSession().getSellRecordDao().queryBuilder().where(SellRecordDao.Properties.SellDate.ge(ymd.parse(ymd.format(new Date())))).orderDesc(SellRecordDao.Properties.SellDate).list();
            int total = 0;
            for (SellRecord mData : tDatas) {
                total += mData.getPrice() * mData.getNumber();
            }
            Log.d(TAG, "setNavTip total=" + total);
            nav_total.setText("今日销售额：" + App.app.showPrice(total));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<StoreList> wDatas = DBUtil.getDaoSession().getStoreListDao().loadAll();
        int warn = 0;
        for (StoreList mData : wDatas) {
            if (mData.getNumber() < mData.getWarnNumber()) {
                warn++;
            }
        }
        Log.d(TAG, "setNavTip warn=" + warn);
        if (warn == 0) {
            nav_warn.setVisibility(View.GONE);
        } else {
            nav_warn.setVisibility(View.VISIBLE);
            nav_warn.setText(warn + " 种需要进货");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.main_btn:
                // 结算
                Log.d(TAG, "onClick size=" + uRecords.size());
                if (uRecords.size() == 0) {
                    return;
                }
                List<StoreList> uStores = new ArrayList<>();
                List<SellRecord> sRecords = new ArrayList<>();
                Date date = new Date();
                for (SellRecord sr : uRecords) {
                    int sellNum = sr.getNumber();
                    sr.setSellDate(date);
                    sr.setPayType(payType);
                    sRecords.add(sr);

                    StoreList si = DBUtil.getDaoSession().getStoreListDao().load(sr.getBarCode());
                    if (sr.getPrice() < 0) {
                        sellNum = -sellNum;
                    }
                    si.setNumber(si.getNumber() - sellNum);
                    uStores.add(si);
                }
                DBUtil.getDaoSession().getStoreListDao().insertOrReplaceInTx(uStores);
                DBUtil.getDaoSession().getSellRecordDao().insertOrReplaceInTx(sRecords);

                uRecords.clear();
                mAdapter.notifyDataSetChanged();
                if (isTablet) {
                    rg.check(R.id.main_cash);
                } else {
                    sp.setSelection(0);
                }
                tv_total.setText("合计：0.0");
                Snackbar.make(mRecyclerView, "结算成功", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                setNavTip();
                break;
        }
    }

    @Override
    protected boolean searchDrug(String result) {
        Log.d(TAG, "searchDrug code=" + result);
        if (TextUtils.isEmpty(result)) {
//            Snackbar.make(tv_total, "条形码输入错误", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
            return false;
        }
        long code = Long.valueOf(result);
        StoreList di = DBUtil.getDaoSession().getStoreListDao().load(code);
        if (di != null) {
            SellRecord sr = null;
            long c = di.getBarCode();
            if (uRecords.size() > 0) {
                for (SellRecord uRecord : uRecords) {
                    if (c == uRecord.getBarCode()) {
                        sr = uRecord;
                        break;
                    }
                }
            }
            if (sr == null) {
                sr = new SellRecord();
                sr.setBarCode(di.getBarCode());
                sr.setNumber(1);
                sr.setPrice(di.getPrice());
                sr.setSellDate(new Date());
            } else {
                sr.setNumber(sr.getNumber() + 1);
                uRecords.remove(sr);
            }
            uRecords.add(0, sr);
            mAdapter.notifyDataSetChanged();
            getTotalPrice();
            et_search.setText("");
            return true;
        } else {
            Snackbar.make(mRecyclerView, "该药品不在库存中,请先添加入库", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        return false;
    }

    private void getTotalPrice() {
        int total = 0;
        for (SellRecord di : uRecords) {
            total += di.getPrice() * di.getNumber();
        }
        tv_total.setText("合计：" + App.app.showPrice(total));
    }

    class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder> {
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item_home, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            final SellRecord sr = uRecords.get(position);
            final long code = sr.getBarCode();
            holder.tv_code.setText("" + code);
            holder.tv_price.setText(App.app.showPrice(sr.getPrice()));
            holder.tv_num.setText("" + sr.getNumber());
            holder.tv_subPrice.setText("小计：" + App.app.showPrice(sr.getPrice() * sr.getNumber()));

            DrugInfo di = DBUtil.getDaoSession().getDrugInfoDao().load(code);
            holder.tv_name.setText(di.getName());
            holder.tv_factory.setText(di.getFactory());

//            holder.tv_price.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    final NiftyDialogBuilder submitDia = NiftyDialogBuilder.getInstance(MainActivity.this);
//                    final EditText et = new EditText(MainActivity.this);
//                    et.setBackgroundResource(0);
//                    et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
//                    et.setGravity(Gravity.CENTER);
//                    et.setText(App.app.showPrice(sr.getPrice()));
//
//                    submitDia.withTitle("调整价格");
//                    submitDia.withButton1Text("取消", 0).setButton1Click(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            submitDia.dismiss();
//                        }
//                    });
//                    submitDia.withButton2Text("确定", 0).setButton2Click(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            int price = App.app.savePrice(et.getText().toString());
//                            if (price == 0) {
//                                Snackbar.make(mRecyclerView, "价格输入有误", Snackbar.LENGTH_LONG)
//                                        .setAction("Action", null).show();
//                            } else {
//                                sr.setPrice(price);
//                                uRecords.remove(position);
//                                uRecords.add(position, sr);
//                                holder.tv_price.setText(App.app.showPrice(sr.getPrice()));
//                                holder.tv_subPrice.setText("小计：" + App.app.showPrice(sr.getPrice() * sr.getNumber()));
//                                getTotalPrice();
//                                submitDia.dismiss();
//                            }
//                        }
//                    });
//                    submitDia.setCustomView(et, MainActivity.this);// "请选择查询日期"
//                    submitDia.withMessage(null).withDuration(400);
//                    submitDia.isCancelable(false);
//                    submitDia.show();
//                }
//            });

            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uRecords.remove(sr);
                    notifyDataSetChanged();
                    getTotalPrice();
                }
            });

            holder.tv_remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int num = sr.getNumber();
                    if (num > 1) {
                        sr.setNumber(num - 1);
                        notifyDataSetChanged();
                        getTotalPrice();
                    }
                }
            });

            holder.tv_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int num = sr.getNumber();
                    sr.setNumber(num + 1);
                    notifyDataSetChanged();
                    getTotalPrice();
                }
            });
        }

        @Override
        public int getItemCount() {
            return uRecords.size();
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
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_enter) {// 添加库存
            startActivity(new Intent(this, EnterActivity.class));
        } else if (id == R.id.nav_stock) {// 查看库存
            startActivity(new Intent(this, StoreActivity.class));
        } else if (id == R.id.nav_enterList) {// 进货记录
            startActivity(new Intent(this, EnterRecordActivity.class));
        } else if (id == R.id.nav_statistics) {// 销售记录
            startActivity(new Intent(this, StatisticsActivity.class));
        } else if (id == R.id.nav_destory) {// 报损处理(自用、损坏或过期、退还商家)
            startActivity(new Intent(this, DestoryActivity.class));
        } else if (id == R.id.nav_camera) {// 设置摄像头
            settingDialog(0);
        } else if (id == R.id.nav_data) {// 导入/导出数据
            settingDialog(1);
        } else if (id == R.id.nav_help) {// 使用帮助
            startActivity(new Intent(this, WebActivity.class));
        } else if (id == R.id.nav_about) {// 关于
            startActivity(new Intent(this, AboutActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 设置对话框：0摄像头、1导入/导出数据
     *
     * @param type
     */
    private void settingDialog(final int type) {
        final NiftyDialogBuilder submitDia = NiftyDialogBuilder.getInstance(this);
        View settingView = LayoutInflater.from(this).inflate(R.layout.dialog_setting, null);
        final RadioButton rb1 = (RadioButton) settingView.findViewById(R.id.radioButton1);
        RadioButton rb2 = (RadioButton) settingView.findViewById(R.id.radioButton2);

        final String SDCardPath = Environment.getExternalStorageDirectory() + File.separator;

        if (type == 0) {
            submitDia.withTitle("设置扫码摄像头");
            if (App.app.share.getIntMessage("SC", "CameraId", 0) == 0) {
                rb1.setChecked(true);
            } else {
                rb2.setChecked(true);
            }
        } else {
            submitDia.withTitle("导入/导出数据");
            rb1.setText("导入数据");
            rb2.setText("导出数据");
            final TextView tv_tip = (TextView) settingView.findViewById(R.id.dialog_tip);
            tv_tip.setVisibility(View.VISIBLE);
            tv_tip.setText("请确保" + SDCardPath + "目录下有shunchang文件，同时应用内数据将被覆盖！");
            ((RadioGroup) settingView.findViewById(R.id.radioGroup)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case R.id.radioButton1:
                            tv_tip.setText("请确保" + SDCardPath + "目录下有shunchang文件，同时应用内数据将被覆盖！");
                            break;
                        case R.id.radioButton2:
                            tv_tip.setText("数据将导出到" + SDCardPath + "目录下");
                            break;
                    }
                }
            });
            submitDia.withButton1Text("取消", 0).setButton1Click(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    submitDia.dismiss();
                }
            });
        }

        submitDia.withButton2Text("确定", 0).setButton2Click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 0) {
                    if (rb1.isChecked()) {
                        App.app.share.saveIntMessage("SC", "CameraId", 0);
                        Snackbar.make(mRecyclerView, "已切换到后置摄像头", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        App.app.share.saveIntMessage("SC", "CameraId", 1);
                        Snackbar.make(mRecyclerView, "已切换到前置摄像头", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } else {
                    if (rb1.isChecked()) {
                        File f = new File(SDCardPath + "shunchang");
                        if (f.exists()) {
                            Snackbar.make(mRecyclerView, "正在导入数据", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                            new Thread() {
                                @Override
                                public void run() {
                                    new FileUtil().copySDcradToDB(MainActivity.this);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Snackbar.make(mRecyclerView, "数据导入完成", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
                                    });
                                }
                            }.start();
                        } else {
                            Snackbar.make(mRecyclerView, "所导入的数据文件不存在", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    } else {
                        Snackbar.make(mRecyclerView, "正在导出数据", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        new Thread() {
                            @Override
                            public void run() {
                                new FileUtil().copyDBToSDcrad(MainActivity.this);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Snackbar.make(mRecyclerView, "数据导出完成", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }
                                });
                            }
                        }.start();
                    }
                }
                submitDia.dismiss();
            }
        });
        submitDia.setCustomView(settingView, this);
        submitDia.withMessage(null).withDuration(400);
        submitDia.isCancelable(false);
        submitDia.show();
    }
}
