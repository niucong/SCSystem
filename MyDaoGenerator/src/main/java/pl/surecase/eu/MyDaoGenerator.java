package pl.surecase.eu;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class MyDaoGenerator {

    public static void main(String args[]) throws Exception {
        Schema schema = new Schema(1, "com.niucong.scsystem.dao");
        schema.setDefaultJavaPackageDao("com.niucong.scsystem.dao");

        Entity drugInfo = schema.addEntity("DrugInfo");
        drugInfo.setTableName("DrugInfo");
        drugInfo.addLongProperty("BarCode").primaryKey().notNull();
        drugInfo.addStringProperty("Name").notNull();
        drugInfo.addStringProperty("Factory");
        drugInfo.addStringProperty("NamePY").notNull();
        drugInfo.addStringProperty("NamePYF").notNull();
        drugInfo.addLongProperty("UpdateTime").notNull();

        Entity storeList = schema.addEntity("StoreList");
        storeList.setTableName("StoreList");
        storeList.addLongProperty("BarCode").primaryKey().notNull();
        storeList.addIntProperty("Number");
        storeList.addIntProperty("Price");
        storeList.addIntProperty("WarnNumber");
        storeList.addLongProperty("UpdateTime").notNull();

        Entity enterRecord = schema.addEntity("EnterRecord");
        enterRecord.setTableName("EnterRecord");
        enterRecord.addIdProperty().autoincrement();
        enterRecord.addLongProperty("BarCode").notNull();
        enterRecord.addIntProperty("Number");
        enterRecord.addIntProperty("Price");
        enterRecord.addDateProperty("EnterDate").notNull();

        Entity sellRecord = schema.addEntity("SellRecord");
        sellRecord.setTableName("SellRecord");
        sellRecord.addIdProperty().autoincrement();
        sellRecord.addLongProperty("BarCode").notNull();
        sellRecord.addIntProperty("Number");
        sellRecord.addIntProperty("Price");
        sellRecord.addIntProperty("PayType");
        sellRecord.addDateProperty("SellDate").notNull();

        new DaoGenerator().generateAll(schema, args[0]);
    }
}
