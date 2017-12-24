package com.niucong.scsystem.util;

import android.os.RemoteException;
import android.util.Base64;
import android.widget.Toast;

import com.gprinter.aidl.GpService;
import com.gprinter.command.EscCommand;
import com.gprinter.command.GpCom;
import com.gprinter.io.utils.GpUtils;
import com.niucong.scsystem.app.App;
import com.niucong.scsystem.dao.DBUtil;
import com.niucong.scsystem.dao.DrugInfo;
import com.niucong.scsystem.dao.SellRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * Created by think on 2017/3/2.
 */

public class PrintUtil {

    /**
     * 打印小票
     *
     * @param mGpService
     * @param sRecords
     */
    public static void printStick(GpService mGpService, List<SellRecord> sRecords) {
        EscCommand esc = new EscCommand();
        esc.addPrintAndFeedLines((byte) 3);
        // 设置打印居中
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);

        // 设置为倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.ON, EscCommand.ENABLE.ON, EscCommand.ENABLE.OFF);
        // 打印文字
        esc.addText("顺昌诊所\n");
        esc.addPrintAndLineFeed();

        // 取消倍高倍宽
        esc.addSelectPrintModes(EscCommand.FONT.FONTA, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF, EscCommand.ENABLE.OFF);
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);// 设置打印左对齐

        int size = sRecords.size();
        int allPrice = 0;
        Date d = null;
        int payType = 0;
        for (int i = 0; i < size; i++) {
            SellRecord sr = sRecords.get(i);
            d = sr.getSellDate();
            payType = sr.getPayType();
            allPrice += sr.getPrice() * sr.getNumber();
            DrugInfo di = DBUtil.getDaoSession().getDrugInfoDao().load(sr.getBarCode());
            esc.addText((i + 1) + "\t" + di.getName() + "\n");
            esc.addText(di.getBarCode() + "\n" + sr.getNumber() + "*" + App.app.showPrice(sr.getPrice()) + "\t\t" + App.app.showPrice(sr.getPrice() * sr.getNumber()) + "\n");
        }
        esc.addPrintAndLineFeed();

        sRecords.get(0).getId();
        esc.addText("订单号：" + d.getTime() + "\n");
        esc.addText("销售时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(d) + "\n");
        esc.addText("销售种类：" + size + "种\n");
        String strType = "现金";
        if (payType == 0) {
            strType = "现金";
        } else if (payType == 1) {
            strType = "微信";
        } else if (payType == 2) {
            strType = "支付宝";
        } else if (payType == 3) {
            strType = "刷卡";
        }
        esc.addText("销售金额：" + App.app.showPrice(allPrice) + "(" + strType + ")\n");

        esc.addPrintAndLineFeed();
        // 设置条码可识别字符位置在条码下方
        esc.addSelectPrintingPositionForHRICharacters(EscCommand.HRI_POSITION.BELOW);
        // 设置条码高度为60点
        esc.addSetBarcodeHeight((byte) 60);
        // 设置条码单元宽度为1点
        esc.addSetBarcodeWidth((byte) 2);
        // 打印Code128码
        esc.addCODE128("" + d.getTime());
        esc.addCODE128(esc.genCodeB("" + d.getTime()));
        esc.addPrintAndLineFeed();
        esc.addText("\n\n");

        Vector<Byte> datas = esc.getCommand();
        // 发送数据
        Byte[] Bytes = datas.toArray(new Byte[datas.size()]);
        byte[] bytes = GpUtils.ByteTo_byte(Bytes);
        String str = Base64.encodeToString(bytes, Base64.DEFAULT);
        int rel;
        try {
            rel = mGpService.sendEscCommand(0, str);
            GpCom.ERROR_CODE r = GpCom.ERROR_CODE.values()[rel];
            if (r != GpCom.ERROR_CODE.SUCCESS) {
                Toast.makeText(App.app, GpCom.getErrorText(r), Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
