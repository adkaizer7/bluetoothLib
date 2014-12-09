package edu.berkeley.sampleapps.bluetoothsampleapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import edu.berkeley.monitoring.util.bluetooth.PairedBTDevices;

public class ListPairedDevices extends Activity {

	private ArrayAdapter<String> mPairedDevicesArrayAdapter;
	
    private static final boolean D = true;
    private static final String TAG = "ListPairedDevices";
    public static final String SELECTED_PAIRED_DEVICE = "Selected Device";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(D)
			Log.e(TAG, "+++ ON CREATE +++");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_paired_devices);
		// Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);
        
        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView pairedListView = (ListView) findViewById(R.id.listViewPairedBTdevices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        // Get a set of currently paired devices
        // If there are paired devices, add each one to the ArrayAdapter
        if (MainActivityBluetoothSampleApp.listPairedDevices.size() > 0) {
            //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (PairedBTDevices device : MainActivityBluetoothSampleApp.listPairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.noPairedDevices).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
        
	}
	
    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        @SuppressLint("NewApi") public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            //TODO
        	//mBtAdapter.cancelDiscovery();
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);           
        
    		Intent intent = new Intent();
    		intent.putExtra(SELECTED_PAIRED_DEVICE, address);
            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();   
            
        }
    };
	
	
	/**@Override
	public void onStart(){		
	}*/
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_paired_devices, menu);
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
}
