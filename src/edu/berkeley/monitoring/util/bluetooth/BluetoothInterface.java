package edu.berkeley.monitoring.util.bluetooth;

import java.util.ArrayList;

public interface BluetoothInterface {
	
	public void onFinishedScanning();	
	public void onObtainedOneUnpairedDevices(String nameAndAddress);
	public void onFinishedObtainingPairedDevices(ArrayList<PairedBTDevices> pairedDevices);
	public void onFinishObtainingUnpairedDevices(ArrayList<UnpairedBTDevices> unpairedDeviceAddress);
	public void onSwitchingonBluetooth();
	//public String pickDeviceToPairWith(String address);
}
