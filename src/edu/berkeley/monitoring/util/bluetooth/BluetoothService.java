/**
 * 
 */
package edu.berkeley.monitoring.util.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

//import com.example.bluetoothsample.BluetoothChat;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * @author  Adarsh
 * 			Phil
 *
 */
enum StateFlags{
	STATE_NOT_CONNECTED,
	STATE_CONNECTING,
	STATE_CONNECTED,
	STATE_LISTEN,
	STATE_NONE
}

enum MessageFlags{
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

//TODO
/**
 * enum BluetoothFlags{

	REQUEST_ENABLE_BT(0);
	
	private int value;
	
	BluetoothFlags(int value){
		this.value = value;
	}
	
	public int getValue(){
		return this.value;
	}
	
} */

public class BluetoothService{

	/* (non-Javadoc)
	 * @see edu.berkeley.monitoring.util.bluetoothservices.Bluetooth#initializeAdapter()
	 */
	private BluetoothAdapter mBluetoothAdapter;
	
    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothChat";
    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    
    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";    
	
    
    public static final int STATE_NONE = 14;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 9;
    public static final int MESSAGE_READ = 10;
    public static final int MESSAGE_WRITE =11;
    public static final int MESSAGE_DEVICE_NAME = 12;
    public static final int MESSAGE_TOAST = 13;
    //TODO     
    MessageFlags msgFlags;
    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    private final int REQUEST_ENABLE_BT = 2;
    
    //Current state of the connection
    private StateFlags mState;
    
    //Debugging purposes
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;
    
    //Thread to accept connections
    private AcceptThread mAcceptThread;
    //Thread to start connection
    private ConnectThread mConnectThread;
    //Thread running communication.
    private ConnectedThread mConnectedThread;    
    
    //Message handler
    private final Handler mHandler;

    //Context of parent application.
    private Activity parentActivity;
	
    //Since we haven't paired with these devices yet, we only know their name 
    //and not other details
    private ArrayList<UnpairedBTDevices> listOfUnpairedDevices;
    
    private BluetoothInterface bluetoothInterface; 
    
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */    
    
	public BluetoothService(Activity parAct, Handler handler, BluetoothInterface bleInt)  throws BluetoothExceptions{

        parentActivity = parAct;
        mHandler = handler;
        mState = StateFlags.STATE_NONE;
        bluetoothInterface = bleInt;
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported, return error
        if (mBluetoothAdapter == null) {
        	throw new BluetoothExceptions(BluetoothExceptions.CANNOT_TURN_ON_BLUETOOTH);
        }
        else{
        	//Allocate memory for listOfUnpairedDevices and listOfPairedDevices;
        	listOfUnpairedDevices = new ArrayList<UnpairedBTDevices>(); 

        	// Register for broadcasts when a device is discovered
        	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        	parentActivity.registerReceiver(mReceiver, filter);

        	// Register for broadcasts when discovery has finished
        	filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        	parentActivity.registerReceiver(mReceiver, filter);   
        	
        	mState = StateFlags.STATE_NONE;
        }        
	}

	/**
	 * Turn on Bluetooth
	 */
	public void switchOnBluetooth() {
		
        if (D) Log.d(TAG,"switchOnBluetooth()1");
        
		if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			parentActivity.startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
		else{
            Message msg = mHandler.obtainMessage(BluetoothService.MESSAGE_TOAST);
            Bundle bundle = new Bundle();    
            bundle.putString(BluetoothService.TOAST, "Bluetooth already on");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
			
	}//TODO : throw error
	

    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
/**        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                mChatService.connect(device);
            }
            break;*/
        case REQUEST_ENABLE_BT:{
            Message msg = mHandler.obtainMessage(BluetoothService.MESSAGE_TOAST);
            Bundle bundle = new Bundle();    
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Send a failure Acknowledgement message to the Activity
                bundle.putString(BluetoothService.TOAST, "Bluetooth Turned on");
            } else {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                // Send a failure message back to the Activity
                bundle.putString(BluetoothService.TOAST, "User declined switching on Bluetooth");
            }
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }break;
/**        case PICKFILE_RESULT_CODE:
        	   if(resultCode== Activity.RESULT_OK){
        	    String FilePath = data.getData().getPath();
            	if (D)
            		Log.e(TAG, "Printing File Path");
        	    mTextFile.setText(FilePath);
        	    sendFile(FilePath);
        	   }
        	   break;*/
        }
    }
	
	
	/**
	 * Start scanning for devices around you.
	 */
	public void startScan()
	{
        if (D) Log.d(TAG, "doDiscovery()");
        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
        	mBluetoothAdapter.cancelDiscovery();
        }
        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
	}//TODO: throw error
	

	/**Connect to a device
	 * 
	 * @param address : The MAC address of the device to be connected to
	 */
	public void connectToDevice(String address){
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        this.connect(device);
	}	
	
	/**
	 * Return the state of the bluetooth adapter
	 * @return mstate
	 */
	public StateFlags getState(){
		return mState;
	}
	
	/**
	 * Ensure device discoverability
	 */
    public void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensureDiscoverable()");
        //See if the device is not already in discovery mode
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            parentActivity.startActivity(discoverableIntent);
        }
    }

	/** (non-Javadoc)
	 * @see edu.berkeley.monitoring.util.bluetoothservices.Bluetooth#disableAdapter(android.bluetooth.BluetoothAdapter)
	 */
	public void disableAdapter() {
		mBluetoothAdapter = null;		
	}
	

	
	public int getPairedDevices(ArrayList<PairedBTDevices> pairedDevicesList) {
 
        Set<BluetoothDevice> pairedBTDevices = mBluetoothAdapter.getBondedDevices();
 
        //If there are paired devices, add each one to the ArrayList
        if (pairedBTDevices.size() > 0) {
            for (BluetoothDevice device : pairedBTDevices) {
            	pairedDevicesList.add(new PairedBTDevices(device));
            }    		
        } 
        //TODO:
        return ((int)pairedBTDevices.size());
        //or
        //bluetoothInterface.finishedObtainingPairedDevices(pairedDevicesList);
   }
	

	/**Remove device from paired List
	 * 
	 */
	public void unpairDevice(BluetoothHealthMonitoringDevice targetDevice) throws BluetoothExceptions {		
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice bluetoothDevice = targetDevice.getBlutoothDevice();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            //See if the list contains bluetooth device
        	if (!pairedDevices.contains(bluetoothDevice)){        	
        		throw new BluetoothExceptions(BluetoothExceptions.DEVICE_NOT_FOUND);
        	}
        	else{        	
        		try{
        			pairedDevices.remove(bluetoothDevice);
        		}
        		//TODO:
        		catch(UnsupportedOperationException e){
        			throw new BluetoothExceptions(BluetoothExceptions.NO_PAIRED_DEVICES);
        		}
        	}        		               
        }
        else{
        	throw new BluetoothExceptions(BluetoothExceptions.NO_PAIRED_DEVICES);
        }        	
	}

	//TODO
	public ArrayList<UnpairedBTDevices> getUnpairedDevices() {
		return listOfUnpairedDevices;
	}

    /**
     * Setting the state of the Bluetooth Service
     */    
    void setState(StateFlags state){
    	mState = state;
    	return;
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
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(StateFlags.STATE_LISTEN);
    }
       
    
    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }
        public void run() {
            if (D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;
            // Listen to the server socket if we're not connected
            while (mState != StateFlags.STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NOT_CONNECTED:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        default : break;                           
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END mAcceptThread");
        }
        public void cancel() {
            if (D) Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }
    
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);
        // Cancel any thread attempting to make a connection
        if (mState == StateFlags.STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(StateFlags.STATE_CONNECTING);
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
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
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
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                BluetoothService.this.start();
                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
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
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BluetoothService.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothService.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
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
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(BluetoothService.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        private void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(BluetoothService.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
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
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != StateFlags.STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }    
        
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(StateFlags.STATE_LISTEN);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothService.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothService.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    
    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(StateFlags.STATE_LISTEN);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BluetoothService.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BluetoothService.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(StateFlags.STATE_NONE);
    }
    
    /**
     * The BroadcastReceiver that listens for discovered devices and
     * changes the title when discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                	UnpairedBTDevices unpairedBTDevice = new UnpairedBTDevices(device.getName(),
                											 device.getAddress(),device);
                	listOfUnpairedDevices.add(unpairedBTDevice);
                	bluetoothInterface.onObtainedOneUnpairedDevices(device.getName() + "\n" + device.getAddress());
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	bluetoothInterface.onFinishedScanning();
            }
        }
    };   
}
