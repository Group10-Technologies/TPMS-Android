package com.group10.tpms;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothService {
    private static final boolean D = true;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String NAME = "BluetoothChat";
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_NONE = 0;
    private static final String TAG = "BluetoothService";
    private AcceptThread mAcceptThread;
    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private final Handler mHandler;
    private int mState = STATE_NONE;
    private Context mContext;

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = BluetoothService.this.mAdapter.listenUsingInsecureRfcommWithServiceRecord(BluetoothService.NAME, BluetoothService.MY_UUID);
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "listen() failed", e);
            }
            this.mmServerSocket = tmp;
        }

        public void run() {
            Log.d(BluetoothService.TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            while (BluetoothService.this.mState != BluetoothService.STATE_CONNECTED) {
                try {
                    BluetoothSocket socket = this.mmServerSocket.accept();
                    if (socket != null) {
                        synchronized (BluetoothService.this) {
                            switch (BluetoothService.this.mState) {
                                case BluetoothService.STATE_NONE /*0*/:
                                case BluetoothService.STATE_CONNECTED /*3*/:
                                    try {
                                        socket.close();
                                        break;
                                    } catch (IOException e) {
                                        Log.e(BluetoothService.TAG, "Could not close unwanted socket", e);
                                        break;
                                    }
                                case BluetoothService.STATE_LISTEN /*1*/:
                                case BluetoothService.STATE_CONNECTING /*2*/:
                                    BluetoothService.this.connected(socket, socket.getRemoteDevice());
                                    break;
                            }
                        }
                    }
                } catch (IOException | NullPointerException e2) {
                    Log.e(BluetoothService.TAG, "accept() failed");
                    break;
                }
            }
            Log.i(BluetoothService.TAG, "END mAcceptThread");
        }

        public void cancel() {
            Log.d(BluetoothService.TAG, "cancel " + this);
            try {
                this.mmServerSocket.close();
            } catch (IOException | NullPointerException e) {
                Log.e(BluetoothService.TAG, "close() of server failed", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothDevice mmDevice;
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(BluetoothService.MY_UUID);
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "create() failed", e);
            }
            this.mmSocket = tmp;
        }

        public void run() {
            Log.i(BluetoothService.TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            BluetoothService.this.mAdapter.cancelDiscovery();
            try {
                this.mmSocket.connect();
                synchronized (BluetoothService.this) {
                    Log.e(TAG, "Inside Synchronized");
                    BluetoothService.this.mConnectThread = null;
                }
                BluetoothService.this.connected(this.mmSocket, this.mmDevice);
            } catch (IOException e) {
                BluetoothService.this.connectionFailed();
                try {
                    this.mmSocket.close();
                    BluetoothDevice hxm = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(mmDevice.getAddress());
                    Method m;
                    //m = hxm.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    m = hxm.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                    mmSocket = (BluetoothSocket) m.invoke(hxm, Integer.valueOf(1));
                    connected(mmSocket, mmDevice);
                } catch (IOException e2) {
                    Log.e(BluetoothService.TAG, "unable to close() socket during connection failure", e2);
                } catch (NullPointerException e22) {
                    Log.e(BluetoothService.TAG, "unable to close() socket during connection failure", e22);
                } catch (NoSuchMethodException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                }
                BluetoothService.this.start();
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException | NullPointerException e) {
                Log.e(BluetoothService.TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(BluetoothService.TAG, "create ConnectedThread");
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "temp sockets not created", e);
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(BluetoothService.TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            byte[] buf = new byte[50];
            int iStep = BluetoothService.STATE_NONE;
            int iCheckSum = BluetoothService.STATE_NONE;
            while (true) {
                if (mState == STATE_CONNECTED) {
                    try {
                        int bytes = this.mmInStream.read(buffer);
                        Log.e(TAG, "data size: " + bytes);
                        StringBuilder sb = new StringBuilder();
                        for(int i = 0 ; i<bytes;i++){
                            sb.append(String.format("%02X ", buffer[i]));
                        }

                        Log.e("Inside Service", sb.toString());

                        if (bytes > 0) {
                            int i = BluetoothService.STATE_NONE;
                            if (buffer[BluetoothService.STATE_NONE] == (byte) -86) {
                                buf[BluetoothService.STATE_NONE] = buffer[BluetoothService.STATE_NONE];
                                iStep = BluetoothService.STATE_NONE + BluetoothService.STATE_LISTEN;
                                i = BluetoothService.STATE_NONE + BluetoothService.STATE_LISTEN;
                            }
                            while (i < bytes) {
                                switch (iStep) {
                                    case BluetoothService.STATE_LISTEN /*1*/:
                                        if (buffer[i] != (byte) -95) {
                                            iStep = BluetoothService.STATE_NONE;
                                            break;
                                        }
                                        buf[iStep] = buffer[i];
                                        iStep += BluetoothService.STATE_LISTEN;
                                        break;
                                    case BluetoothService.STATE_CONNECTING /*2*/:
                                        if (buffer[i] != (byte) 65) {
                                            iStep = BluetoothService.STATE_NONE;
                                            break;
                                        }
                                        buf[iStep] = buffer[i];
                                        iStep += BluetoothService.STATE_LISTEN;
                                        break;
                                    case BluetoothService.STATE_CONNECTED /*3*/:
                                        if (buffer[i] != (byte) 14) {
                                            if (buffer[i] != (byte) 18) {
                                                if (buffer[i] != (byte) 7) {
                                                    iStep = BluetoothService.STATE_NONE;
                                                    break;
                                                }
                                                buf[iStep] = buffer[i];
                                                iStep += BluetoothService.STATE_LISTEN;
                                                iCheckSum = 403;
                                                break;
                                            }
                                            buf[iStep] = buffer[i];
                                            iStep += BluetoothService.STATE_LISTEN;
                                            iCheckSum = 414;
                                            break;
                                        }
                                        buf[iStep] = buffer[i];
                                        iStep += BluetoothService.STATE_LISTEN;
                                        iCheckSum = 410;
                                        break;
                                    default:
                                        if (iStep > BluetoothService.STATE_CONNECTED && iStep < 18) {
                                            buf[iStep] = buffer[i];
                                            iStep += BluetoothService.STATE_LISTEN;
                                            if (iStep == 7 && buf[BluetoothService.STATE_CONNECTED] == (byte) 7 && (iCheckSum & 255) == (buffer[i] & 255)) {
                                                BluetoothService.this.mHandler.obtainMessage(BluetoothService.STATE_CONNECTING, iStep, -1, buf).sendToTarget();
                                                Log.e(TAG, "STATE_CONNECTING send data 1");
                                                iStep = BluetoothService.STATE_NONE;
                                                Thread.sleep(200);
                                            }
                                            if (iStep == 14 && buf[BluetoothService.STATE_CONNECTED] == (byte) 14 && (iCheckSum & 255) == (buffer[i] & 255)) {
                                                BluetoothService.this.mHandler.obtainMessage(BluetoothService.STATE_CONNECTING, iStep, -1, buf).sendToTarget();
                                                Log.e(TAG, "STATE_CONNECTING send data 2");
                                                iStep = BluetoothService.STATE_NONE;
                                                Thread.sleep(200);
                                            }
                                            if (iStep != 18 || buf[BluetoothService.STATE_CONNECTED] != (byte) 18 || (iCheckSum & 255) != (buffer[i] & 255)) {
                                                iCheckSum += buffer[i] & 255;
                                                break;
                                            }
                                            BluetoothService.this.mHandler.obtainMessage(BluetoothService.STATE_CONNECTING, iStep, -1, buf).sendToTarget();
                                            Log.e(TAG, "STATE_CONNECTING send data 3");
                                            iStep = BluetoothService.STATE_NONE;
                                            Thread.sleep(200);
                                            break;
                                        }
                                        iStep = BluetoothService.STATE_NONE;
                                        break;
                                }
                                i += BluetoothService.STATE_LISTEN;
                            }
                            Thread.sleep(100);
                        } else {
                            continue;
                        }
                    } catch (IOException | NullPointerException e) {
                        Log.e(BluetoothService.TAG, "disconnected", e);
                        BluetoothService.this.connectionLost();
                        return;
                    } catch (InterruptedException e2) {
                        e2.printStackTrace();
                    }
                }else {
                    Log.e(TAG, "Make sure device is on and try again!");
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                this.mmOutStream.write(buffer);
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
                Log.e(BluetoothService.TAG, "close() of connect socket failed", e);
            }
        }
    }

    public BluetoothService(Context context, Handler handler) {
        //mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mContext = context;
        this.mHandler = handler;
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + this.mState + " -> " + state);
        this.mState = state;
        this.mHandler.obtainMessage(STATE_LISTEN, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return this.mState;
    }

    public synchronized void start() {
        Log.d(TAG, "start");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread == null) {
            this.mAcceptThread = new AcceptThread();
            this.mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        if (this.mState == STATE_CONNECTING && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        this.mConnectThread = new ConnectThread(device);
        this.mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.start();

        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    public void write(byte[] out) {
        synchronized (this) {
            if (this.mState != STATE_CONNECTED) {
                return;
            }
            ConnectedThread r = this.mConnectedThread;
            r.write(out);
        }
    }

    private void connectionFailed() {
        setState(STATE_LISTEN);
        Message msg = this.mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        setState(STATE_LISTEN);
        Message msg = this.mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Device connection was lost");
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }
}
