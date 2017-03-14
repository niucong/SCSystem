package com.niucong.scsystem;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.command.GpCom;
import com.gprinter.command.GpUtils;
import com.gprinter.command.LabelCommand;
import com.gprinter.io.GpDevice;
import com.gprinter.service.GpPrintService;
import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.SellRecord;
import com.niucong.scsystem.dao.SellRecordDao;
import com.niucong.scsystem.dao.StoreList;
import com.niucong.scsystem.printer.PrinterConnectDialog;
import com.niucong.scsystem.util.FileUtil;
import com.niucong.scsystem.util.PrintUtil;
import com.niucong.scsystem.view.DividerItemDecoration;
import com.niucong.scsystem.view.NiftyDialogBuilder;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static com.gprinter.service.GpPrintService.CONNECT_STATUS;
import static com.niucong.scsystem.app.App.app;
import static com.niucong.scsystem.dao.DBUtil.getDaoSession;
import static com.niucong.scsystem.printer.ListViewAdapter.DEBUG_TAG;

public class MainActivity extends BasicActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private TextView tv_total, nav_total, nav_warn, nav_time;
    private Spinner sp;
    private RadioGroup rg;
    private CheckBox cb;

    private List<SellRecord> uRecords;
    private HomeAdapter mAdapter;

    private int payType = 0;

    int mHourOfDay, mMinute;

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

        MenuItem nav_time = navigationView.getMenu().findItem(R.id.nav_time);
        mHourOfDay = App.app.share.getIntMessage("SC", "hourOfDay", 0);
        mMinute = App.app.share.getIntMessage("SC", "minute", 0);
        String tStr = "设置对账时间（";
        if (mHourOfDay < 10) {
            tStr += "0" + mHourOfDay;
        } else {
            tStr += mHourOfDay;
        }
        if (mMinute < 10) {
            tStr += "：0" + mMinute;
        } else {
            tStr += "：" + mMinute;
        }
        nav_time.setTitle(tStr + "）");

        uRecords = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.main_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new HomeAdapter());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        setSearchBar();

        cb = (CheckBox) findViewById(R.id.main_print);
        cb.setVisibility(View.VISIBLE);
        sp = (Spinner) findViewById(R.id.main_spinner);
        rg = (RadioGroup) findViewById(R.id.main_pay);
        setPayType();
        tv_total = (TextView) findViewById(R.id.main_total);
        findViewById(R.id.main_btn).setOnClickListener(this);
        mRecyclerView.requestFocus();

        connection();
        registerReceiver(mBroadcastReceiver, new IntentFilter(GpCom.ACTION_DEVICE_REAL_STATUS));
    }

    @Override
    protected void setSearchBar() {
//        super.setSearchBar();
        et_search = (AutoCompleteTextView) findViewById(R.id.search_et);
        final ImageView iv_delete = (ImageView) findViewById(R.id.search_delete);
        final ImageView iv_scan = (ImageView) findViewById(R.id.search_scan);

        List<DrugInfo> list = DBUtil.getDaoSession().getDrugInfoDao().loadAll();
        SearchAdapter searchAdapter = new SearchAdapter(MainActivity.this, list);
        et_search.setAdapter(searchAdapter);
//        et_search.setThreshold(2);

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

                    try {
                        Long.valueOf(str);
                        if (str.length() > 12) {
                            searchDrug(str);
                        }
                    } catch (NumberFormatException e) {
                        // TODO
//                        List<DrugInfo> list = DBUtil.getDaoSession().getDrugInfoDao().queryBuilder().whereOr(DrugInfoDao.Properties.Name.like("%" + str + "%"),
//                                DrugInfoDao.Properties.NamePY.like("%" + str.toLowerCase() + "%"), DrugInfoDao.Properties.NamePYF.like("%" + str.toLowerCase() + "%")).list();
//                        Log.i("MainActivity", "size=" + list.size());
                    }
                } else {
                    iv_delete.setVisibility(View.GONE);
                    iv_scan.setVisibility(View.VISIBLE);
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

    class SearchAdapter extends BaseAdapter implements Filterable {

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
                holder.tv_factory = (TextView) convertView.findViewById(R.id.item_search_factory);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final DrugInfo di = list.get(position);
            holder.tv_name.setText(di.getName());
            holder.tv_factory.setText(di.getFactory());
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchDrug("" + di.getBarCode());
                }
            });
            return convertView;
        }

        class ViewHolder {
            TextView tv_name, tv_factory;
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

        //        @Override
//        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item_search, parent, false));
//            return holder;
//        }
//
//        @Override
//        public void onBindViewHolder(final MyViewHolder holder, final int position) {
//            DrugInfo di = list.get(position);
//            holder.tv_name.setText(di.getName());
//            holder.tv_factory.setText(di.getFactory());
//        }
//
//        @Override
//        public int getItemCount() {
//            return list.size();
//        }
//
//        class MyViewHolder extends RecyclerView.ViewHolder {
//            TextView tv_name, tv_factory;
//
//            public MyViewHolder(View view) {
//                super(view);
//                tv_name = (TextView) view.findViewById(R.id.item_search_name);
//                tv_factory = (TextView) view.findViewById(R.id.item_search_factory);
//            }
//        }
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
            SimpleDateFormat ymdhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String ms = ymd.format(new Date());
            if (mHourOfDay < 10) {
                ms += " 0" + mHourOfDay;
            } else {
                ms += " " + mHourOfDay;
            }
            if (mMinute < 10) {
                ms += ":0" + mMinute;
            } else {
                ms += ":" + mMinute;
            }
            ms += ":00";
            Date ed = ymdhms.parse(ms);// 今天结账时间
            Date sd = new Date(ed.getTime() - 24 * 60 * 60 * 1000);// 昨天结账时间
            List<SellRecord> tDatas = getDaoSession().getSellRecordDao().queryBuilder().where(SellRecordDao.Properties.SellDate.ge(sd), SellRecordDao.Properties.SellDate.le(ed)).orderDesc(SellRecordDao.Properties.SellDate).list();
            int total = 0;
            for (SellRecord mData : tDatas) {
                total += mData.getPrice() * mData.getNumber();
            }
            Log.d(TAG, "setNavTip total=" + total);
            nav_total.setText("今日销售额：" + app.showPrice(total));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<StoreList> wDatas = getDaoSession().getStoreListDao().loadAll();
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

                    StoreList si = getDaoSession().getStoreListDao().load(sr.getBarCode());
                    if (sr.getPrice() < 0) {
                        sellNum = -sellNum;
                    }
                    si.setNumber(si.getNumber() - sellNum);
                    uStores.add(si);
                }
                getDaoSession().getStoreListDao().insertOrReplaceInTx(uStores);
                getDaoSession().getSellRecordDao().insertOrReplaceInTx(sRecords);

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
                // TODO 弹框提示是否打印小票
                if (cb.isChecked()) {
                    PrintUtil.printStick(mGpService, sRecords);
                }
                cb.setChecked(false);
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
        StoreList di = getDaoSession().getStoreListDao().load(code);
        // TODO
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
        tv_total.setText("合计：" + app.showPrice(total));
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
            holder.tv_price.setText(app.showPrice(sr.getPrice()));
            holder.tv_num.setText("" + sr.getNumber());
            holder.tv_subPrice.setText("小计：" + app.showPrice(sr.getPrice() * sr.getNumber()));

            DrugInfo di = getDaoSession().getDrugInfoDao().load(code);
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
    public boolean onNavigationItemSelected(final MenuItem item) {
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
        } else if (id == R.id.nav_camera) {// 连接打印机
            if (mGpService == null) {
                Toast.makeText(this, "Print Service is not start, please check it", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(DEBUG_TAG, "openPortConfigurationDialog ");
                Intent intent = new Intent(this, PrinterConnectDialog.class);
                boolean[] state = getConnectState();
                intent.putExtra(CONNECT_STATUS, state);
                this.startActivity(intent);
            }
        } else if (id == R.id.nav_time) {// 设置对账时间
            Calendar time = Calendar.getInstance();
            Dialog timeDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mHourOfDay = hourOfDay;
                    mMinute = minute;
                    String tStr = "设置对账时间（";
                    if (hourOfDay < 10) {
                        tStr += "0" + hourOfDay;
                    } else {
                        tStr += hourOfDay;
                    }
                    if (minute < 10) {
                        tStr += "：0" + minute;
                    } else {
                        tStr += "：" + minute;
                    }
                    item.setTitle(tStr + "）");
                    app.share.saveIntMessage("SC", "hourOfDay", hourOfDay);
                    app.share.saveIntMessage("SC", "minute", minute);
                }
            }, mHourOfDay, mMinute, true);
            timeDialog.setTitle("设置每天对账时间");
            timeDialog.show();
        } else if (id == R.id.nav_data) {// 导入/导出数据
            settingDialog(1);
        } else if (id == R.id.nav_camera) {// 设置摄像头
            settingDialog(0);
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
            if (app.share.getIntMessage("SC", "CameraId", 0) == 0) {
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
                        app.share.saveIntMessage("SC", "CameraId", 0);
                        Snackbar.make(mRecyclerView, "已切换到后置摄像头", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        app.share.saveIntMessage("SC", "CameraId", 1);
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
                                    final boolean flag = new FileUtil().copySDcradToDB(MainActivity.this);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (flag) {
                                                Snackbar.make(mRecyclerView, "数据导入成功", Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            } else {
                                                Snackbar.make(mRecyclerView, "数据导入失败", Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
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
                                final boolean flag = new FileUtil().copyDBToSDcrad(MainActivity.this);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (flag) {
                                            Snackbar.make(mRecyclerView, "数据导出成功", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        } else {
                                            Snackbar.make(mRecyclerView, "数据导出失败", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        }
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

    private int mPrinterIndex = 0;
    private GpService mGpService;
    private PrinterServiceConnection conn = null;

    private static final int MAIN_QUERY_PRINTER_STATUS = 0xfe;
    private static final int REQUEST_PRINT_LABEL = 0xfd;
    private static final int REQUEST_PRINT_RECEIPT = 0xfc;

    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {

            Log.i(DEBUG_TAG, "onServiceDisconnected() called");
            mGpService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mGpService = GpService.Stub.asInterface(service);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // GpCom.ACTION_DEVICE_REAL_STATUS 为广播的IntentFilter
            if (action.equals(GpCom.ACTION_DEVICE_REAL_STATUS)) {

                // 业务逻辑的请求码，对应哪里查询做什么操作
                int requestCode = intent.getIntExtra(GpCom.EXTRA_PRINTER_REQUEST_CODE, -1);
                // 判断请求码，是则进行业务操作
                if (requestCode == MAIN_QUERY_PRINTER_STATUS) {

                    int status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16);
                    String str;
                    if (status == GpCom.STATE_NO_ERR) {
                        str = "打印机正常";
                    } else {
                        str = "打印机 ";
                        if ((byte) (status & GpCom.STATE_OFFLINE) > 0) {
                            str += "脱机";
                        }
                        if ((byte) (status & GpCom.STATE_PAPER_ERR) > 0) {
                            str += "缺纸";
                        }
                        if ((byte) (status & GpCom.STATE_COVER_OPEN) > 0) {
                            str += "打印机开盖";
                        }
                        if ((byte) (status & GpCom.STATE_ERR_OCCURS) > 0) {
                            str += "打印机出错";
                        }
                        if ((byte) (status & GpCom.STATE_TIMES_OUT) > 0) {
                            str += "查询超时";
                        }
                    }

                    Toast.makeText(getApplicationContext(), "打印机：" + mPrinterIndex + " 状态：" + str, Toast.LENGTH_SHORT)
                            .show();
                } else if (requestCode == REQUEST_PRINT_LABEL) {
                    int status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16);
                    if (status == GpCom.STATE_NO_ERR) {
                        sendLabel();
                    } else {
                        Toast.makeText(MainActivity.this, "query printer status error", Toast.LENGTH_SHORT).show();
                    }
                } else if (requestCode == REQUEST_PRINT_RECEIPT) {
                    int status = intent.getIntExtra(GpCom.EXTRA_PRINTER_REAL_STATUS, 16);
                    if (status == GpCom.STATE_NO_ERR) {
                        sendReceipt();
                    } else {
                        Toast.makeText(MainActivity.this, "query printer status error", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    void sendReceipt() {
        EscCommand esc = new EscCommand();
        esc.addInitializePrinter();
        esc.addPrintAndFeedLines((byte) 3);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);// 设置打印居中
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);// 设置为倍高倍宽
        esc.addText("Sample\n"); // 打印文字
        esc.addPrintAndLineFeed();

		/* 打印文字 */
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);// 取消倍高倍宽
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);// 设置打印左对齐
        esc.addText("Print text\n"); // 打印文字
        esc.addText("Welcome to use SMARNET printer!\n"); // 打印文字

		/* 打印繁体中文 需要打印机支持繁体字库 */
        String message = "佳博智匯票據打印機\n";
        // esc.addText(message,"BIG5");
        esc.addText(message, "GB2312");
        esc.addPrintAndLineFeed();

		/* 绝对位置 具体详细信息请查看GP58编程手册 */
        esc.addText("智汇");
        esc.addSetHorAndVerMotionUnits((byte) 7, (byte) 0);
        esc.addSetAbsolutePrintPosition((short) 6);
        esc.addText("网络");
        esc.addSetAbsolutePrintPosition((short) 10);
        esc.addText("设备");
        esc.addPrintAndLineFeed();

		/* 打印图片 */
        // esc.addText("Print bitmap!\n"); // 打印文字
        // Bitmap b = BitmapFactory.decodeResource(getResources(),
        // R.drawable.gprinter);
        // esc.addRastBitImage(b, b.getWidth(), 0); // 打印图片

		/* 打印一维条码 */
        esc.addText("Print code128\n"); // 打印文字
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);//
        // 设置条码可识别字符位置在条码下方
        esc.addSetBarcodeHeight((byte) 60); // 设置条码高度为60点
        esc.addSetBarcodeWidth((byte) 1); // 设置条码单元宽度为1
        esc.addCODE128(esc.genCodeB("SMARNET")); // 打印Code128码
        esc.addPrintAndLineFeed();

		/*
         * QRCode命令打印 此命令只在支持QRCode命令打印的机型才能使用。 在不支持二维码指令打印的机型上，则需要发送二维条码图片
		 */
        esc.addText("Print QRcode\n"); // 打印文字
        esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31); // 设置纠错等级
        esc.addSelectSizeOfModuleForQRCode((byte) 3);// 设置qrcode模块大小
        esc.addStoreQRCodeData("www.smarnet.cc");// 设置qrcode内容
        esc.addPrintQRCode();// 打印QRCode
        esc.addPrintAndLineFeed();

		/* 打印文字 */
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);// 设置打印左对齐
        esc.addText("Completed!\r\n"); // 打印结束
        esc.addGeneratePlus(LabelCommand.FOOT.F5, (byte) 255, (byte) 255);
        // esc.addGeneratePluseAtRealtime(LabelCommand.FOOT.F2, (byte) 8);

        esc.addPrintAndFeedLines((byte) 8);

        Vector<Byte> datas = esc.getCommand(); // 发送数据
        byte[] bytes = GpUtils.ByteTo_byte(datas);
        String sss = Base64.encodeToString(bytes, Base64.DEFAULT);
        int rs;
        try {
            rs = mGpService.sendEscCommand(mPrinterIndex, sss);
            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rs];
            if (r != GpCom.ERROR_CODE.SUCCESS) {
                Toast.makeText(getApplicationContext(), GpCom.getErrorText(r), Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    void sendLabel() {
        LabelCommand tsc = new LabelCommand();
        tsc.addSize(60, 60); // 设置标签尺寸，按照实际尺寸设置
        tsc.addGap(0); // 设置标签间隙，按照实际尺寸设置，如果为无间隙纸则设置为0
        tsc.addDirection(LabelCommand.DIRECTION.BACKWARD, LabelCommand.MIRROR.NORMAL);// 设置打印方向
        tsc.addReference(0, 0);// 设置原点坐标
        tsc.addTear(EscCommand.ENABLE.ON); // 撕纸模式开启
        tsc.addCls();// 清除打印缓冲区
        // 绘制简体中文
        tsc.addText(20, 20, LabelCommand.FONTTYPE.SIMPLIFIED_CHINESE, LabelCommand.ROTATION.ROTATION_0, LabelCommand.FONTMUL.MUL_1, LabelCommand.FONTMUL.MUL_1,
                "Welcome to use SMARNET printer!");
        // 绘制图片
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.gprinter);
        tsc.addBitmap(20, 50, LabelCommand.BITMAP_MODE.OVERWRITE, b.getWidth(), b);

        tsc.addQRCode(250, 80, LabelCommand.EEC.LEVEL_L, 5, LabelCommand.ROTATION.ROTATION_0, " www.smarnet.cc");
        // 绘制一维条码
        tsc.add1DBarcode(20, 250, LabelCommand.BARCODETYPE.CODE128, 100, LabelCommand.READABEL.EANBEL, LabelCommand.ROTATION.ROTATION_0, "SMARNET");
        tsc.addPrint(1, 1); // 打印标签
        tsc.addSound(2, 100); // 打印标签后 蜂鸣器响
        tsc.addCashdrwer(LabelCommand.FOOT.F5, 255, 255);
        Vector<Byte> datas = tsc.getCommand(); // 发送数据
        byte[] bytes = GpUtils.ByteTo_byte(datas);
        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        int rel;
        try {
            rel = mGpService.sendLabelCommand(mPrinterIndex, str);
            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
            if (r != GpCom.ERROR_CODE.SUCCESS) {
                Toast.makeText(getApplicationContext(), GpCom.getErrorText(r), Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void connection() {
        conn = new PrinterServiceConnection();
        Log.i(DEBUG_TAG, "connection");
        Intent intent = new Intent(this, GpPrintService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService
    }

    public boolean[] getConnectState() {
        boolean[] state = new boolean[GpPrintService.MAX_PRINTER_CNT];
        for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {
            state[i] = false;
        }
        for (int i = 0; i < GpPrintService.MAX_PRINTER_CNT; i++) {
            try {
                if (mGpService.getPrinterConnectStatus(i) == GpDevice.STATE_CONNECTED) {
                    state[i] = true;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return state;
    }
}
