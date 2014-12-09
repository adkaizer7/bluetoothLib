package edu.berkeley.sampleapps.bluetoothsampleapp;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import edu.berkeley.monitoring.util.bluetooth.BTDeviceHandlerInterface;
import edu.berkeley.monitoring.util.bluetooth.BTSendable;
import edu.berkeley.monitoring.util.bluetooth.BTSendableInterface;
import edu.berkeley.monitoring.util.bluetooth.BluetoothExceptions;
import edu.berkeley.monitoring.util.bluetooth.BluetoothInterface;
import edu.berkeley.monitoring.util.bluetooth.BluetoothService;
import edu.berkeley.monitoring.util.bluetooth.MessageFlags;
import edu.berkeley.monitoring.util.bluetooth.PairedBTDevices;
import edu.berkeley.monitoring.util.bluetooth.UnpairedBTDevices;

public class MainActivityBluetoothSampleApp extends Activity implements BluetoothInterface, BTDeviceHandlerInterface{
    private static final boolean D = true;
    private static final String TAG = "BluetoothChat";
    public static BluetoothService bluetoothServiceHandler;
    private Button mTurnOnBluetooth;
    private Button mStartScan;
    private Button mMakeDiscoverable;
    private Button mEnlistPairedDevices;
    private Button mBeginAccept;
    private Activity thisActivity = this;
    public static ArrayList<PairedBTDevices> listPairedDevices;
    public static ArrayList<UnpairedBTDevices> listUnpairedDevices;
    public static final String TOAST = "toast";
    private static final int REQUEST_PAIR_DEVICE = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    PairedBTDevices pairedDevice;
    


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bluetooth_sample_app);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        // Set up the window layout
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.activity_main_bluetooth_sample_app);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        // Set up the custom title
        //mTitle = (TextView) findViewById(R.id.title_left_text);
        //mTitle.setText(R.string.app_name);
        //mTitle = (TextView) findViewById(R.id.title_right_text);
        //mTextFile = (TextView)findViewById(R.id.textfile);
        // Get local Bluetooth adapter
    
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
        try{

        	bluetoothServiceHandler = new BluetoothService(this, msgHandler, this);
        	
        }
        catch(BluetoothExceptions e){
        	//TODO
        }
        setupChat();
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");
        
        listPairedDevices = new ArrayList<PairedBTDevices>();
        listUnpairedDevices = new ArrayList<UnpairedBTDevices>();
        
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);
        //Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.editTextOut);
        //mOutEditText.setOnEditorActionListener(mWriteListener);
        // Initialize the send button with a listener that for click events
        mTurnOnBluetooth = (Button) findViewById(R.id.buttonTurnOnBT);
        mTurnOnBluetooth.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	bluetoothServiceHandler.switchOnBluetooth();
            }
        });
        
        mSendButton = (Button) findViewById(R.id.buttonSend);
        mSendButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		//TODO: the button should be hidden if we haven't yet paired to a device.
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.editTextOut);
                String message = view.getText().toString();
        		BTSendable<String> msg2Send = new BTSendable<String>(message);        		
        		pairedDevice.write(msg2Send);
        	}
        	
        });
        
        mStartScan = (Button) findViewById(R.id.buttonStartScan);
        mStartScan.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		bluetoothServiceHandler.startScan();
        	}
        	
        });
        
        mMakeDiscoverable = (Button) findViewById(R.id.buttonMakeDiscoverable);
        mMakeDiscoverable.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		bluetoothServiceHandler.ensureDiscoverable();
        	}
        	
        });
        
        mEnlistPairedDevices = (Button) findViewById(R.id.buttonEnlistPairedDevices);
        mEnlistPairedDevices.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		bluetoothServiceHandler.getPairedDevices();
        	}
        	
        });
        
        mBeginAccept = (Button) findViewById(R.id.buttonBeginAccept);
        mBeginAccept.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		bluetoothServiceHandler.beginAccept();
        	}
        });
        
        /**
         * mConnect2Device = (Button) findViewById(R.id.button_connect2device);
         
        mConnect2Device.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	fConnect2Device();
            }
        });
        mPickAFile = (Button) findViewById(R.id.button_pickAFile);
        mPickAFile.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            	intent.setType("file/*");
            	startActivityForResult(intent,PICKFILE_RESULT_CODE);
            	    
            	   }});

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");*/
    }
   
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_bluetooth_sample_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	public void onFinishedScanning() {
		if (D)
			Log.e(TAG,"Callback from library");
        Intent serverIntent = new Intent(thisActivity, ListUnpairedDevices.class);
        this.startActivityForResult(serverIntent, REQUEST_PAIR_DEVICE);     		

		
	}

	@Override
	public void onObtainedOneUnpairedDevices(UnpairedBTDevices pUnpairedBTDevice) {
		listUnpairedDevices.add(pUnpairedBTDevice);		
	}

	@Override
	public void onFinishedObtainingPairedDevices() {
        if (D)
        	Log.e(TAG,"onFinishedObtainingPairedDevices");
        Intent serverIntent = new Intent(thisActivity, ListPairedDevices.class);
        this.startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE);      		
		
	}

	@Override
	public void onFinishObtainingUnpairedDevices() {
		// TODO Auto-generated method stub
		
	}
	
	public void onBeginAccept(){
		
	}
	
	public void onIncomingConnection(PairedBTDevices pairedDevice){
        pairedDevice.registerHandler(this);
	}
	
    // The Handler that gets information back from the BluetoothChatService
    private final Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	MessageFlags msgFlags = MessageFlags.values()[msg.what];
            switch (msgFlags) {
/**            case BluetoothService.MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                    mTitle.setText(R.string.title_connected_to);
                    mTitle.append(mConnectedDeviceName);
                    mConversationArrayAdapter.clear();
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    mTitle.setText(R.string.title_connecting);
                    break;
                case BluetoothChatService.STATE_LISTEN:
                case BluetoothChatService.STATE_NONE:
                    mTitle.setText(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;*/
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
			default:
				break;
            }
        }
    };
	@Override
	public void onSwitchingonBluetooth() {
		// TODO Auto-generated method stub
		
	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_PAIR_DEVICE:
            // When ListUnpairedDevices returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                // Get the device address
                int address = data.getExtras()
                                     .getInt(ListUnpairedDevices.UNPAIRED_DEVICE_INDEX);
                // Get the BLuetoothDevice object
                UnpairedBTDevices device = listUnpairedDevices.get(address);
                // Attempt to connect to the device
                PairedBTDevices pairedBTDevice = device.pairToDevice(device);
                
            }
            break;
        case REQUEST_CONNECT_DEVICE:
        {
            if (resultCode == Activity.RESULT_OK) {
                // Get the device address
                String address = data.getExtras()
                                     .getString(ListPairedDevices.SELECTED_PAIRED_DEVICE);
                // Attempt to connect to the device
                PairedBTDevices pairedBTDevice = bluetoothServiceHandler.getPairedDeviceFromAddress(address);
                pairedBTDevice.registerHandler(this);
                pairedBTDevice.connect();
                
            }

        	
        }break;
//        case REQUEST_ENABLE_BT:
//            // When the request to enable Bluetooth returns
//            if (resultCode == Activity.RESULT_OK) {
//                // Bluetooth is now enabled, so set up a chat session
//                setupChat();
//            } else {
//                // User did not enable Bluetooth or an error occured
//                Log.d(TAG, "BT not enabled");
//                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
//                finish();
//            }
//            break;
//        case PICKFILE_RESULT_CODE:
//        	   if(resultCode== Activity.RESULT_OK){
//        	    String FilePath = data.getData().getPath();
//            	if (D)
//            		Log.e(TAG, "Printing File Path");
//        	    mTextFile.setText(FilePath);
//        	    sendFile(FilePath);
//        	   }
//        	   break;
        }
    }
	@Override
	public void onObtainedOnePairedDevices(
			PairedBTDevices pairedBTDevice) {
		// TODO Auto-generated method stub
		listPairedDevices.add(pairedBTDevice);		
	}
	
	public void onReceive(BTSendableInterface<?> o){
		
	}
	public void onFailure(Exception e){
		
	}
	public void onConnect(String name){
		String a = new String();
		a = name;
	}
	
	public void onFailure(){
		String a = new String();
		a = "name";
//		Toast.makeText(getApplicationContext(), "Cannot connect to device", Toast.LENGTH_SHORT).show();
	}
}


