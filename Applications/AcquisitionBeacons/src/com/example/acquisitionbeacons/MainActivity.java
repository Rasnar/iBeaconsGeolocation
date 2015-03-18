/*-------------------------------------------------------------------------
	FILE		: 	MainActivity.java
	DESCRIPTION	:	This activity implement an acquisition of beacons.
					Each time a scan is done the Beacons are saves inside
					a file.
	AUTHORS		:	Da Silva Andrade David
-------------------------------------------------------------------------*/
package com.example.acquisitionbeacons;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	public static final String EXTRAS_TARGET_ACTIVITY = "extrasTargetActivity";
	public static final String EXTRAS_BEACON = "extrasBeacon";

	private static final int REQUEST_ENABLE_BT = 1000; // Activation bluetooth
	private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);

	private BeaconManager beaconManager;
	private LeDeviceListAdapter adapter;

	GPSTracker gps;
	SaveBeaconsOnFile saveBeacons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS); // Show loading icon
		setContentView(R.layout.activity_main);
		getActionBar().setDisplayHomeAsUpEnabled(false);

		// Disable android lock screen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Configure device list.
		adapter = new LeDeviceListAdapter(this);
		ListView list = (ListView) findViewById(R.id.device_list);
		list.setAdapter(adapter);

		// Configure verbose debug logging.
		L.enableDebugLogging(true);

		saveBeacons = new SaveBeaconsOnFile();

		gps = new GPSTracker(MainActivity.this);

		// Configure BeaconManager.
		beaconManager = new BeaconManager(MainActivity.this);
		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
			@Override
			public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
				// Note that results are not delivered on UI thread.
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						getActionBar().setSubtitle("Found beacons: " + beacons.size());
						adapter.replaceWith(beacons);
						saveBeacons.write(beacons, gps);
					}
				});
			}
		});

		findViewById(R.id.bStartScan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Check if device supports Bluetooth Low Energy.
				if (!beaconManager.hasBluetooth()) {
					Toast.makeText(MainActivity.this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
					return;
				}

				// check if GPS enabled
				if (gps.canGetLocation()) {
					gps.getLocation();
					Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + gps.getLatitude() + "\nLong: " + gps.getLongitude(),
							Toast.LENGTH_SHORT).show();
				} else {
					// can't get location
					// GPS or Network is not enabled
					// Ask user to enable GPS/network in settings
					gps.showSettingsAlert();
					gps.getLocation();
					Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + gps.getLatitude() + "\nLong: " + gps.getLongitude(),
							Toast.LENGTH_SHORT).show();

				}

				// If Bluetooth is not enabled, let user enable it.
				if (!beaconManager.isBluetoothEnabled()) {
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				} else {
					saveBeacons.startCapture();
					connectToService();
					findViewById(R.id.bStartScan).setEnabled(false);
					findViewById(R.id.bStopScan).setEnabled(true);
				}
			}
		});

		findViewById(R.id.bStopScan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				disconnectFromService();
				findViewById(R.id.bStartScan).setEnabled(true);
				findViewById(R.id.bStopScan).setEnabled(false);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		beaconManager.disconnect();

		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		try {
			beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
		} catch (RemoteException e) {
			Log.d(TAG, "Error while stopping ranging", e);
		}
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume(); // Always call the superclass method first

		// If scanning is enabled
		if (!findViewById(R.id.bStartScan).isEnabled())
			connectToService();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Si c'est l'activation du bluetooth
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				connectToService();
				saveBeacons.startCapture();
				findViewById(R.id.bStartScan).setEnabled(false);
				findViewById(R.id.bStopScan).setEnabled(true);
			} else {
				Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
				getActionBar().setSubtitle("Bluetooth not enabled");
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void connectToService() {
		getActionBar().setSubtitle("Scanning...");
		adapter.replaceWith(Collections.<Beacon> emptyList());
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
					setProgressBarIndeterminateVisibility(true);
				} catch (RemoteException e) {
					Toast.makeText(MainActivity.this, "Cannot start ranging, something terrible happened", Toast.LENGTH_LONG).show();
					Log.e(TAG, "Cannot start ranging", e);
				}
			}
		});
	}

	public void disconnectFromService() {
		getActionBar().setSubtitle("Scanning stopped");
		adapter.clear();
		try {
			beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
			setProgressBarIndeterminateVisibility(false); // Disable loading icon
		} catch (RemoteException e) {
			Toast.makeText(MainActivity.this, "Cannot stop ranging, something terrible happened", Toast.LENGTH_LONG).show();
			Log.e(TAG, "Cannot stop ranging", e);
		}
	}
}
