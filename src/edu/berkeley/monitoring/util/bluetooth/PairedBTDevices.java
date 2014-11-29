package edu.berkeley.monitoring.util.bluetooth;

import java.io.Serializable;
import android.bluetooth.BluetoothDevice;

public class PairedBTDevices extends BluetoothHealthMonitoringDevice implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8949144926410820876L;

	public PairedBTDevices(BluetoothDevice dev){
		super(dev);
	}	
	
	public String getName(){
		return this.getBlutoothDevice().getName();
		
	}
	
	public String getAddress(){
		return this.getBlutoothDevice().getAddress();
	}

}
