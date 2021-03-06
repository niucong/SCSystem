package com.niucong.scsystem.view.wheel;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.niucong.scsystem.R;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by think on 2016/9/19.
 */
public class DateSelectView extends LinearLayout {
    private static final String TAG = "DateSelectView";

    private WheelView wvYear, wvMonth, wvDay;
    private ArrayList<String> arry_years = new ArrayList<String>();
    private ArrayList<String> arry_months = new ArrayList<String>();
    private ArrayList<String> arry_days = new ArrayList<String>();
    private CalendarTextAdapter mYearAdapter;
    private CalendarTextAdapter mMonthAdapter;
    private CalendarTextAdapter mDaydapter;

    private int month;
    private int day;

    private int currentYear = getYear();
    private int currentMonth = getMonth();
    private int currentDay = getDay();

    private int maxTextSize = 20;
    private int minTextSize = 14;

    private boolean issetdata = false;

    /**
     * Constructor
     */
    public DateSelectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initData(context);
    }

    /**
     * Constructor
     */
    public DateSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
    }

    /**
     * Constructor
     */
    public DateSelectView(Context context) {
        super(context);
        initData(context);
    }

    /**
     * Initializes class data
     *
     * @param context the context
     */
    private void initData(final Context context) {
        wvYear = new WheelView(context);
        wvMonth = new WheelView(context);
        wvDay = new WheelView(context);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1);
        addView(wvYear, layoutParams);
        addView(wvMonth, layoutParams);
        addView(wvDay, layoutParams);

        initYears();
        mYearAdapter = new CalendarTextAdapter(context, arry_years, setYear(currentYear), maxTextSize, minTextSize);
        wvYear.setVisibleItems(3);
        wvYear.setViewAdapter(mYearAdapter);
        Log.d(TAG, "currentYear=" + currentYear + ",setYear=" + setYear(currentYear));
        wvYear.setCurrentItem(setYear(currentYear));

        initMonths(month);
        mMonthAdapter = new CalendarTextAdapter(context, arry_months, setMonth(currentMonth), maxTextSize, minTextSize);
        wvMonth.setVisibleItems(3);
        wvMonth.setViewAdapter(mMonthAdapter);
        wvMonth.setCurrentItem(setMonth(currentMonth));

        initDays(day);
        mDaydapter = new CalendarTextAdapter(context, arry_days, currentDay - 1, maxTextSize, minTextSize);
        wvDay.setVisibleItems(3);
        wvDay.setViewAdapter(mDaydapter);
        wvDay.setCurrentItem(currentDay - 1);

        wvYear.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mYearAdapter.getItemText(wheel.getCurrentItem());
                Log.d(TAG, "wvYear.addChangingListener currentText=" + currentText);
                setTextviewSize(currentText, mYearAdapter);
                currentYear = Integer.parseInt(currentText);
                setYear(currentYear);
                initMonths(month);
                mMonthAdapter = new CalendarTextAdapter(context, arry_months, 0, maxTextSize, minTextSize);
                wvMonth.setVisibleItems(3);
                wvMonth.setViewAdapter(mMonthAdapter);
                wvMonth.setCurrentItem(0);
            }
        });

        wvYear.addScrollingListener(new OnWheelScrollListener() {

            @Override
            public void onScrollingStarted(WheelView wheel) {

            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mYearAdapter.getItemText(wheel.getCurrentItem());
                Log.d(TAG, "wvYear.addScrollingListener currentText=" + currentText);
                setTextviewSize(currentText, mYearAdapter);
            }
        });

        wvMonth.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mMonthAdapter.getItemText(wheel.getCurrentItem());
                Log.d(TAG, "wvMonth.addChangingListener currentText=" + currentText);
                setTextviewSize(currentText, mMonthAdapter);
                currentMonth = Integer.parseInt(currentText);
                setMonth(Integer.parseInt(currentText));
                initDays(day);
                mDaydapter = new CalendarTextAdapter(context, arry_days, 0, maxTextSize, minTextSize);
                wvDay.setVisibleItems(3);
                wvDay.setViewAdapter(mDaydapter);
                wvDay.setCurrentItem(0);
            }
        });

        wvMonth.addScrollingListener(new OnWheelScrollListener() {

            @Override
            public void onScrollingStarted(WheelView wheel) {

            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mMonthAdapter.getItemText(wheel.getCurrentItem());
                Log.d(TAG, "wvMonth.addScrollingListener currentText=" + currentText);
                setTextviewSize(currentText, mMonthAdapter);
            }
        });

        wvDay.addChangingListener(new OnWheelChangedListener() {

            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                String currentText = (String) mDaydapter.getItemText(wheel.getCurrentItem());
                Log.d(TAG, "wvDay.addChangingListener currentText=" + currentText);
                setTextviewSize(currentText, mDaydapter);
                currentDay = Integer.parseInt(currentText);
            }
        });

        wvDay.addScrollingListener(new OnWheelScrollListener() {

            @Override
            public void onScrollingStarted(WheelView wheel) {

            }

            @Override
            public void onScrollingFinished(WheelView wheel) {
                String currentText = (String) mDaydapter.getItemText(wheel.getCurrentItem());
                Log.d(TAG, "wvDay.addScrollingListener currentText=" + currentText);
                setTextviewSize(currentText, mDaydapter);
            }
        });
    }

    public int getYear() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR);
    }

    public int getMonth() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.MONTH) + 1;
    }

    public int getDay() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.DATE);
    }

    public void initYears() {
        for (int i = 2016; i < getYear() + 1; i++) {
            arry_years.add(i + "");
        }
    }

    public void initMonths(int months) {
        arry_months.clear();
        for (int i = 1; i <= months; i++) {
            if (i < 10) {
                arry_months.add("0" + i);
            } else {
                arry_months.add("" + i);
            }
        }
    }

    public void initDays(int days) {
        arry_days.clear();
        for (int i = 1; i <= days; i++) {
            if (i < 10) {
                arry_days.add("0" + i);
            } else {
                arry_days.add("" + i);
            }
        }
    }

//    /**
//     * 设置年月日
//     *
//     * @param year
//     * @param month
//     * @param day
//     */
//    public void setDate(int year, int month, int day) {
//        selectYear = year + "";
//        selectMonth = month + "";
//        selectDay = day + "";
//        issetdata = true;
//        this.currentYear = year;
//        this.currentMonth = month;
//        this.currentDay = day;
//        if (year == getYear()) {
//            this.month = getMonth();
//        } else {
//            this.month = 12;
//        }
//        calDays(year, month);
//    }

    /**
     * 获取年月日
     *
     * @return
     */
    public String getDate() {
        String selectDate = "" + currentYear;
        if (Integer.valueOf(currentMonth) < 10) {
            selectDate += "-0" + currentMonth;
        } else {
            selectDate += "-" + currentMonth;
        }
        if (Integer.valueOf(currentDay) < 10) {
            selectDate += "-0" + currentDay;
        } else {
            selectDate += "-" + currentDay;
        }
        return selectDate;
    }

    /**
     * 设置年份
     *
     * @param year
     */
    public int setYear(int year) {
        int yearIndex = 0;
        if (year != getYear()) {
            this.month = 12;
        } else {
            this.month = getMonth();
        }
        for (int i = 2016; i < getYear() + 1; i++) {
            if (i == year) {
                return yearIndex;
            }
            yearIndex++;
        }
        return yearIndex;
    }

    /**
     * 设置月份
     *
     * @param month
     * @return
     */
    public int setMonth(int month) {
        int monthIndex = 0;
        calDays(currentYear, month);
        for (int i = 1; i < this.month; i++) {
            if (month == i) {
                return monthIndex;
            } else {
                monthIndex++;
            }
        }
        return monthIndex;
    }

    /**
     * 计算每月多少天
     *
     * @param year
     * @param month
     */
    public void calDays(int year, int month) {
        boolean leayyear = false;
        leayyear = year % 4 == 0 && year % 100 != 0;
        for (int i = 1; i <= 12; i++) {
            switch (month) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    this.day = 31;
                    break;
                case 2:
                    if (leayyear) {
                        this.day = 29;
                    } else {
                        this.day = 28;
                    }
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    this.day = 30;
                    break;
            }
        }
        if (year == getYear() && month == getMonth()) {
            this.day = getDay();
        }
    }

    /**
     * 设置字体大小
     *
     * @param curriteItemText
     * @param adapter
     */
    public void setTextviewSize(String curriteItemText, CalendarTextAdapter adapter) {
        Log.d(TAG, "setTextviewSize=" + curriteItemText);
        ArrayList<View> arrayList = adapter.getTestViews();
        int size = arrayList.size();
        String currentText;
        for (int i = 0; i < size; i++) {
            TextView textvew = (TextView) arrayList.get(i);
            currentText = textvew.getText().toString();
            if (curriteItemText.equals(currentText)) {
                textvew.setTextSize(maxTextSize);
            } else {
                textvew.setTextSize(minTextSize);
            }
        }
    }

    private class CalendarTextAdapter extends AbstractWheelTextAdapter {
        ArrayList<String> list;

        protected CalendarTextAdapter(Context context, ArrayList<String> list, int currentItem, int maxsize, int minsize) {
            super(context, R.layout.item_date, NO_RESOURCE, currentItem, maxsize, minsize);
            this.list = list;
            setItemTextResource(R.id.tempValue);
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            View view = super.getItem(index, cachedView, parent);
            return view;
        }

        @Override
        public int getItemsCount() {
            return list.size();
        }

        @Override
        protected CharSequence getItemText(int index) {
            return list.get(index) + "";
        }
    }

}
