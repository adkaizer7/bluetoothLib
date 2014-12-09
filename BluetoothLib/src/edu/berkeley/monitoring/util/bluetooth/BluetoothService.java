/**
 * 
 */
package edu.berkeley.monitoring.util.bluetooth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

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
//import com.example.bluetoothsample.BluetoothChat;

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
	STATE_NONE, 
	STATE_RETRYCONNECTING,
	STATE_RETRYCONNECT1,
	STATE_RETRYCONNECT2, 
	STATE_RETRYCONNECTING2
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
	public BluetoothAdapter mBluetoothAdapter;
    private AcceptThread mAcceptThread;
    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    
    private final int REQUEST_ENABLE_BT = 2;
    //TODO     
    MessageFlags msgFlags;
  
    ArrayList<PairedBTDevices> pairedDevicesList;
    
    //Debugging purposes
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;
    private StateFlags mState;
    public static final UUID MY_UUID = UUID.fromString("e302e180-7efd-11e4-80c6-0002a5d5c51b");
    
    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothDevice";
    
    //Thread to start connection
    //Message handler
    private final Handler mHandler;

    //Context of parent application.
    private Activity parentActivity;
	    
    private BluetoothInterface bluetoothInterface;

    
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */    
    
	public BluetoothService(Activity parAct, Handler handler, BluetoothInterface bleInt)  throws BluetoothExceptions{
		if(D) Log.e(TAG, "++ BluetoothService Constructor Invoked ++");
        parentActivity = parAct;
        mHandler = handler;
        bluetoothInterface = bleInt;
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported, return error
        if (mBluetoothAdapter == null) {
        	throw new BluetoothExceptions(BluetoothExceptions.CANNOT_TURN_ON_BLUETOOTH);
        }
        else{

        	// Register for broadcasts when a device is discovered
        	IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        	parentActivity.registerReceiver(mReceiver, filter);

        	// Register for broadcasts when discovery has finished
        	filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        	parentActivity.registerReceiver(mReceiver, filter);  
        	
        	// Register for broadcasts when pairing is initiated
    		IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    		parentActivity.registerReceiver(mPairReceiver, intent);
    		
    		pairedDevicesList = new ArrayList<PairedBTDevices>();
            Set<BluetoothDevice> pairedBTDevices = mBluetoothAdapter.getBondedDevices();
            PairedBTDevices pairedDevice;
    		 
            //If there are paired devices, add each one to the ArrayList
            if (pairedBTDevices.size() > 0) {
                for (BluetoothDevice device : pairedBTDevices) {
                	pairedDevice = new PairedBTDevices(device);
                	pairedDevicesList.add(pairedDevice);               	
                	//pairedDevice.start();
                }    
                
            }
            //TODO:
            //return ((int)pairedBTDevices.size());
            //or
        }        
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
                if (mState == StateFlags.STATE_LISTEN){
	            	try {
	                    // This is a blocking call and will only return on a
	                    // successful connection or an exception
	                    socket = mmServerSocket.accept();
	                    mState = StateFlags.STATE_CONNECTING;
	                } catch (IOException e) {
	                    Log.e(TAG, "accept() failed", e);
	                    break;
	                }
                }
                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {                        
                        case STATE_LISTEN:
                        	break;
                        case STATE_CONNECTING:
                          BluetoothDevice requester = socket.getRemoteDevice();
          	        	  for (int i = 0; i < pairedDevicesList.size(); i++){
        	        		  String devAddress = pairedDevicesList.get(i).getAddress();
        	        		  if (devAddress.equals(requester.getAddress())){
        	        			  setState(StateFlags.STATE_CONNECTED);
        	        			  PairedBTDevices pairedDevice = pairedDevicesList.get(i);
        	        			  bluetoothInterface.onIncomingConnection(pairedDevice);
        	        			  pairedDevice.connected(socket, socket.getRemoteDevice());
        	        			  break;
        	        		  }
                          /*for (PairedBTDevices pairedDevice : pairedDevicesList){
                        	  if (requester.getAddress().equals(pairedDevice.getAddress())){
                        		  setState(StateFlags.STATE_CONNECTED);
                        		  pairedDevice.connected(socket, socket.getRemoteDevice());
                        	  }
          	        	  }*/
          	        	  }
          	        	break;
                        case STATE_NONE:
                        	break;
                        case STATE_NOT_CONNECTED:
                        	break;
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try {
                                socket.close();
                                setState(StateFlags.STATE_LISTEN);
                            } catch (IOException e) {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
						default:
							break;
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
	 * Turn on Bluetooth
	 */
	public void switchOnBluetooth() {
		
        if (D) Log.d(TAG,"switchOnBluetooth()1");
        
		if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			parentActivity.startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
		else{
			msgFlags = MessageFlags.MESSAGE_TOAST;
            Message msg = mHandler.obtainMessage(msgFlags.getValue());
            Bundle bundle = new Bundle();    
            bundle.putString(BluetoothService.TOAST, "Bluetooth already on");
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
			
	}//TODO : throw error
	
	/**
	 * Begin accepting connections from the environment.
	 */
	public void beginAccept(){
    	// start listener for incoming connections
    	if (mAcceptThread == null) {
    		setState(StateFlags.STATE_LISTEN);
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
	}
	
	/**
	 * End accepting connections from the environment.
	 */
	public void endAccept(AcceptThread mAcceptThread){
		if (mAcceptThread != null){
			setState(StateFlags.STATE_NONE);
			mAcceptThread.cancel();
		}
	}

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
        	MessageFlags msgFlags = MessageFlags.MESSAGE_TOAST;
            Message msg = mHandler.obtainMessage(msgFlags.getValue());
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
	

	
	public void getPairedDevices() {
 
        
        //If there are paired devices, add each one to the ArrayList
        if (pairedDevicesList.size() > 0) {
            for (PairedBTDevices pairedDevice : pairedDevicesList) {
            	//TODO
            	bluetoothInterface.onObtainedOnePairedDevices(pairedDevice);
            }    		
        } 
        //TODO:
        //return ((int)pairedBTDevices.size());
        //or
        bluetoothInterface.onFinishedObtainingPairedDevices();
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
       
	public PairedBTDevices getPairedDeviceFromAddress(String address){
			PairedBTDevices retDevice = null; 
	        //If there are paired devices, add each one to the ArrayList
	        if (pairedDevicesList.size() > 0) {
	            /*for (PairedBTDevices pairedDevice : pairedDevicesList) {
	            	String devAddress = pairedDevice.getAddress();
	            	if (devAddress.equals(address)){
	            		return pairedDevice;
	            	}*/
	        	  int i = 0;
	        	  for (i = 0; i < pairedDevicesList.size(); i++){
	        		  String devAddress = pairedDevicesList.get(i).getAddress();
	        		  if (devAddress.equals(address)){
	        			  retDevice = pairedDevicesList.get(i);
	        			  return retDevice;
	        		  }
	        	  }
	        }
	        return retDevice;
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
                											 device.getAddress(),device,mHandler);
                	//listUnpairedBTDevice.add(unpairedBTDevice);
                	bluetoothInterface.onObtainedOneUnpairedDevices(unpairedBTDevice);
                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            	if (D)
            		Log.e(TAG,"Finished Scanning from library");
            	bluetoothInterface.onFinishedScanning();
            }
        }
    };
    
    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {	        	
	        	 final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
	        	 final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
	        	 MessageFlags msgFlags = MessageFlags.MESSAGE_TOAST;
	        	 
	        	 if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
	        		 Message msg = mHandler.obtainMessage(msgFlags.getValue());
	                 Bundle bundle = new Bundle();
	                 bundle.putString(BluetoothService.TOAST, "Paired");
	                 msg.setData(bundle);
	                 mHandler.sendMessage(msg);
	        	 }
	        	 else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
	        		 Message msg = mHandler.obtainMessage(msgFlags.getValue());
	                 Bundle bundle = new Bundle();
	                 bundle.putString(BluetoothService.TOAST, "Paired");
	                 msg.setData(bundle);
	                 mHandler.sendMessage(msg);
	        	 }
	        }
	    }
	};
}
