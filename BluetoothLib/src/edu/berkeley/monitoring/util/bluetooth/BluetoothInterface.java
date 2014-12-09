package edu.berkeley.monitoring.util.bluetooth;


public interface BluetoothInterface {
	
	public void onFinishedScanning();	
	public void onObtainedOneUnpairedDevices(UnpairedBTDevices unpairedBTDevice);
	public void onObtainedOnePairedDevices(PairedBTDevices pairedBTDevice);
	public void onFinishedObtainingPairedDevices();
	public void onFinishObtainingUnpairedDevices();
	public void onSwitchingonBluetooth();
	public void onBeginAccept();
	//public String pickDeviceToPairWith(String address);
	public void onIncomingConnection(PairedBTDevices pairedDevice);
}
