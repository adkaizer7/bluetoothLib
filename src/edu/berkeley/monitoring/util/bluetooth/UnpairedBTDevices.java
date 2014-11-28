package edu.berkeley.monitoring.util.bluetooth;

import android.bluetooth.BluetoothDevice;

public class UnpairedBTDevices extends BluetoothHealthMonitoringDevice{
	public String deviceName;
	public String macAddress;
	
	public UnpairedBTDevices(String devName, String macAdd,BluetoothDevice dev){
		super(dev);
		this.deviceName = devName;
		this.macAddress = macAdd;
	}
	

}
