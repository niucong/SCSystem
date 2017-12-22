package com.niucong.scsystem.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by think on 2016/10/31.
 */

public class FileUtil {

    private String SDCardPath = Environment.getExternalStorageDirectory() + File.separator;
    private SimpleDateFormat ymdhms = new SimpleDateFormat("yyyyMMddHHmm");

    /**
     * 导出数据
     */
    public boolean copyDBToSDcrad(Context c) {
        String DATABASE_NAME = "shunchang";
        String oldPath = c.getDatabasePath(DATABASE_NAME).getPath();
        String newPath = SDCardPath + DATABASE_NAME;
        Log.i("mainactivity", "copyDBToSDcrad oldPath=" + oldPath);
        Log.i("mainactivity", "copyDBToSDcrad newPath=" + newPath);
        File proFile = new File(newPath);
        if (proFile.exists()) {
            File myFile = new File(newPath + ymdhms.format(new Date(proFile.lastModified())));
            proFile.renameTo(myFile);
        }
        return copyFile(oldPath, newPath + ymdhms.format(new Date()));
    }

    /**
     * 导入数据
     */
    public boolean copySDcradToDB(Context c) {
        String DATABASE_NAME = "shunchang";
        String oldPath = "file:///android_asset/" + DATABASE_NAME;// SDCardPath
        String newPath = c.getDatabasePath(DATABASE_NAME).getPath();
        Log.i("mainactivity", "copySDcradToDB oldPath=" + oldPath);
        Log.i("mainactivity", "copySDcradToDB newPath=" + newPath);
        return copyAssetsFile(c, DATABASE_NAME, newPath);
//        copyFile(oldPath, newPath);
//        File proFile = new File(oldPath);
//        File myFile = new File(oldPath + ymdhms.format(new Date()));
//        return proFile.renameTo(myFile);
    }

    /**
     *  * 复制单个文件
     *  *
     *  * @param oldPath
     *  *            String 原文件路径
     *  * @param newPath
     *  *            String 复制后路径
     *  * @return boolean
     *  
     */
    public static boolean copyAssetsFile(Context c, String oldFileName, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File newfile = new File(newPath);
            if (!newfile.exists()) {
                newfile.createNewFile();
            }
            InputStream inStream = c.getAssets().open(oldFileName);
            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread; // 字节数 文件大小
                fs.write(buffer, 0, byteread);
            }
            inStream.close();
            Log.i("mainactivity", "复制文件成功");
            return true;
        } catch (Exception e) {
            Log.i("mainactivity", "复制文件操作出错");
            e.printStackTrace();
            return false;
        }
    }

    /**
     *  * 复制单个文件
     *  *
     *  * @param oldPath
     *  *            String 原文件路径
     *  * @param newPath
     *  *            String 复制后路径
     *  * @return boolean
     *  
     */
    public static boolean copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            File newfile = new File(newPath);
            if (!newfile.exists()) {
                newfile.createNewFile();
            }
            if (oldfile.exists()) { // 文件存在时
                InputStream inStream = new FileInputStream(oldPath); // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; // 字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                Log.i("mainactivity", "复制文件成功");
                return true;
            } else {
                Log.i("mainactivity", "文件不存在");
            }
            return false;
        } catch (Exception e) {
            Log.i("mainactivity", "复制文件操作出错");
            e.printStackTrace();
            return false;
        }
    }
}
