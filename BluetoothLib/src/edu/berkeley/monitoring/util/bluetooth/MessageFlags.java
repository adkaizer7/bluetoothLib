package edu.berkeley.monitoring.util.bluetooth;

public enum MessageFlags{
		MESSAGE_STATE_CHANGE(0),
		MESSAGE_READ(1),
		MESSAGE_WRITE(2),
		MESSAGE_DEVICE_NAME(3),
		MESSAGE_TOAST(4);
		
		private int value;
		
		MessageFlags(int value)
		{
			this.value = value;
		}
		
		public int getValue(){
			return this.value;
		}
	}
