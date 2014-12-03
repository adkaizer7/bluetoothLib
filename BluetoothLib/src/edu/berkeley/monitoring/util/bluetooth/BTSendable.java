package edu.berkeley.monitoring.util.bluetooth;

import java.io.Serializable;

public class BTSendable<T> implements BTSendableInterface<T>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5617644480344851351L;
	
	final private T obj;
	
	public BTSendable(T o) {
		this.obj = o;
	}
	
	public T getObj() {
		return this.obj;
	}
}
