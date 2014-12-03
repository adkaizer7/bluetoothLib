package edu.berkeley.monitoring.util.bluetooth;

public class BluetoothExceptions extends Exception{

	/**
	 * Unique serial ID for serialization 
	 */
	private static final long serialVersionUID = 7026446725617507522L;
	public static int UNKNOWN_EXCEPTION = 0;
	public static int CANNOT_TURN_ON_BLUETOOTH = 1;
	public static int NO_PAIRED_DEVICES = 2;
	public static int DEVICE_NOT_FOUND = 3;
	private int errorCode = UNKNOWN_EXCEPTION;
	
	
	/**
	 * 
	 * @param errCode
	 */
	public BluetoothExceptions(int errCode)
	{
		this.errorCode = errCode;
	}
	
	public BluetoothExceptions()
	{
		
	}
	
	public int getErrorCode()
	{
		return this.errorCode;
	}

}
