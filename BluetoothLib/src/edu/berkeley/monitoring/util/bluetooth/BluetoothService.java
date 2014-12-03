/**
 * 
 */
package edu.berkeley.monitoring.util.bluetooth;

import java.util.ArrayList;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
	STATE_NONE
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
    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";    
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    private final int REQUEST_ENABLE_BT = 2;
    //TODO     
    MessageFlags msgFlags;
  
    
    //Debugging purposes
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;
    
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
	

	
	public void getPairedDevices(ArrayList<PairedBTDevices> pairedDevicesList) {
 
        Set<BluetoothDevice> pairedBTDevices = mBluetoothAdapter.getBondedDevices();
        PairedBTDevices pairedDevice;
 
        //If there are paired devices, add each one to the ArrayList
        if (pairedBTDevices.size() > 0) {
            for (BluetoothDevice device : pairedBTDevices) {
            	pairedDevice = new PairedBTDevices(device);
            	pairedDevicesList.add(pairedDevice);
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
