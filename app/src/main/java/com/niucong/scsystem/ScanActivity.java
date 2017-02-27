package com.niucong.scsystem;

import android.os.Bundle;

import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * Created by think on 2016/11/9.
 */

public class ScanActivity extends CaptureActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            //申请WRITE_EXTERNAL_STORAGE权限
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
//                    1);
//        } else {
//            initCamera();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission Granted
//                initCamera();
//            } else {
//                // Permission Denied
//                finish();
//            }
//        }
    }

    private void initCamera() {

    }
}
