package edu.berkeley.monitoring.util.bluetooth;

import java.io.Serializable;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

public class UnpairedBTDevices extends BluetoothHealthMonitoringDevice implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4810239452599001857L;
	public String deviceName;
	public String macAddress;
	
	private BluetoothAdapter mBluetoothAdapter;
	private Handler mHandler;
	
	public UnpairedBTDevices(String devName, String macAdd, BluetoothDevice dev, Handler handler){
		super(dev);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		this.deviceName = devName;
		this.macAddress = macAdd;
		mHandler = handler;
	}
	
	public String getName(){
		return this.getBlutoothDevice().getName();
		
	}
	
	public String getAddress(){
		return this.getBlutoothDevice().getAddress();
	}

	public PairedBTDevices pairToDevice(UnpairedBTDevices device){
		// Make a bluetooth device object from the unpaired device object
		BluetoothDevice deviceToPair = mBluetoothAdapter.getRemoteDevice(device.getAddress());

		// Attempt pairing with device
		try {
            //Method method = deviceToPair.getClass().getMethod("createBond", (Class[]) null);
            //method.invoke(deviceToPair, (Object[]) null);
			deviceToPair.createBond();
        } catch (Exception e) {
            e.printStackTrace();
        }       
		return (new PairedBTDevices(deviceToPair,mHandler));
	}

}
