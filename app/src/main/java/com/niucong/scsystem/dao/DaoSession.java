package com.niucong.scsystem.dao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.StoreList;
import com.niucong.scsystem.dao.EnterRecord;
import com.niucong.scsystem.dao.SellRecord;

import com.niucong.scsystem.dao.DrugInfoDao;
import com.niucong.scsystem.dao.StoreListDao;
import com.niucong.scsystem.dao.EnterRecordDao;
import com.niucong.scsystem.dao.SellRecordDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig drugInfoDaoConfig;
    private final DaoConfig storeListDaoConfig;
    private final DaoConfig enterRecordDaoConfig;
    private final DaoConfig sellRecordDaoConfig;

    private final DrugInfoDao drugInfoDao;
    private final StoreListDao storeListDao;
    private final EnterRecordDao enterRecordDao;
    private final SellRecordDao sellRecordDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        drugInfoDaoConfig = daoConfigMap.get(DrugInfoDao.class).clone();
        drugInfoDaoConfig.initIdentityScope(type);

        storeListDaoConfig = daoConfigMap.get(StoreListDao.class).clone();
        storeListDaoConfig.initIdentityScope(type);

        enterRecordDaoConfig = daoConfigMap.get(EnterRecordDao.class).clone();
        enterRecordDaoConfig.initIdentityScope(type);

        sellRecordDaoConfig = daoConfigMap.get(SellRecordDao.class).clone();
        sellRecordDaoConfig.initIdentityScope(type);

        drugInfoDao = new DrugInfoDao(drugInfoDaoConfig, this);
        storeListDao = new StoreListDao(storeListDaoConfig, this);
        enterRecordDao = new EnterRecordDao(enterRecordDaoConfig, this);
        sellRecordDao = new SellRecordDao(sellRecordDaoConfig, this);

        registerDao(DrugInfo.class, drugInfoDao);
        registerDao(StoreList.class, storeListDao);
        registerDao(EnterRecord.class, enterRecordDao);
        registerDao(SellRecord.class, sellRecordDao);
    }
    
    public void clear() {
        drugInfoDaoConfig.getIdentityScope().clear();
        storeListDaoConfig.getIdentityScope().clear();
        enterRecordDaoConfig.getIdentityScope().clear();
        sellRecordDaoConfig.getIdentityScope().clear();
    }

    public DrugInfoDao getDrugInfoDao() {
        return drugInfoDao;
    }

    public StoreListDao getStoreListDao() {
        return storeListDao;
    }

    public EnterRecordDao getEnterRecordDao() {
        return enterRecordDao;
    }

    public SellRecordDao getSellRecordDao() {
        return sellRecordDao;
    }

}
