package edu.berkeley.monitoring.util.bluetooth;

import java.io.Serializable;

import android.bluetooth.BluetoothDevice;

public class BluetoothHealthMonitoringDevice implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5524397443847841410L;
	BluetoothDevice device;
	
	public BluetoothHealthMonitoringDevice(BluetoothDevice dev)
	{
		device = dev;
	}
	
	public BluetoothDevice getBlutoothDevice(){
		return this.device;
	}
}
