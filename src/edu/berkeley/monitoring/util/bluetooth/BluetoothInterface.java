package edu.berkeley.monitoring.util.bluetooth;


public interface BluetoothInterface {
	
	public void onFinishedScanning();	
	public void onObtainedOneUnpairedDevices(UnpairedBTDevices unpairedBTDevice);
	public void onFinishedObtainingPairedDevices();
	public void onFinishObtainingUnpairedDevices();
	public void onSwitchingonBluetooth();
	//public String pickDeviceToPairWith(String address);
}
