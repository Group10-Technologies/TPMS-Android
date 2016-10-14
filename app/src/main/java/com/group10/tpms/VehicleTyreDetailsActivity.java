package com.group10.tpms;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by group10 on 10/8/16.
 */

public class VehicleTyreDetailsActivity extends AppCompatActivity {

    private TextView tvFrtyno, tvFlTyno, tvRLITyno, tvRLOTyno, tvRROTyno, tvRRITyno,
            tvFlPos, tvFrPos, tvRLIPos, tvRRIPos, tvRLOPos, tvRROPos,
            tvFlTemp, tvFrTemp, tvRLITemp, tvRLOTemp, tvRRITemp, tvRROTemp,
            tvFlPresure, tvFRPressure, tvRLIPresure, tvRLOPresure, tvRRIPresure, tvRROPresure;

    private String TAG = "DetailsActivity";
    private BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private BluetoothService mChatService = null;
    private String mConnectedDeviceName = null;
    int i = 0;
    CountDownTimer countDownTimer;
    Boolean allTyre = false;
    Boolean dataCollected = false;
    Boolean refreshingData = false;
    StringBuilder allTyreData = new StringBuilder();
    int allTyreDataCounter = 0;
    Logger tpmsLogger;
    Activity activity;
    int offset = 0;
    int countex = 0;
    int requestCounter = 0;
    static final int MESSAGE_SEND = 6;
    static final int MESSAGE_TOAST = 5;
    MenuItem refreshItem = null;
    Boolean handShake = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_tyre_details);
        initViews();
        activity = this;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }
        mChatService = new BluetoothService(this, mHandler);

    }

    private void initViews() {

        tvFlPos = (TextView) findViewById(R.id.tv_fl_position);
        tvFlTyno = (TextView) findViewById(R.id.tv_fl_tyreno);
        tvFlTemp = (TextView) findViewById(R.id.tv_fl_temp);
        tvFlPresure = (TextView) findViewById(R.id.tv_fl_pressure);

        tvFrtyno = (TextView) findViewById(R.id.tv_fr_tyreno);
        tvFrPos = (TextView) findViewById(R.id.tv_fr_position);
        tvFrTemp = (TextView) findViewById(R.id.tv_fr_temp);
        tvFRPressure = (TextView) findViewById(R.id.tv_fr_pressure);

        tvRLITyno = (TextView) findViewById(R.id.tv_rli_tyreno);
        tvRLIPos = (TextView) findViewById(R.id.tv_rli_position);
        tvRLITemp = (TextView) findViewById(R.id.tv_rli_temp);
        tvRLIPresure = (TextView) findViewById(R.id.tv_rli_pressure);

        tvRRITyno = (TextView) findViewById(R.id.tv_rri_tyreno);
        tvRRIPos = (TextView) findViewById(R.id.tv_rri_pos);
        tvRRITemp = (TextView) findViewById(R.id.tv_rri_temp);
        tvRRIPresure = (TextView) findViewById(R.id.tv_rri_pressure);

        tvRLOTyno = (TextView) findViewById(R.id.tv_rlo_tyreno);
        tvRLOPos = (TextView) findViewById(R.id.tv_rlo_position);
        tvRLOTemp = (TextView) findViewById(R.id.tv_rlo_temp);
        tvRLOPresure = (TextView) findViewById(R.id.tv_rlo_pressure);

        tvRROTyno = (TextView) findViewById(R.id.tv_rro_tyreno);
        tvRROPos = (TextView) findViewById(R.id.tv_rro_tyrepos);
        tvRROTemp = (TextView) findViewById(R.id.tv_rro_temp);
        tvRROPresure = (TextView) findViewById(R.id.tv_rro_tyrepressure);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    Log.d(TAG, "Data send");
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    //String writeMessage = new String(writeBuf);
                    //Log.d(TAG, "Write buf size: " + writeBuf.length);
                    /*if (writeBuf.length == 7 && writeBuf[writeBuf.length - 1] == 0xF6) {
                        allTyre = true;
                        Log.e(TAG, "All tyre: true");
                    } else {
                        allTyre = false;
                        Log.e(TAG, "All tyre: false");
                    }*/
                    StringBuilder sbb = new StringBuilder();
                    for (byte b : writeBuf) {
                        sbb.append(String.format("%02X ", b));
                    }
                    //mConversationArrayAdapter.add("Me:  " + sbb.toString());
                    //Logger.addRecordToLog("Me:  " + sbb.toString());
                    Log.e("Me", sbb.toString());
                    break;
                case Constants.MESSAGE_READ:
                    Log.d(TAG, "Data received");
                    //byte
                    byte[] readBuf = (byte[]) msg.obj;
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < msg.arg1; i++) {
                        sb.append(String.format("%02X ", readBuf[i]));
                    }
                    /*for (byte b : readBuf) {
                        sb.append(String.format("%02X ", b));
                    }*/
                    Log.e("TPMS ", msg.arg1 + "");
                    Log.e("TPMS ", (sb.toString()));

                    processTyreData(readBuf, msg.arg1);

                    /*if (allTyre) {
                        allTyreDataCounter += msg.arg1;
                        //Log.d(TAG, "Counter size: " + allTyreDataCounter);
                    }
                    //Log.d(TAG, "Data size: " + readBuf.length);
                    StringBuilder sb = new StringBuilder();
                    int j = 0;
                    for (byte b : readBuf) {
                        if (j < msg.arg1) {
                            sb.append(String.format("%02X ", b));
                        } else {
                            break;
                        }
                        j++;
                    }
                    Log.e("TPMS ", (sb.toString()));
                    //tpmsLogger.addRecordToLog("TPMS : " + (sb.toString()));
                    //String readMessage = new String(readBuf, 0, msg.arg1);
                    if (allTyre) {
                        if (allTyreDataCounter <= 98) {
                            allTyreData.append(sb);
                            *//*int jj = 0;
                            for (byte b: readBuf) {
                                if(jj < msg.arg1) {
                                    data[jj+offset] = String.format("%02X ", b);
                                }else {
                                    break;
                                }
                                jj++;
                            }
                            offset += msg.arg1;*//*
                            if (allTyreDataCounter == 98 && !dataCollected) {
                                showMsg(msg);
                                dataCollected = true;
                            }

                        } else {
                            //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + allTyreData.toString());
                            if(!dataCollected) {
                                showMsg(msg);
                                dataCollected = true;
                            }
                        }
                    } else {
                        //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + sb.toString());
                        Logger.addRecordToLog("TPMS : " + (sb.toString()));
                    }*/
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        //Toast.makeText(activity, "Connected to "
                        //        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                        ThreadSend();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;

                case MESSAGE_SEND: /*6*/
                    sendData(msg.arg1);
                    break;
            }
        }
    };

    private void processTyreData(byte[] readBuf, int dataLength) {
        int pres;
        int temp;
        double pressure;

        pres = (int) (((((readBuf[9] & 3) * 256) + (readBuf[10] & 255))) * 0.025);
        pressure = ((double) (((readBuf[9] & 3) * 256) + (readBuf[10] & 255))) * 0.025d;
        Log.e(TAG, "Pressure: " + pressure);
        pressure = (double) (pressure * 14.5d);

        Log.e(TAG, "Pressure: " + pressure);

        temp = readBuf[11] & 255;

        if (temp != 0) {
            temp -= 50;
        }

        Log.e(TAG, "Temp: " + temp);

        if (readBuf[5] != 0 && dataLength == 14) {
            switch (readBuf[5]) {
                case 1:
                    Log.e(TAG, "First Tyre");
                    tvFlTyno.setText(String.format("%02X ", readBuf[6]) + "" + String.format("%02X ", readBuf[7]) + "" + String.format("%02X ", readBuf[8]));
                    tvFlPresure.setText(pressure + " PSI");
                    tvFlTemp.setText("" + temp + (char) 0x00B0 + "C");
                    break;
                case 2:
                    Log.e(TAG, "Second Tyre");
                    tvFrtyno.setText(String.format("%02X ", readBuf[6]) + "" + String.format("%02X ", readBuf[7]) + "" + String.format("%02X ", readBuf[8]));
                    tvFRPressure.setText(pressure + " PSI");
                    tvFrTemp.setText("" + temp + (char) 0x00B0 + "C");
                    break;
                case 3:
                    Log.e(TAG, "Third Tyre");
                    tvRLOTyno.setText(String.format("%02X ", readBuf[6]) + "" + String.format("%02X ", readBuf[7]) + "" + String.format("%02X ", readBuf[8]));
                    tvRLOPresure.setText(pressure + " PSI");
                    tvRLOTemp.setText("" + temp + (char) 0x00B0 + "C");
                    break;
                case 4:
                    Log.e(TAG, "Fourth Tyre");
                    tvRLITyno.setText(String.format("%02X ", readBuf[6]) + "" + String.format("%02X ", readBuf[7]) + "" + String.format("%02X ", readBuf[8]));
                    tvRLIPresure.setText(pressure + " PSI");
                    tvRLITemp.setText("" + temp + (char) 0x00B0 + "C");
                    break;
                case 5:
                    Log.e(TAG, "Fifth Tyre");
                    tvRROTyno.setText(String.format("%02X ", readBuf[6]) + "" + String.format("%02X ", readBuf[7]) + "" + String.format("%02X ", readBuf[8]));
                    tvRROPresure.setText(pressure + " PSI");
                    tvRROTemp.setText("" + temp + (char) 0x00B0 + "C");
                    break;
                case 6:
                    Log.e(TAG, "Sixth Tyre");
                    tvRRITyno.setText(String.format("%02X ", readBuf[6]) + "" + String.format("%02X ", readBuf[7]) + "" + String.format("%02X ", readBuf[8]));
                    tvRRIPresure.setText(pressure + " PSI");
                    tvRRITemp.setText("" + temp + (char) 0x00B0 + "C");
                    break;
            }
        } else {
            Log.e(TAG, "Invalid tyre number or invalid length data: " + readBuf[5] + ", " + dataLength);
        }
    }

    /*private void showMsg(Message msg) {
        String data[] = allTyreData.toString().split(" ");
        showTyreData(data, msg.arg1);
        Logger.addRecordToLog("TPMS : " + (allTyreData.toString()));
        Log.e("TPMS All data: ", (allTyreData.toString()));
        allTyreData = new StringBuilder();
        allTyreDataCounter = 0;
        offset = 0;
    }*/

    /*private void showTyreData(String[] data, int arg1) {
        *//*int fl = Integer.parseInt(data[14]);
        int fr = Integer.parseInt(data[29]);
        int flo = Integer.parseInt(data[44]);
        int fli = Integer.parseInt(data[59]);
        int fro = Integer.parseInt(data[74]);
        int fri = Integer.parseInt(data[89]);*//*

        Log.e(TAG, "Inside showTyreData");

        int pressureLower = Integer.parseInt(data[18], 16);
        int pressureUpper = Integer.parseInt(data[19], 16);

        int pressureInt = Integer.parseInt(calculatePSI(String.format("%8s", Integer.toBinaryString(pressureLower)).replace(' ', '0'), String.format("%8s", Integer.toBinaryString(pressureUpper)).replace(' ', '0')), 2);

        Log.d(TAG, pressureInt + "");
        int pressure = (int) ((pressureInt * 0.025) * 14.5038);

        tvFlTyno.setText(data[15] + " " + data[16] + " " + data[17]);
        tvFlPresure.setText(pressure + " PSI");
        int temp = Integer.parseInt(data[20], 16);
        if (temp != 0) {
            temp -= 50;
        }
        tvFlTemp.setText("" + temp + (char) 0x00B0 + "C");

        pressureLower = Integer.parseInt(data[33], 16);
        pressureUpper = Integer.parseInt(data[34], 16);

        pressureInt = Integer.parseInt(calculatePSI(String.format("%8s", Integer.toBinaryString(pressureLower)).replace(' ', '0'), String.format("%8s", Integer.toBinaryString(pressureUpper)).replace(' ', '0')), 2);

        Log.d(TAG, pressureInt + "");
        pressure = (int) ((pressureInt * 0.025) * 14.5038);

        tvFrtyno.setText(data[30] + " " + data[31] + " " + data[32]);
        tvFRPressure.setText(pressure + " PSI");
        temp = Integer.parseInt(data[35], 16);
        if (temp != 0) {
            temp -= 50;
        }
        tvFrTemp.setText("" + temp  + (char) 0x00B0 + "C");

        pressureLower = Integer.parseInt(data[48], 16);
        pressureUpper = Integer.parseInt(data[49], 16);

        pressureInt = Integer.parseInt(calculatePSI(String.format("%8s", Integer.toBinaryString(pressureLower)).replace(' ', '0'), String.format("%8s", Integer.toBinaryString(pressureUpper)).replace(' ', '0')), 2);

        Log.d(TAG, pressureInt + "");
        pressure = (int) ((pressureInt * 0.025) * 14.5038);

        tvRLOTyno.setText(data[45] + " " + data[46] + " " + data[47]);
        tvRLOPresure.setText(pressure + " PSI");
        temp = Integer.parseInt(data[50], 16);
        if (temp != 0) {
            temp -= 50;
        }
        tvRLOTemp.setText("" + temp + (char) 0x00B0 + "C");

        pressureLower = Integer.parseInt(data[63], 16);
        pressureUpper = Integer.parseInt(data[64], 16);

        pressureInt = Integer.parseInt(calculatePSI(String.format("%8s", Integer.toBinaryString(pressureLower)).replace(' ', '0'), String.format("%8s", Integer.toBinaryString(pressureUpper)).replace(' ', '0')), 2);

        Log.d(TAG, pressureInt + "");
        pressure = (int) ((pressureInt * 0.025) * 14.5038);

        tvRLITyno.setText(data[60] + " " + data[61] + " " + data[62]);
        tvRLIPresure.setText(pressure + " PSI");
        temp = Integer.parseInt(data[65], 16);
        if (temp != 0) {
            temp -= 50;
        }
        tvRLITemp.setText("" + temp + (char) 0x00B0 + "C");

        pressureLower = Integer.parseInt(data[78], 16);
        pressureUpper = Integer.parseInt(data[79], 16);

        pressureInt = Integer.parseInt(calculatePSI(String.format("%8s", Integer.toBinaryString(pressureLower)).replace(' ', '0'), String.format("%8s", Integer.toBinaryString(pressureUpper)).replace(' ', '0')), 2);

        Log.d(TAG, pressureInt + "");
        pressure = (int) ((pressureInt * 0.025) * 14.5038);

        tvRROTyno.setText(data[75] + " " + data[76] + " " + data[77]);
        tvRROPresure.setText(pressure + " PSI");
        temp = Integer.parseInt(data[80], 16);
        if (temp != 0) {
            temp -= 50;
        }
        tvRROTemp.setText("" + temp + (char) 0x00B0 + "C");

        pressureLower = Integer.parseInt(data[93], 16);
        pressureUpper = Integer.parseInt(data[94], 16);

        pressureInt = Integer.parseInt(calculatePSI(String.format("%8s", Integer.toBinaryString(pressureLower)).replace(' ', '0'), String.format("%8s", Integer.toBinaryString(pressureUpper)).replace(' ', '0')), 2);

        Log.d(TAG, pressureInt + "");
        pressure = (int) ((pressureInt * 0.025) * 14.5038);

        tvRRITyno.setText(data[90] + " " + data[91] + " " + data[92]);
        tvRRIPresure.setText(pressure + " PSI");
        temp = Integer.parseInt(data[95], 16);
        if (temp != 0) {
            temp -= 50;
        }
        tvRRITemp.setText("" + temp + (char) 0x00B0 + "C");
    }*/

    /*private String calculatePSI(String lower, String upper) {
        Log.d(TAG, lower);
        Log.d(TAG, upper);
        String bArray[] = (lower + upper).split("");
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String b : bArray) {
            if (i > 6) {
                sb.append(b);
            }
            i++;
        }
        Log.d(TAG, sb.toString());
        return sb.toString();
    }*/

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    //send message
                    //sendMessages();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(activity, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    //getActivity().finish();
                }
        }
    }

    private void setStatus(int resId) {
        if (null == activity) {
            return;
        }
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    private void setStatus(CharSequence subTitle) {
        if (null == activity) {
            return;
        }
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device/*, secure*/);
    }

    /*private void sendMessages() {
        Log.e("TPMS", "sendMessage called!");
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(activity, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        //char[] handShake = {0xAA, 0x41, 0xA1, 0x06, 0x11, 0xa3};
        char[] frontLeft9422 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x01, 0xBA, 0x6C, 0x43, 0x00, 0x00, 0x00, 0x00, 0x67};
        char[] frontRight9422 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x02, 0xBA, 0x6B, 0x7F, 0x00, 0x00, 0x00, 0x00, 0xA3};
        char[] rearLeftOuter9422 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x03, 0x56, 0xA8, 0xCB, 0x00, 0x00, 0x00, 0x00, 0xC9};
        char[] rearLeftInner9422 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x04, 0x56, 0xA6, 0xBE, 0x00, 0x00, 0x00, 0x00, 0xBB};
        char[] rearRightOuter9422 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x05, 0x56, 0xA7, 0x81, 0x00, 0x00, 0x00, 0x00, 0x80};
        char[] rearRightInner9422 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x06, 0x56, 0xA7, 0xC5, 0x00, 0x00, 0x00, 0x00, 0xC5};
        char[] frontLeft9458 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x01, 0xBA, 0x6A, 0x90, 0x00, 0x00, 0x00, 0x00, 0xB6};
        char[] frontRight9458 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x02, 0xBA, 0x6D, 0xAF, 0x00, 0x00, 0x00, 0x00, 0xD9};
        char[] rearLeftOuter9458 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x03, 0xBA, 0x6C, 0xDD, 0x00, 0x00, 0x00, 0x00, 0x04};
        char[] rearLeftInner9458 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x04, 0x56, 0xA9, 0x9D, 0x00, 0x00, 0x00, 0x00, 0x9C};
        char[] rearRightOuter9458 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x05, 0xBA, 0x6B, 0x75, 0x00, 0x00, 0x00, 0x00, 0x99};
        char[] rearRightInner9458 = {0xAA, 0x41, 0xA1, 0x0E, 0x63, 0x06, 0xBA, 0x6B, 0x09, 0x00, 0x00, 0x00, 0x00, 0x2C};
        char[] getAllTyreData = {0xAA, 0x41, 0xA1, 0x07, 0x63, 0x00, 0xF6};
        char[] getAllSensorId = {0xAA, 0x41, 0xA1, 0x07, 0x64, 0x10, 0x07};
        //char[] getFirstTyreData = {0xAA, 0x41, 0xA1, 0x07, 0x63, 0x01, 0xF7};
        //char[] getAllAlertData = {0xAA, 0x41, 0xA1, 0x07, 0x62, 0x0, 0xF5};
        byte[] getFirstTyreData = {-86, 65, -95, 7, 99, 1, -9};
        byte[] handShake = {};
        byte[] getAllTyreDatax = {-86, 65, -95, 7, 99, 0, -117 };

        final byte[][] busOne = {handShake*//*, frontRight9422, rearLeftOuter9422, rearLeftInner9422, rearRightOuter9422,
                rearRightInner9422*//*, *//*frontLeft9458, frontRight9458, rearLeftOuter9458, rearLeftInner9458,
                rearRightOuter9458, rearRightInner9458, *//* getAllTyreDatax};

        sendHex(busOne);
        //mChatService.write(getFirstTyreData);

    }

    private void sendHex(final byte[][] busOne) {
        i = 0;
        countDownTimer = new CountDownTimer(1800000, 500) {
            public void onTick(long millisUntilFinished) {
                Log.e(TAG, "Timer Ticked!");
                if (i < 2) {
                    mChatService.write(busOne[i]);
                    i++;
                } else {
                    countDownTimer.cancel();
                }
            }

            @Override
            public void onFinish() {
                Log.e(TAG, "Timer Finished!");
            }
        }.start();
    }*/

    private void sendData(int i) {
        Log.e(TAG, "Inside sendData!");
        if(!handShake){
            byte[] sendData = new byte[]{(byte) -86, (byte) 65, (byte) -95, (byte) 6, (byte) 17, (byte) -93};
            this.mChatService.write(sendData);
            handShake = true;
            try {
                Thread.sleep(10000);
               /* //byte[] sendAllData = new byte[]{(byte) -86, (byte) 65, (byte) -95, (byte) 7, (byte) 102, (byte) 0, (byte) -7};
                byte[] sendAllData = {(byte)-86, (byte)65, (byte)-95, (byte)7, (byte)99, (byte)0, (byte)-10 };
                this.mChatService.write(sendAllData);*/
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (i < MESSAGE_SEND) {
            byte[] senddata = new byte[]{(byte) -86, (byte) 65, (byte) -95, (byte) 7, (byte) 99, (byte) 1, (byte) -9};
            //byte[] senddata = new byte[]{(byte) -86, (byte) 65, (byte) -95, (byte) 7, (byte) 102, (byte) 1, (byte) -6};
            senddata[MESSAGE_TOAST] = (byte) (senddata[MESSAGE_TOAST] + ((byte) i));
            senddata[MESSAGE_SEND] = (byte) (senddata[MESSAGE_SEND] + ((byte) i));
            this.mChatService.write(senddata);
        }
    }

    public void ThreadSend() {
        Log.e(TAG, "Inside sendData!");
        if (this.mChatService.getState() != REQUEST_ENABLE_BT) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
        } else {
            new Thread(new Runnable() {
                public void run() {
                    Log.e(TAG, "Inside sendData run!");
                    countex = 0;
                    if (!refreshingData) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getBaseContext(), "Refreshing Tyre Data", Toast.LENGTH_SHORT).show();
                            }
                        });
                        while (requestCounter < 600) {
                            Log.e(TAG, "Inside sendData run while countex : " + countex);
                            refreshingData = true;
                            Message m = mHandler.obtainMessage();
                            m.what = MESSAGE_SEND;
                            m.arg1 = countex;
                            mHandler.sendMessage(m);
                            countex += 1;
                            requestCounter += 1;
                            if (countex == 6) {
                                countex = 0;
                            }
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        refreshingData = false;
                        requestCounter = 0;
                        //handShake = false;
                        Log.e(TAG, "refreshingData : " + refreshingData);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                completeRefresh();
                            }
                        });

                    } else {
                        //Toast.makeText(VehicleTyreDetailsActivity.this, "Please wait, Refreshing Tyre Data", Toast.LENGTH_SHORT).show();
                    }
                }
            }).start();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            //send message
            //sendMessages();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetooth_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_data: {
                // Launch the DeviceListActivity to see devices and do scan
                Log.e(TAG, "Refresh data clicked");
                if (!refreshingData) {
                    if (mConnectedDeviceName != null) {
                        refreshItem = item;
                        refresh();
                        ThreadSend();
                    } else {
                        Toast.makeText(VehicleTyreDetailsActivity.this, "Not connected to device", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VehicleTyreDetailsActivity.this, "Please wait, Refreshing Tyre Data", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                if (mConnectedDeviceName != null) {
                    //Toast.makeText(VehicleTyreDetailsActivity.this, "Already connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    if (mChatService != null) {
                        mChatService.stop();
                        mConnectedDeviceName = null;
                        completeRefresh();
                    }
                } else {
                    Intent serverIntent = new Intent(activity, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                    return true;
                }
            }
            /*case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }*/
        }
        return false;
    }

    public void refresh() {
     /* Attach a rotating ImageView to the refresh item as an ActionView */
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_layout, null);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh_animation);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);

        refreshItem.setActionView(iv);

        //TODO trigger loading
    }

    public void completeRefresh() {
        if (refreshItem != null) {
            refreshItem.getActionView().clearAnimation();
            refreshItem.setActionView(null);
            refreshItem = null;
        }
    }
}
