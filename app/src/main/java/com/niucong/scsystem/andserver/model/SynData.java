package com.niucong.scsystem.andserver.model;

import java.util.List;

public class SynData {
    private long updateTime;
    private List<DrugInfoData> data;

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public List<DrugInfoData> getData() {
        return data;
    }

    public void setData(List<DrugInfoData> data) {
        this.data = data;
    }
}
