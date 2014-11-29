package edu.berkeley.monitoring.util.bluetooth;

import java.io.Serializable;

import android.bluetooth.BluetoothDevice;

public class UnpairedBTDevices extends BluetoothHealthMonitoringDevice implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4810239452599001857L;
	public String deviceName;
	public String macAddress;
	
	public UnpairedBTDevices(String devName, String macAdd,BluetoothDevice dev){
		super(dev);
		this.deviceName = devName;
		this.macAddress = macAdd;
	}
	

}
