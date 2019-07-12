/*
 * Copyright 2018 Yan Zhenjie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.niucong.scsystem.andserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.niucong.scsystem.andserver.model.DrugInfoData;
import com.niucong.scsystem.andserver.model.SynData;
import com.niucong.scsystem.andserver.util.Logger;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.DrugInfoDao;
import com.niucong.scsystem.dao.StoreList;
import com.yanzhenjie.andserver.annotation.PostMapping;
import com.yanzhenjie.andserver.annotation.RequestBody;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.annotation.RestController;
import com.yanzhenjie.andserver.util.MediaType;

import java.util.List;

/**
 * Created by YanZhenjie on 2018/6/9.
 */
@RestController
@RequestMapping(path = "/sc")
public class ApiController {

    /**
     * 处理蓝牙请求
     *
     * @param readMessage
     * @return
     */
    public String getBluetoothResult(String readMessage) {
        JSONObject json = null;
        try {
            json = JSON.parseObject(readMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject object = new JSONObject();
        if (json == null) {
            object.put("code", 400);
            object.put("msg", "参数格式错误");
        } else if (!json.containsKey("method")) {
            object.put("code", 400);
            object.put("msg", "缺少参数method");
        } else {
            String method = json.getString("method");
            object.put("method", method);
            object.put("code", 200);
            object.put("msg", "请求成功");
            if ("synData".equals(method)) {
                if (json.containsKey("updateTime") && json.containsKey("data")) {
                    long updateTime = 0;
                    try {
                        updateTime = json.getLongValue("updateTime");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    SynData synData = new SynData();
                    synData.setUpdateTime(updateTime);
                    synData.setData(JSON.parseArray(json.getString("data"), DrugInfoData.class));
                    object.put("data", synData(synData));
                } else {
                    object.put("code", 400);
                    object.put("msg", "缺少参数");
                }
            } else {
                object.put("code", 404);
                object.put("msg", "请求方法不存在");
            }
        }
        Logger.d("getBluetoothResult object=" + object.toString());
        return object.toString();
    }

    // 1、synData 同步数据
    @PostMapping(path = {"/synData"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
//    String synData(@RequestParam(name = "updateTime") long updateTime, @RequestParam(name = "data") String data) {
    String synData(@RequestBody SynData synData) {

        List<DrugInfoData> drugInfoList = synData.getData();
        if (drugInfoList != null) {
            for (DrugInfoData infoData : drugInfoList) {
                DrugInfo drugInfo = DBUtil.getDaoSession().getDrugInfoDao().load(infoData.getBarCode());
                if (drugInfo == null || infoData.getUpdateTime() > drugInfo.getUpdateTime()) {
                    drugInfo = new DrugInfo();
                    drugInfo.setBarCode(infoData.getBarCode());
                    drugInfo.setName(infoData.getName());
                    drugInfo.setFactory(infoData.getFactory());
                    drugInfo.setNamePY(infoData.getNamePY());
                    drugInfo.setNamePYF(infoData.getNamePYF());
                    drugInfo.setUpdateTime(infoData.getUpdateTime());

                    StoreList store = new StoreList();
                    store.setBarCode(infoData.getBarCode());
                    store.setPrice((int) infoData.getPrice());
                    store.setNumber(0);
                    store.setWarnNumber(0);
                    store.setUpdateTime(System.currentTimeMillis());

                    DBUtil.getDaoSession().getDrugInfoDao().insertOrReplace(drugInfo);
                    DBUtil.getDaoSession().getStoreListDao().insertOrReplace(store);
                }
            }
        }

        JSONArray array = new JSONArray();
        List<DrugInfo> infoList;
        long updateTime = synData.getUpdateTime();
        if (updateTime == 0) {
            infoList = DBUtil.getDaoSession().getDrugInfoDao().loadAll();
        } else {
            infoList = DBUtil.getDaoSession().getDrugInfoDao().queryBuilder().where(DrugInfoDao.Properties.UpdateTime.ge(updateTime)).list();
        }
        Logger.d("size=" + infoList.size());
        for (DrugInfo drugInfo : infoList) {
            JSONObject json = new JSONObject();
            json.put("barCode", drugInfo.getBarCode());
            json.put("name", drugInfo.getName());
            json.put("factory", drugInfo.getFactory());
            json.put("namePY", drugInfo.getNamePY());
            json.put("namePYF", drugInfo.getNamePYF());
            json.put("updateTime", drugInfo.getUpdateTime());
            StoreList store = DBUtil.getDaoSession().getStoreListDao().load(drugInfo.getBarCode());
            json.put("price", store.getPrice());
            array.add(json);
        }
        return array.toString();
    }

}