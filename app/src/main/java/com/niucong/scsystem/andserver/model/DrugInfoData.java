package com.niucong.scsystem.andserver.model;

public class DrugInfoData {

    private long barCode;
    private String name;
    private String factory;
    private String namePY;
    private String namePYF;
    private long updateTime;
    private long price;

    public long getBarCode() {
        return barCode;
    }

    public void setBarCode(long barCode) {
        this.barCode = barCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFactory() {
        return factory;
    }

    public void setFactory(String factory) {
        this.factory = factory;
    }

    public String getNamePY() {
        return namePY;
    }

    public void setNamePY(String namePY) {
        this.namePY = namePY;
    }

    public String getNamePYF() {
        return namePYF;
    }

    public void setNamePYF(String namePYF) {
        this.namePYF = namePYF;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
