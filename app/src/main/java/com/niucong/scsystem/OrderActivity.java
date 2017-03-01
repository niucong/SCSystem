package com.niucong.scsystem;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.command.GpCom;
import com.gprinter.io.utils.GpUtils;
import com.gprinter.service.GpPrintService;
import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.SellRecord;
import com.niucong.scsystem.dao.SellRecordDao;
import com.niucong.scsystem.dao.StoreList;
import com.niucong.scsystem.view.DividerItemDecoration;
import com.niucong.scsystem.view.NiftyDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static com.niucong.scsystem.printer.ListViewAdapter.DEBUG_TAG;

public class OrderActivity extends BasicActivity {
    private static final String TAG = "OrderActivity";

    private Toolbar toolbar;
    private RecyclerView mRecyclerView;
    private TextView tv_warn;
    private Button btn_print;

    List<SellRecord> mDatas;

    private SimpleDateFormat ymdhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private int mPrinterIndex = 0;
    private GpService mGpService;
    private PrinterServiceConnection conn = null;

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

    private void connection() {
        conn = new PrinterServiceConnection();
        Log.i(DEBUG_TAG, "connection");
        Intent intent = new Intent(this, GpPrintService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE); // bindService
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Log.d(TAG, "onCreate...");

        Date d = new Date(getIntent().getLongExtra("Date", 0));
        mDatas = DBUtil.getDaoSession().getSellRecordDao().queryBuilder().where(SellRecordDao.Properties.SellDate.eq(d)).orderDesc(SellRecordDao.Properties.SellDate).list();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("销售详情");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setSearchBar();

        tv_warn = (TextView) findViewById(R.id.store_warn);
        btn_print = (Button) findViewById(R.id.store_print);
        btn_print.setVisibility(View.VISIBLE);
        btn_print.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.store_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new StoreAdapter(mDatas));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        String warm = "订单号：" + d.getTime();
        warm += "\n销售时间：" + ymdhms.format(d);
        warm += "\n销售种类：" + mDatas.size() + "种";
        int allPrice = 0;
        for (SellRecord record : mDatas) {
            allPrice += record.getPrice() * record.getNumber();
        }
        warm += "\n销售金额：" + App.app.showPrice(allPrice);
        tv_warn.setText(warm);
        mRecyclerView.requestFocus();

        connection();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.store_print:
                //打印小票  
                printStick();
                break;
        }
    }

    private void printStick() {
        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 3);
        // 设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);

        // 设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        // 打印文字
        esc.addText("Sample\n");
        esc.addPrintAndLineFeed();

        Vector<Byte> datas = esc.getCommand();
        // 发送数据
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = GpUtils.ByteTo_byte(Bytes);
        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        int rel;
        try {
            rel = mGpService.sendEscCommand(mPrinterIndex, str);
            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
            if (r != GpCom.ERROR_CODE.SUCCESS) {
                Toast.makeText(getApplicationContext(), GpCom.getErrorText(r), Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean searchDrug(String result) {
        return false;
    }

    class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.MyViewHolder> {

        List<SellRecord> dis;

        public StoreAdapter(List<SellRecord> dis) {
            this.dis = dis;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyViewHolder holder = new MyViewHolder(LayoutInflater.from(OrderActivity.this).inflate(R.layout.item_statistics, parent, false));
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final SellRecord sr = dis.get(position);
            long code = sr.getBarCode();
            holder.tv_code.setText("" + code);
            holder.tv_num.setText(sr.getNumber() + "*" + App.app.showPrice(sr.getPrice()));
            holder.tv_time.setVisibility(View.GONE);
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

            holder.iv_delete.setVisibility(View.GONE);
            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final NiftyDialogBuilder delDia = NiftyDialogBuilder.getInstance(OrderActivity.this);
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
                                mDatas.remove(position);
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
