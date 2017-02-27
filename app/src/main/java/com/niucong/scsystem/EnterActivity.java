package com.niucong.scsystem;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.EnterRecord;
import com.niucong.scsystem.dao.EnterRecordDao;
import com.niucong.scsystem.dao.StoreList;
import com.umeng.analytics.MobclickAgent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EnterActivity extends BasicActivity {
    private String TAG = "EnterActivity";

    private LinearLayout ll_old;
    private TextView tv_last_date, tv_last_num, tv_last_price;
    private EditText et_code, et_name, et_factory, et_warn, et_num, et_price_input, et_price;
    private TextView tv_store_num, tv_sell_price;
    private Button btn_send;

    private DrugInfo di;// 药品信息
    private StoreList sl;// 库存
    private EnterRecord er;// 进货记录

    private SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        MobclickAgent.onEvent(this, "1");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("添加库存");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setSearchBar();
        setView();

        btn_send.setOnClickListener(this);
    }

    private void setView() {
        et_code = (EditText) findViewById(R.id.enter_code);
        et_name = (EditText) findViewById(R.id.enter_name);
        et_factory = (EditText) findViewById(R.id.enter_factory);

        ll_old = (LinearLayout) findViewById(R.id.enter_last_info);
        tv_last_date = (TextView) findViewById(R.id.enter_last_date);
        tv_last_num = (TextView) findViewById(R.id.enter_last_num);
        tv_last_price = (TextView) findViewById(R.id.enter_last_price);

        et_warn = (EditText) findViewById(R.id.enter_warn);
        tv_store_num = (TextView) findViewById(R.id.enter_store_num);
        et_num = (EditText) findViewById(R.id.enter_num);

        et_price_input = (EditText) findViewById(R.id.enter_price_input);
        tv_sell_price = (TextView) findViewById(R.id.enter_sell_price);
        et_price = (EditText) findViewById(R.id.enter_price);

        btn_send = (Button) findViewById(R.id.enter_btn);

        et_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (str.contains("\n")) {
                    str = str.replace("\n", "");
                    et_name.setText(str);
                    et_name.setSelection(str.length());
                }
            }
        });

        et_factory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (str.contains("\n")) {
                    str = str.replace("\n", "");
                    et_factory.setText(str);
                    et_factory.setSelection(str.length());
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.enter_btn:
                addDrugInfo();
                break;

        }
    }

    private void addDrugInfo() {
        String str_code = et_code.getText().toString();
        String str_name = et_name.getText().toString();
        String str_factory = et_factory.getText().toString();
        String str_warn = et_warn.getText().toString();
        String str_num = et_num.getText().toString();
        String str_price_input = et_price_input.getText().toString();
        String str_price = et_price.getText().toString();
        Log.d(TAG, "addDrugInfo str_code=" + str_code + ",str_name=" + str_name + ",str_factory=" + str_factory + ",str_num=" + str_num + ",str_price_input=" + str_price_input + ",str_price=" + str_price);

        if (di == null) {
            di = new DrugInfo();
        }
        if (sl == null) {
            sl = new StoreList();
            sl.setNumber(0);
        }
        if (er == null) {
            er = new EnterRecord();
        }

        try {
            di.setBarCode(Long.valueOf(str_code));
            di.setName(str_name);
            di.setFactory(str_factory);

            sl.setBarCode(Long.valueOf(str_code));
            if (TextUtils.isEmpty(str_num)) {
                str_num = "0";
            }
            int num = Integer.valueOf(str_num);
            sl.setNumber(num + sl.getNumber());
            if (TextUtils.isEmpty(str_warn)) {
                str_warn = "0";
            }
            sl.setWarnNumber(Integer.valueOf(str_warn));
            sl.setPrice(App.app.savePrice(str_price));

            er.setBarCode(Long.valueOf(str_code));
            er.setNumber(num);
            er.setPrice(App.app.savePrice(str_price_input));
            er.setEnterDate(new Date());

            if (TextUtils.isEmpty(str_name)) {
                Snackbar.make(btn_send, "药品名称不能为空", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            } else if (di.getBarCode() < 1) {
                Snackbar.make(btn_send, "条形码输入错误", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            } else if (er.getPrice() <= 0) {
//                DBUtil.getDaoSession().getDrugInfoDao().insertOrReplace(di);
//                Snackbar.make(btn_send, "进货价格必须大于0", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                return;
            } else if (sl.getPrice() <= 0) {
                DBUtil.getDaoSession().getDrugInfoDao().insertOrReplace(di);
                Snackbar.make(btn_send, "销售价格必须大于0", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }
        } catch (Exception e) {
            Snackbar.make(btn_send, "药品信息输入错误", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }


        Log.d(TAG, "addDrugInfo 开始入库");
        DBUtil.getDaoSession().getDrugInfoDao().insertOrReplace(di);
        DBUtil.getDaoSession().getStoreListDao().insertOrReplace(sl);
        if (er.getNumber() > 0) {
            DBUtil.getDaoSession().getEnterRecordDao().insertOrReplace(er);
        }
        clearInput();
        et_search.requestFocus();
        Snackbar.make(btn_send, "入库成功", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();

//        String[] arr = App.app.share.getBarCodes();
//        boolean flag = false;
//        for (String s : arr) {
//            if (str_code.equals(s)) {
//                flag = true;
//                break;
//            }
//        }
//        if (!flag) {
//            App.app.share.saveBarCode(str_code);
//        }
    }

    @Override
    protected boolean searchDrug(String result) {
        Log.d(TAG, "searchDrug code=" + result);
        if (TextUtils.isEmpty(result)) {
//            Snackbar.make(btn_send, "条形码输入错误", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
            return false;
        }
        clearInput();
        di = DBUtil.getDaoSession().getDrugInfoDao().load(Long.valueOf(result));
        sl = DBUtil.getDaoSession().getStoreListDao().load(Long.valueOf(result));
        try {
            er = DBUtil.getDaoSession().getEnterRecordDao().queryBuilder().where(EnterRecordDao.Properties.BarCode.eq(result)).orderDesc(EnterRecordDao.Properties.EnterDate).list().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        et_code.setText(result);
        et_search.setText("");
        if (di != null) {
            et_factory.setText(di.getFactory());
            et_name.setText(di.getName());
        }
        if (sl != null) {
            et_warn.setText("" + sl.getWarnNumber());
            tv_store_num.setText("库存数量：" + sl.getNumber());
            String price = App.app.showPrice(sl.getPrice());
            tv_sell_price.setText("销售价格：" + price);
            et_price.setText(price);
        }
        if (er != null) {
            ll_old.setVisibility(View.VISIBLE);
            tv_last_date.setText("上次进货时间：" + ymd.format(er.getEnterDate()));
            tv_last_num.setText("数量：" + er.getNumber());
            String price = App.app.showPrice(er.getPrice());
            tv_last_price.setText("价格：" + price);
            et_price_input.setText(price);

            et_num.requestFocus();
        } else {
            et_name.requestFocus();
        }
        return true;
    }

    private void clearInput() {
        et_code.setText("");
        et_name.setText("");
        et_factory.setText("");

        ll_old.setVisibility(View.GONE);

        et_warn.setText("");
        tv_store_num.setText("库存数量：0");
        et_num.setText("");

        et_price_input.setText("");
        tv_sell_price.setText("销售价格：0.0");
        et_price.setText("");

        di = null;
        er = null;
        sl = null;
    }
}
