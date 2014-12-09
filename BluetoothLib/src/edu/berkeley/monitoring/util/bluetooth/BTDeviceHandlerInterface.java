package edu.berkeley.monitoring.util.bluetooth;

public interface BTDeviceHandlerInterface {
	void onReceive(BTSendableInterface<?> o);
	void onFailure();
	void onConnect(String Name);
}
