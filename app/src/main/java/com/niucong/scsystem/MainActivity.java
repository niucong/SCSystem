package com.niucong.scsystem;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.command.GpCom;
import com.gprinter.command.GpUtils;
import com.gprinter.command.LabelCommand;
import com.gprinter.io.GpDevice;
import com.gprinter.service.GpPrintService;
//import com.niucong.scsystem.andserver.BluetoothChatService;
import com.niucong.scsystem.andserver.ServerManager;
//import com.niucong.scsystem.andserver.controller.ApiController;
import com.niucong.scsystem.andserver.util.Logger;
import com.niucong.scsystem.andserver.util.NetUtils;
import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.SellRecord;
import com.niucong.scsystem.dao.StoreList;
import com.niucong.scsystem.printer.PrinterConnectDialog;
import com.niucong.scsystem.util.PrintUtil;
import com.niucong.scsystem.view.DividerItemDecoration;
import com.yanzhenjie.loading.dialog.LoadingDialog;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static com.niucong.scsystem.app.App.app;
import static com.niucong.scsystem.dao.DBUtil.getDaoSession;

public class MainActivity extends BasicActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private TextView tv_total, nav_title, nav_total, nav_warn, main_ip;// , nav_time
    //    private Spinner sp;
//    private RadioGroup rg;
    private CheckBox cb;

    private List<SellRecord> uRecords;
    private HomeAdapter mAdapter;

    private int payType = 0;

//    int mHourOfDay, mMinute;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mServerManager.unRegister();
//        bluetoothChatService.stop();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
//        MobclickAgent.onEvent(this, "0");

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
        nav_title = (TextView) headerView.findViewById(R.id.nav_title);
        nav_total = (TextView) headerView.findViewById(R.id.nav_total);
        nav_warn = (TextView) headerView.findViewById(R.id.nav_warn);

//        MenuItem nav_time = navigationView.getMenu().findItem(R.id.nav_time);
//        mHourOfDay = App.app.share.getIntMessage("SC", "hourOfDay", 0);
//        mMinute = App.app.share.getIntMessage("SC", "minute", 0);
//        String tStr = "设置对账时间（";
//        if (mHourOfDay < 10) {
//            tStr += "0" + mHourOfDay;
//        } else {
//            tStr += mHourOfDay;
//        }
//        if (mMinute < 10) {
//            tStr += "：0" + mMinute;
//        } else {
//            tStr += "：" + mMinute;
//        }
//        nav_time.setTitle(tStr + "）");

        uRecords = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.main_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new HomeAdapter());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        app.list = getDaoSession().getDrugInfoDao().loadAll();
        setSearchBar(this, true);

        cb = (CheckBox) findViewById(R.id.main_print);
        cb.setVisibility(View.VISIBLE);
//        sp = (Spinner) findViewById(R.id.main_spinner);
//        rg = (RadioGroup) findViewById(R.id.main_pay);
//        setPayType();
        main_ip = (TextView) findViewById(R.id.main_ip);
        tv_total = (TextView) findViewById(R.id.main_total);
        findViewById(R.id.main_btn).setOnClickListener(this);
        mRecyclerView.requestFocus();

        connection();
        registerReceiver(mBroadcastReceiver, new IntentFilter(GpCom.ACTION_DEVICE_REAL_STATUS));

        et_search.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (KeyEvent.ACTION_DOWN == event.getAction()) {
//                    Log.d(TAG, "onCreate keyCode=" + keyCode);
                    if (KeyEvent.KEYCODE_ENTER == keyCode) {
                        if (TextUtils.isEmpty(et_search.getText().toString())) {
                            //处理事件
                            sendOrder();
                            return true;
                        }
                    } else if (keyCode == 111) {
                        uRecords.clear();
                        mAdapter.notifyDataSetChanged();
                        tv_total.setText("合计：0.0");
                        return true;
                    }
                }
                return false;
            }
        });

//        apiController = new ApiController();
        // 开启wifi服务
        mServerManager = new ServerManager(this);
        mServerManager.register();
//        mServerManager.startServer();

//        // 开启蓝牙服务
//        bluetoothChatService = BluetoothChatService.getInstance(handler);
//        bluetoothChatService.start();

        // + "\n\n蓝牙地址：" + getBtAddressByReflection()
        nav_title.setText("IP地址：" + getIpAddress());
        main_ip.setText(getIpAddress());

    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (event.getDeviceId() == KeyCharacterMap.VIRTUAL_KEYBOARD) {//如果是虚拟键盘则不截获输入事件
//            return false;
//        }
//        InputDevice inputDevice = InputDevice.getDevice(event.getDeviceId());
//        Log.e("key", "onKeyDown: InputDevice:name=" +  inputDevice.getName()+",productId="+inputDevice.getProductId()+",VendorId="+ inputDevice.getVendorId());
//        Log.e("key", "onKeyDown: keyCode=" + keyCode + "String=" + KeyEvent.keyCodeToString(keyCode));
////        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {// 按下遥控器左键
////
////            return true;
////        }
////        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {// 按下遥控器右键
////
////            return true;
////        }
////        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {// 按下遥控器上键
////
////            return true;
////        }
////        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {// 按下遥控器下键
////
////            return true;
////        }
////        if (keyCode == KeyEvent.KEYCODE_BACK) {// 按下遥控器返回键
////
////            return true;
////        }
////        if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {// 按下遥控器OK键
////
////            return true;
////        }
//        //监听键盘以及二维码输入
//        return true;//截获事件
//    }
//    private void setPayType() {
//        if (isTablet) {
//            rg.setVisibility(View.VISIBLE);
//            rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//                @Override
//                public void onCheckedChanged(RadioGroup group, int checkedId) {
//                    switch (checkedId) {
//                        case R.id.main_cash:
//                            payType = 0;
//                            break;
//                        case R.id.main_wechat:
//                            payType = 1;
//                            break;
//                        case R.id.main_alipay:
//                            payType = 2;
//                            break;
//                        case R.id.main_card:
//                            payType = 3;
//                            break;
//                    }
//                }
//            });
//        } else {
//            sp.setVisibility(View.VISIBLE);
//            List<String> list = new ArrayList<String>();
//            list.add("现金");
//            list.add("微信");
//            list.add("支付宝");
//            list.add("刷卡");
//            ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            sp.setAdapter(adapter);
//            sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    payType = position;
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> parent) {
//
//                }
//            });
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();
//        setNavTip();
        if (App.app.refresh) {
            App.app.refresh = false;
            setSearchBar(this, true);
        }
    }

//    private void setNavTip() {
//        try {
////            SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
////            SimpleDateFormat ymdhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////            String ms = ymd.format(new Date());
////            if (mHourOfDay < 10) {
////                ms += " 0" + mHourOfDay;
////            } else {
////                ms += " " + mHourOfDay;
////            }
////            if (mMinute < 10) {
////                ms += ":0" + mMinute;
////            } else {
////                ms += ":" + mMinute;
////            }
////            ms += ":00";
////            Date ed = ymdhms.parse(ms);// 今天结账时间
////            Date sd = new Date(ed.getTime() - 24 * 60 * 60 * 1000);// 昨天结账时间
////            List<SellRecord> tDatas = getDaoSession().getSellRecordDao().queryBuilder().where(SellRecordDao.Properties.SellDate.ge(sd), SellRecordDao.Properties.SellDate.le(ed)).orderDesc(SellRecordDao.Properties.SellDate).list();
//
//            SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");
//            List<SellRecord> tDatas = getDaoSession().getSellRecordDao().queryBuilder().where(SellRecordDao.Properties.SellDate.ge(ymd.parse(ymd.format(new Date())))).orderDesc(SellRecordDao.Properties.SellDate).list();
//            int total = 0;
//            for (SellRecord mData : tDatas) {
//                total += mData.getPrice() * mData.getNumber();
//            }
//            Log.d(TAG, "setNavTip total=" + total);
//            nav_total.setText("今日销售额：" + app.showPrice(total));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//        List<StoreList> wDatas = getDaoSession().getStoreListDao().loadAll();
//        int warn = 0;
//        for (StoreList mData : wDatas) {
//            try {
//                if (mData.getNumber() < mData.getWarnNumber()) {
//                    warn++;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        Log.d(TAG, "setNavTip warn=" + warn);
//        if (warn == 0) {
//            nav_warn.setVisibility(View.GONE);
//        } else {
//            nav_warn.setVisibility(View.VISIBLE);
//            nav_warn.setText(warn + " 种需要进货");
//        }
//
//        nav_title.setText(getIpAddress() + "\n" + getBtAddressByReflection());
//    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.main_btn:
                // 结算
                sendOrder();
                break;
        }
    }

    private void sendOrder() {
        Log.d(TAG, "sendOrder size=" + uRecords.size());
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
//        if (isTablet) {
//            rg.check(R.id.main_cash);
//        } else {
//            sp.setSelection(0);
//        }
        tv_total.setText("合计：0.0");
        Snackbar.make(mRecyclerView, "结算成功", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
//        setNavTip();
        if (cb.isChecked()) {
            PrintUtil.printStick(mGpService, sRecords);
            cb.setChecked(false);
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
//            et_search.setText("");
//            mRecyclerView.requestFocus();
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
        Logger.d("ipAddress=" + ipAddress);
        Snackbar.make(mRecyclerView, getIpAddress(), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
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
//        } else if (id == R.id.nav_enterList) {// 进货记录
//            startActivity(new Intent(this, EnterRecordActivity.class));
//        } else if (id == R.id.nav_statistics) {// 销售记录
//            startActivity(new Intent(this, StatisticsActivity.class));
//        } else if (id == R.id.nav_destory) {// 报损处理(自用、损坏或过期、退还商家)
//            startActivity(new Intent(this, DestoryActivity.class));
        } else if (id == R.id.nav_printer) {// 连接打印机
            if (mGpService == null) {
                Toast.makeText(this, "Print Service is not start, please check it", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, PrinterConnectDialog.class);
                boolean[] state = getConnectState();
                intent.putExtra("connect.status", state);
                this.startActivity(intent);
            }
        } else if (id == R.id.nav_help) {// 使用帮助
            startActivity(new Intent(this, WebActivity.class));
        } else if (id == R.id.nav_about) {// 关于
            startActivity(new Intent(this, AboutActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private GpService mGpService = null;
    private PrinterServiceConnection conn = null;

    private int mPrinterIndex = 0;
    private static final int MAIN_QUERY_PRINTER_STATUS = 0xfe;
    private static final int REQUEST_PRINT_LABEL = 0xfd;
    private static final int REQUEST_PRINT_RECEIPT = 0xfc;

    class PrinterServiceConnection implements ServiceConnection {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("ServiceConnection", "onServiceDisconnected() called");
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
            e.printStackTrace();
        }
    }

    private void connection() {
        conn = new PrinterServiceConnection();
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

    //    private ApiController apiController;
    public static String result = "";

//    private BluetoothChatService bluetoothChatService;
//    private Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case BluetoothChatService.BLUE_TOOTH_READ: {
//                    bluetoothChatService.sendData(apiController.getBluetoothResult(result).getBytes());
//                }
//                break;
//            }
//        }
//    };
//
//    /**
//     * 获取蓝牙地址
//     *
//     * @return
//     */
//    public String getBtAddressByReflection() {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        Field field = null;
//        try {
//            field = BluetoothAdapter.class.getDeclaredField("mService");
//            field.setAccessible(true);
//            Object bluetoothManagerService = field.get(bluetoothAdapter);
//            if (bluetoothManagerService == null) {
//                return "";
//            }
//            Method method = bluetoothManagerService.getClass().getMethod("getAddress");
//            if (method != null) {
//                Object obj = method.invoke(bluetoothManagerService);
//                if (obj != null) {
//                    return obj.toString();
//                }
//            }
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

    private ServerManager mServerManager;
    private String ipAddress;

    public String getIpAddress() {
//        if (TextUtils.isEmpty(ipAddress) || "0.0.0.0".equals(ipAddress)) {
        try {
            ipAddress = NetUtils.getLocalIPAddress().getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        }
        if (TextUtils.isEmpty(ipAddress)) {
            ipAddress = "";
        }
        Logger.d("ipAddress=" + ipAddress);
        return ipAddress;
    }

    /**
     * Start notify.
     */
    public void onServerStart(String ip) {
        ipAddress = ip;
        Snackbar.make(mRecyclerView, ip, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    /**
     * Error notify.
     */
    public void onServerError(String message) {
    }

    /**
     * Stop notify.
     */
    public void onServerStop() {
    }
}
