package com.lijun.androidstudy.qrcode;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by lijun on 17-11-13.
 */

public class BluetoothReceiver extends BroadcastReceiver {

    private static final String TAG = BluetoothReceiver.class.getSimpleName();
    String pin = "1234";  //此处为你要连接的蓝牙设备的初始密钥，一般为1234或0000

    public BluetoothReceiver() {

    }

    //广播接收器，当远程蓝牙设备被发现时，回调函数onReceiver()会被执行
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction(); //得到action
        Log.e(TAG, "BluetoothDevice onReceive action = " + action);

        BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {//发现设备
            Log.e(TAG, "ACTION_FOUND" + " [" + btDevice.getName() + "]" + ":" + btDevice.getAddress());
            if (btDevice.getName().equals(QRCodeDemoActivity.btName) && btDevice.getAddress().equals(btDevice.getAddress())) {
                if (btDevice.getBondState() == BluetoothDevice.BOND_NONE) {

                    Log.e("ywq", "attemp to bond:" + "[" + btDevice.getName() + "]");
                    try {
                        //通过工具类ClsUtils,调用createBond方法
                        ClsUtils.createBond(btDevice.getClass(), btDevice);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else
                Log.e("error", "Is faild");
        } else if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
            Log.e(TAG, "PAIRING_REQUEST [" + btDevice.getName() + "]" + ":" + btDevice.getAddress());
            if (btDevice.getName().equals(QRCodeDemoActivity.btName) && btDevice.getAddress().equals(btDevice.getAddress())) {
                Log.e(TAG, "OKOKOK");
                try {
                    //1.确认配对
                    ClsUtils.setPairingConfirmation(btDevice.getClass(), btDevice, true);
                    //2.终止有序广播
                    Log.i(TAG, "isOrderedBroadcast:" + isOrderedBroadcast() + ",isInitialStickyBroadcast:" + isInitialStickyBroadcast());
                    abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
                    //3.调用setPin方法进行配对...
                    boolean ret = ClsUtils.setPin(btDevice.getClass(), btDevice, pin);

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else
                Log.e(TAG, "这个设备不是目标蓝牙设备");

        }
    }
}
