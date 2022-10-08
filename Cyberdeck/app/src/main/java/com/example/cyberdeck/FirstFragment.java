package com.example.cyberdeck;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.cyberdeck.databinding.FragmentFirstBinding;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

public class FirstFragment extends Fragment
        implements Runnable{

    private FragmentFirstBinding binding;
    private static final int targetVendorID = 0x1A86; //Arduino Uno
    private static final int targetProductID = 0x7523; //Arduino Uno, not 0067
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    PendingIntent mPermissionIntent;
    UsbDevice deviceFound = null;
    UsbInterface usbInterfaceFound = null;
    UsbInterface usbInterface;
    UsbDeviceConnection usbDeviceConnection;
    UsbEndpoint endpointIn = null;
    UsbEndpoint endpointOut = null;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);

        mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        Context context=getContext();
        context.registerReceiver(mUsbReceiver, filter);

        context.registerReceiver(mUsbDeviceReceiver, new IntentFilter(
                UsbManager.ACTION_USB_DEVICE_ATTACHED));
        context.registerReceiver(mUsbDeviceReceiver, new IntentFilter(
                UsbManager.ACTION_USB_DEVICE_DETACHED));




        return binding.getRoot();

    }
    private void connectUsb() {

        binding.textStatus.setText(binding.textStatus.getText()+"\n"+"connectUsb()");

        searchEndPoint();

        if (usbInterfaceFound != null) {
            binding.textStatus.setText(binding.textStatus.getText()+"\n"+"found interface");
            setupUsbComm();
        }

    }
    private boolean setupUsbComm() {

        // for more info, search SET_LINE_CODING and
        // SET_CONTROL_LINE_STATE in the document:
        // "Universal Serial Bus Class Definitions for Communication Devices"
        // at http://adf.ly/dppFt
        final int RQSID_SET_LINE_CODING = 0x20;
        final int RQSID_SET_CONTROL_LINE_STATE = 0x22;

        boolean success = false;

        UsbManager manager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
        Boolean permitToRead = manager.hasPermission(deviceFound);

        if (permitToRead) {
            usbDeviceConnection = manager.openDevice(deviceFound);
            if (usbDeviceConnection != null) {
                usbDeviceConnection.claimInterface(usbInterfaceFound, true);

                usbDeviceConnection.controlTransfer(0x21, // requestType
                        RQSID_SET_CONTROL_LINE_STATE, // SET_CONTROL_LINE_STATE
                        0, // value
                        0, // index
                        null, // buffer
                        0, // length
                        0); // timeout

                // baud rate = 9600
                // 8 data bit
                // 1 stop bit
                byte[] encodingSetting = new byte[] { (byte) 0x80, 0x25, 0x00,
                        0x00, 0x00, 0x00, 0x08 };
                usbDeviceConnection.controlTransfer(0x21, // requestType
                        RQSID_SET_LINE_CODING, // SET_LINE_CODING
                        0, // value
                        0, // index
                        encodingSetting, // buffer
                        7, // length
                        0); // timeout

                //2015-04-15
                Thread thread = new Thread(this);
                thread.start();
            }

        } else {
            manager.requestPermission(deviceFound, mPermissionIntent);
            binding.textStatus.setText(binding.textStatus.getText()+"\n"+"Permission: " + permitToRead);
        }

        return success;
    }
    private void releaseUsb() {

        binding.textStatus.setText(binding.textStatus.getText()+"\n"+"releaseUsb()");

        if (usbDeviceConnection != null) {
            if (usbInterface != null) {
                usbDeviceConnection.releaseInterface(usbInterface);
                usbInterface = null;
            }
            usbDeviceConnection.close();
            usbDeviceConnection = null;
        }

        deviceFound = null;
        usbInterfaceFound = null;
        endpointIn = null;
        endpointOut = null;
    }

    private void searchEndPoint() {

        usbInterfaceFound = null;
        endpointOut = null;
        endpointIn = null;

        // Search device for targetVendorID and targetProductID
        if (deviceFound == null) {
            UsbManager manager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();

            while (deviceIterator.hasNext()) {
                UsbDevice device = deviceIterator.next();

                if (device.getVendorId() == targetVendorID) {
                    if (device.getProductId() == targetProductID) {
                        deviceFound = device;
                    }
                }
            }
        }

        if (deviceFound == null) {
            binding.textStatus.setText(binding.textStatus.getText()+"\n"+"device not found");
        } else {

            // Search for UsbInterface with Endpoint of USB_ENDPOINT_XFER_BULK,
            // and direction USB_DIR_OUT and USB_DIR_IN

            for (int i = 0; i < deviceFound.getInterfaceCount(); i++) {
                UsbInterface usbif = deviceFound.getInterface(i);

                UsbEndpoint tOut = null;
                UsbEndpoint tIn = null;

                int tEndpointCnt = usbif.getEndpointCount();
                if (tEndpointCnt >= 2) {
                    for (int j = 0; j < tEndpointCnt; j++) {
                        if (usbif.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                            if (usbif.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT) {
                                tOut = usbif.getEndpoint(j);
                            } else if (usbif.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN) {
                                tIn = usbif.getEndpoint(j);
                            }
                        }
                    }

                    if (tOut != null && tIn != null) {
                        // This interface have both USB_DIR_OUT
                        // and USB_DIR_IN of USB_ENDPOINT_XFER_BULK
                        usbInterfaceFound = usbif;
                        endpointOut = tOut;
                        endpointIn = tIn;
                    }
                }

            }

        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {

                binding.textStatus.setText(binding.textStatus.getText()+"\n"+"ACTION_USB_PERMISSION");

                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            connectUsb();
                        }
                    } else {
                        binding.textStatus.setText(binding.textStatus.getText()+"\n"+"permission denied for device ");
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mUsbDeviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {

                deviceFound = (UsbDevice) intent
                        .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                binding.textStatus.setText(binding.textStatus.getText()+"\n"+"ACTION_USB_DEVICE_ATTACHED");

                connectUsb();

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                UsbDevice device = (UsbDevice) intent
                        .getParcelableExtra(UsbManager.EXTRA_DEVICE);

                binding.textStatus.setText(binding.textStatus.getText()+"\n"+"ACTION_USB_DEVICE_DETACHED");

                if (device != null) {
                    if (device == deviceFound) {
                        releaseUsb();
                    }
                }

            }
        }

    };

    int state=0;
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                state= (state + 1) % 3;
                byte[] buf = {(byte)48,(byte)49,(byte)(48+state)};
                try {
                    String val=""+buf[2];
                    binding.textStatus.setText(binding.textStatus.getText()+"setting val "+val);
                    usbDeviceConnection.bulkTransfer(endpointOut,
                            buf, buf.length, 0);
                }
                catch  (Exception e)
                {
                    binding.textStatus.setText(binding.textStatus.getText()+"\n"+"failed to send");
                }

                //                if(mPhysicaloid.open()) {

//                    mPhysicaloid.write(buf, buf.length);
//
//                    mPhysicaloid.addReadListener(new ReadLisener() {
//                        @Override
//                        public void onRead(int size) {
//                            byte[] buf = new byte[size];
//                            mPhysicaloid.read(buf, size);
//
//                        }
//                    });
//                } else {
//                    Toast.makeText(getContext(), "Cannot open", Toast.LENGTH_LONG).show();
//                }



//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });
    }
    int indexRx;
    final int RX_STATE_0_IDLE = 0;
    final int RX_STATE_1_RXing = 1;
    int RxState;
    final byte PIXELON = 'A';
    final byte PIXELOFF = 'B';
    String logString = "";
    @Override
    public void run() {
        RxState = RX_STATE_0_IDLE;
        byte[] buf = new byte[255];
        int numRead=0;

//        ByteBuffer buffer = ByteBuffer.allocate(7);
//        UsbRequest request = new UsbRequest();
//        request.initialize(usbDeviceConnection, endpointIn);
        while (true) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                numRead=usbDeviceConnection.bulkTransfer(endpointIn,buf,0,255,1000);
            }
            else
            {
                getActivity().runOnUiThread(new Runnable(){

                    @Override
                    public void run() {

                        binding.textStatus.setText(binding.textStatus.getText()+"\n"+"cannot receive");
                    }});

            }
//            request.queue(buffer, 7);
//            if (usbDeviceConnection.requestWait() == request) {
//                byte[] buf= buffer.array();
//
//
            if(numRead>0)
            {
                for (int i = 0; i < numRead; i++) {
                    logString += buf[i] + " ";
                }


                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        binding.textStatus.setText(binding.textStatus.getText() + logString + "\n");
                        logString = "";
                    }
                });
            }

//            }else{
//                break;
//            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}