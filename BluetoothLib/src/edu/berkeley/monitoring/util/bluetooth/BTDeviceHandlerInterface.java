package edu.berkeley.monitoring.util.bluetooth;

public interface BTDeviceHandlerInterface {
	void onReceive(BTSendableInterface<?> o);
	void onFailure(Exception e);
	void onConnect(String Name);
}
