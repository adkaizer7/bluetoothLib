package edu.berkeley.monitoring.util.bluetooth;

public interface BTDeviceHandlerInterface {
	void onReceive(BTSendable<?> o);
	void onFailure();
	void onConnect(String Name);
}
