package com.niucong.scsystem;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.DrugInfoDao;
import com.niucong.scsystem.dao.EnterRecord;
import com.niucong.scsystem.dao.EnterRecordDao;
import com.niucong.scsystem.dao.StoreList;
import com.niucong.scsystem.util.CnToSpell;
//import com.umeng.analytics.MobclickAgent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EnterActivity extends BasicActivity {
    private String TAG = "EnterActivity";

    private LinearLayout ll_old;
    private TextView tv_last_date, tv_last_num, tv_last_price, tv_store_num, tv_sell_price;
    private EditText et_code, et_warn, et_num, et_price_input, et_price;
    private AutoCompleteTextView et_name, et_factory;
    private Button btn_send;

    private DrugInfo di;// 药品信息
    private StoreList sl;// 库存
    private EnterRecord er;// 进货记录

    private long barCode;

    private SimpleDateFormat ymd = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
//        MobclickAgent.onEvent(this, "1");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("添加库存");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setSearchBar(this, true);
        setView();

        btn_send.setOnClickListener(this);

        barCode = getIntent().getLongExtra("BarCode", 0);
        if (barCode > 0) {
            searchDrug(barCode + "");
        }
    }

    private void setView() {
        et_code = (EditText) findViewById(R.id.enter_code);
        et_name = (AutoCompleteTextView) findViewById(R.id.enter_name);
        et_factory = (AutoCompleteTextView) findViewById(R.id.enter_factory);

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

        SearchAdapter searchAdapter = new SearchAdapter(this, App.app.list);
        et_name.setAdapter(searchAdapter);
        et_factory.setAdapter(searchAdapter);

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
                if (isManualInput) {
                    searchType = 1;
                }
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
                if (isManualInput) {
                    searchType = 2;
                }
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
                String str_code = et_code.getText().toString();
                String str_name = et_name.getText().toString();
                String str_factory = et_factory.getText().toString();
                String str_warn = et_warn.getText().toString();
                String str_num = et_num.getText().toString();
                String str_price_input = et_price_input.getText().toString();
                String str_price = et_price.getText().toString();
                Log.d(TAG, "onClick str_code=" + str_code + ",str_name=" + str_name + ",str_factory=" + str_factory + ",str_num=" + str_num + ",str_price_input=" + str_price_input + ",str_price=" + str_price);

                if (TextUtils.isEmpty(str_name)) {
                    Snackbar.make(btn_send, "药品名称不能为空", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                } else if (TextUtils.isEmpty(str_code)) {
                    try {
                        di = DBUtil.getDaoSession().getDrugInfoDao().queryBuilder().where(DrugInfoDao.Properties.Name.eq(str_name)).list().get(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (di != null) {
                        addDrugInfo("" + di.getBarCode(), str_name, str_factory, str_warn, str_num, str_price_input, str_price);
                        return;
                    } else {
                        getMyCode(str_code, str_name, str_factory, str_warn, str_num, str_price_input, str_price);
                        return;
                    }
                }
                addDrugInfo(str_code, str_name, str_factory, str_warn, str_num, str_price_input, str_price);
                break;
        }
    }

    /**
     * 生成自定义条形码
     *
     * @param str_code
     * @param str_name
     * @param str_factory
     * @param str_warn
     * @param str_num
     * @param str_price_input
     * @param str_price
     */
    private void getMyCode(String str_code, String str_name, String str_factory, String str_warn, String str_num, String str_price_input, String str_price) {
        String pyNF = CnToSpell.getPinYinHeadChar(str_name).toLowerCase();
        if (TextUtils.isEmpty(pyNF)) {
            Snackbar.make(btn_send, "药品名称输入错误", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        String pyPF = null;
        if (!TextUtils.isEmpty(str_factory)) {
            pyPF = CnToSpell.getPinYinHeadChar(str_factory).toLowerCase();
        }
        Log.i(TAG, "getMyCode pyNF=" + pyNF + ",pyPF=" + pyPF);

        String py = "";
        int ipyny = pyNF.length();
        if (ipyny > 3) {
            py += pyNF.substring(0, 4);
        } else if (ipyny == 3) {
            py += pyNF + "0";
        } else if (ipyny == 2) {
            py += pyNF + "00";
        } else if (ipyny == 1) {
            py += pyNF + "000";
        }
        if (TextUtils.isEmpty(pyPF)) {
            py += "000";
        } else {
            int ipypy = pyPF.length();
            if (ipypy > 2) {
                py += pyPF.substring(0, 3);
            } else if (ipyny == 2) {
                py += pyPF + "0";
            } else if (ipypy == 1) {
                py += pyPF + "00";
            }
        }
        Log.i(TAG, "getMyCode py=" + py);

        String tempCode = "99";
        for (int i = 0; i < py.length(); i++) {
            char c = py.charAt(i);
            for (int j = 0; j < pys.length; j++) {
                if (pys[j].equals("" + c)) {
                    tempCode += pycodes[j];
                    break;
                }
            }
        }
        tempCode += "0";
        Log.i(TAG, "getMyCode tempCode=" + tempCode);

        addDrugInfo("" + getRepeatCode(Long.valueOf(tempCode)), str_name, str_factory, str_warn, str_num, str_price_input, str_price);
    }

    /**
     * 编码去重
     *
     * @param tempCode
     * @return
     */
    private long getRepeatCode(long tempCode) {
        Log.i(TAG, "getRepeatCode tempCode=" + tempCode);
        di = DBUtil.getDaoSession().getDrugInfoDao().load(tempCode);
        if (di != null) {
            di = null;
            return getRepeatCode(tempCode + 1);
        }
        return tempCode;
    }

    private String[] pys = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0",
            "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private String[] pycodes = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24",
            "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36"};

    /**
     * 保存药品
     *
     * @param str_code
     * @param str_name
     * @param str_factory
     * @param str_warn
     * @param str_num
     * @param str_price_input
     * @param str_price
     */
    private void addDrugInfo(String str_code, String str_name, String str_factory, String str_warn, String str_num, String str_price_input, String str_price) {
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
            di.setNamePY(CnToSpell.getPinYin(str_name));
            di.setNamePYF(CnToSpell.getPinYinHeadChar(str_name));
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

            if (sl.getPrice() <= 0) {
                DBUtil.getDaoSession().getDrugInfoDao().insertOrReplace(di);
                Snackbar.make(btn_send, "销售价格必须大于0", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }

            er.setBarCode(Long.valueOf(str_code));
            er.setNumber(num);
            er.setPrice(App.app.savePrice(str_price_input));
            er.setEnterDate(new Date());
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
        boolean flag = false;
        for (DrugInfo drugInfo : App.app.list) {
            if (drugInfo.getBarCode() == di.getBarCode()) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            App.app.list.add(di);
            setSearchBar(this, true);
            SearchAdapter searchAdapter = new SearchAdapter(this, App.app.list);
            et_name.setAdapter(searchAdapter);
        }
        clearInput();
        et_search.requestFocus();
        Snackbar.make(btn_send, "入库成功", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    protected boolean searchDrug(String result) {
        Log.d(TAG, "searchDrug code=" + result);
        if (TextUtils.isEmpty(result)) {
//            Snackbar.make(btn_send, "条形码输入错误", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
            return false;
        }
        isManualInput = false;
        if (searchType == 0) {
            clearInput();
        }
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
            Log.d(TAG, "searchDrug1 isManualInput=" + isManualInput + ",searchType=" + searchType);
            if (searchType == 0 || searchType == 2) {
                et_factory.setText(di.getFactory());
            }
            Log.d(TAG, "searchDrug2 isManualInput=" + isManualInput + ",searchType=" + searchType);
            if (searchType == 0 || searchType == 1) {
                et_name.setText(di.getName());
            }
        }
        Log.d(TAG, "searchDrug3 isManualInput=" + isManualInput + ",searchType=" + searchType);
        if (sl != null && searchType == 0) {
            et_warn.setText("" + sl.getWarnNumber());
            tv_store_num.setText("库存数量：" + sl.getNumber());
            String price = App.app.showPrice(sl.getPrice());
            tv_sell_price.setText("销售价格：" + price);
            et_price.setText(price);
        }
        Log.d(TAG, "searchDrug4 isManualInput=" + isManualInput + ",searchType=" + searchType);
        if (er != null && searchType == 0) {
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
        isManualInput = true;
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
