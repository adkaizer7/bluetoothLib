package edu.berkeley.sampleapps.bluetoothsampleapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import edu.berkeley.monitoring.util.bluetooth.UnpairedBTDevices;

public class ListUnpairedDevices extends Activity {

	private ArrayAdapter<String> mUnpairedDevicesArrayAdapter;
	
    private static final boolean D = true;
    private static final String TAG = "ListUnpairedDevices";
    public static String UNPAIRED_DEVICE_INDEX = "device_index";


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (D)
			Log.e(TAG,"++OnCreate++");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_unpaired_devices);
		
        setResult(Activity.RESULT_CANCELED);
        
        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mUnpairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView unpairedListView = (ListView) findViewById(R.id.listViewUnpairedBTdevices);
        unpairedListView.setAdapter(mUnpairedDevicesArrayAdapter);
        unpairedListView.setOnItemClickListener(mDeviceClickListener);
        // Get a set of currently paired devices
        // If there are paired devices, add each one to the ArrayAdapter
        if (MainActivityBluetoothSampleApp.listUnpairedDevices.size() > 0) {
            //findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (UnpairedBTDevices device : MainActivityBluetoothSampleApp.listUnpairedDevices) {
                mUnpairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
        	if (D)
        		Log.e(TAG,"No Unpaired Devices");
            String noDevices = getResources().getText(R.string.noUnpairedDevices).toString();
            mUnpairedDevicesArrayAdapter.add(noDevices);
        }
        

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_unpaired_devices, menu);
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
	
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            int address = arg2;
            
            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(UNPAIRED_DEVICE_INDEX, address);
            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };
	
}
