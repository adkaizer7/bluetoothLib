package edu.berkeley.monitoring.util.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PairedBTDevices extends BluetoothHealthMonitoringDevice implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8949144926410820876L;
	private BluetoothAdapter mBluetoothAdapter;
	private static final boolean D = true;
	private static final String TAG = "PairedBTDevices";
	

	


    // Unique UUID for this application
    //public static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final UUID MY_UUID = UUID.fromString("e302e180-7efd-11e4-80c6-0002a5d5c51b");
    
	
    //Current state of the connection
    private StateFlags mState;
    
    //Thread to accept connections
    private ConnectThread mConnectThread;
    //Thread running communication.
    private ConnectedThread mConnectedThread;   
    
    

    //TODO     
    MessageFlags msgFlags;
  
    //Thread to start connection
    //Message handler
    //private final Handler mHandler;
    private BTDeviceHandlerInterface btDeviceHandler;
    
    public static final int STATE_NONE = 14;


	public PairedBTDevices(BluetoothDevice dev){
		super(dev);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = StateFlags.STATE_NONE;
	}

	public String getName(){
		return this.getBlutoothDevice().getName();
		
	}
	
	public String getAddress(){
		return this.getBlutoothDevice().getAddress();
	}

	
	/**
	 * Return the state of the bluetooth adapter
	 * @return mstate
	 */
	public StateFlags getState(){
		return mState;
	}
	
    /**
     * Setting the state of the Bluetooth Service
     */    
    void setState(StateFlags state){
    	mState = state;
    	return;
    }
    
    /**
     * 
     * @param handler
     */
    public void registerHandler(BTDeviceHandlerInterface handler) {
    	this.btDeviceHandler = handler;
    }
	
	
    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Start the thread to listen on a BluetoothServerSocket
        
        setState(StateFlags.STATE_LISTEN);
    }

	
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect() {
    	BluetoothDevice btDevice = this.device;
        if (D) Log.d(TAG, "connect to: " + device);
        // Cancel any thread attempting to make a connection
        if (mState == StateFlags.STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(btDevice);
        mConnectThread.start();
        if (mState == StateFlags.STATE_RETRYCONNECT1) {
        	setState(StateFlags.STATE_RETRYCONNECTING);
        }
        else if (mState == StateFlags.STATE_RETRYCONNECT2){
        	setState(StateFlags.STATE_RETRYCONNECTING2);
        }
        else {
        setState(StateFlags.STATE_CONNECTING);
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
        	Log.d(TAG, "ConnectThread instantiated");
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            if(D)
            	Log.d(TAG,"mmSocket initialized");
            mmSocket = tmp;
        }
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
            	Log.d(TAG, "Tried making socket");
                mmSocket.connect();
            } catch (IOException e) {
            	if (mState == StateFlags.STATE_RETRYCONNECTING) {
            		connectionLost();
            	}
            	else {
                connectionFailed();
            	}
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                PairedBTDevices.this.start();
                return;
                //getBluetoothService()
            }
            // Reset the ConnectThread because we're done
            synchronized (this) {
                mConnectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    
    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */

    
    
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Cancel the accept thread because we only want to connect to one device
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // Send the name of the connected device back to the UI Activity
        //MessageFlags msgFlags = MessageFlags.MESSAGE_DEVICE_NAME;
        btDeviceHandler.onConnect(device.getName());
//        Message msg = mHandler.obtainMessage(msgFlags.getValue());
//        Bundle bundle = new Bundle();
//        bundle.putString(BluetoothService.DEVICE_NAME, device.getName());
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
        setState(StateFlags.STATE_CONNECTED);
    }
    
    
    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        
        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            //byte[] buffer = new byte[1024];
            //int bytes;
            // Keep listening to the InputStream while connected
            while (true) {
            	ObjectInputStream ois;
                try {
                   
                    ois = new ObjectInputStream(mmInStream);
                	// Read from the InputStream
                    BTSendableInterface<?> myObj = (BTSendableInterface<?>) ois.readObject();
                    
                    btDeviceHandler.onReceive(myObj);
                    //bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI Activity
                    
                } catch (OptionalDataException e) {
					
				} catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                } catch (ClassNotFoundException e) {
					btDeviceHandler.onFailure(e);
				} finally {
					
				}
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        private void write(BTSendableInterface<?> o) {
            try {
            	ObjectOutputStream oos = new ObjectOutputStream(mmOutStream);
            	oos.writeObject(o);
                //mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                //mHandler.obtainMessage(msgFlags.getValue(), -1, -1, buffer)
                //        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(BTSendableInterface<?> o) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != StateFlags.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(o);
    }    
        
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(StateFlags.STATE_LISTEN);
        btDeviceHandler.onFailure(new Exception()); // TODO: fix
    }
    
    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
    	if (mState != StateFlags.STATE_RETRYCONNECTING){
	        setState(StateFlags.STATE_RETRYCONNECT1);
    	}
    	else{
    		setState(StateFlags.STATE_RETRYCONNECT2);
    	}
    	this.connect();
        // TODO: Big fix
    }
    
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        setState(StateFlags.STATE_NONE);
    }
    


}
