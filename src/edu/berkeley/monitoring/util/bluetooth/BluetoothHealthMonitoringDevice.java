package edu.berkeley.monitoring.util.bluetooth;

import android.bluetooth.BluetoothDevice;

public class BluetoothHealthMonitoringDevice {
	BluetoothDevice device;
	
	public BluetoothHealthMonitoringDevice(BluetoothDevice dev)
	{
		device = dev;
	}
	
	public BluetoothDevice getBlutoothDevice(){
		return this.device;
	}
}
