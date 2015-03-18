/*-------------------------------------------------------------------------
	FILE		: 	MainActivity.java
	DESCRIPTION	:	This is the main activity of the application.
					This activity will listen to a broadcast service and 
					display all beacons provided by this service.
					It will also display in which bus you are.
	AUTHORS		:	Da Silva Andrade David
-------------------------------------------------------------------------*/
package com.hepia.BusLocation;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Utils;

public class MainActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 1000; // Activation bluetooth
	private Intent intent;

	ArrayList<Bus> allBuses;

	private LeDeviceListAdapter adapter;

	TextView tvBusID;

	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		allBuses = new ArrayList<Bus>();

		ArrayList<Beacon> bus1BeaconsFrontToBack = new ArrayList<Beacon>();
		bus1BeaconsFrontToBack.add(new Beacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", "Beacon1", "FC:56:89:42:FB:FF", 11111, 1, -68, -1));
		bus1BeaconsFrontToBack.add(new Beacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", "Beacon2", "D7:2E:74:79:1B:6B", 11111, 2, -68, -1));
		Bus bus1 = new Bus("10", "Genève", "1", bus1BeaconsFrontToBack, 10);

		ArrayList<Beacon> bus2BeaconsFrontToBack = new ArrayList<Beacon>();
		bus2BeaconsFrontToBack.add(new Beacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", "Beacon3", "F7:52:5C:F7:7E:E1", 11111, 3, -68, -1));
		bus2BeaconsFrontToBack.add(new Beacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", "Beacon4", "FA:A2:99:BF:A4:78", 11111, 4, -68, -1));
		Bus bus2 = new Bus("10", "Genève", "2", bus2BeaconsFrontToBack, 10);

		ArrayList<Beacon> bus3BeaconsFrontToBack = new ArrayList<Beacon>();
		bus3BeaconsFrontToBack.add(new Beacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", "Beacon5", "F7:52:5C:F7:7E:E1", 11111, 5, -68, -1));
		bus3BeaconsFrontToBack.add(new Beacon("b9407f30-f5f8-466e-aff9-25556b57fe6d", "Beacon6", "FA:A2:99:BF:A4:78", 11111, 6, -68, -1));
		Bus bus3 = new Bus("10", "Genève", "3", bus3BeaconsFrontToBack, 10);

		allBuses.add(bus1);
		allBuses.add(bus2);
		allBuses.add(bus3);

		intent = new Intent(this, BroadcastService.class);

		// Configure device list.
		adapter = new LeDeviceListAdapter(this);
		ListView list = (ListView) findViewById(R.id.device_list);
		list.setAdapter(adapter);

		// Get textview
		tvBusID = (TextView) findViewById(R.id.text_bus_id);

		findViewById(R.id.bStartScan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BeaconManager beaconManager = new BeaconManager(MainActivity.this);
				// Check if device supports Bluetooth Low Energy.
				if (!beaconManager.hasBluetooth()) {
					Toast.makeText(MainActivity.this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
					return;
				}

				// If Bluetooth is not enabled, let user enable it.
				if (!beaconManager.isBluetoothEnabled()) {
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				} else {
					getActionBar().setSubtitle("Scanning...");
					tvBusID.setText("Scan in progress...");
					findViewById(R.id.bStartScan).setEnabled(false);
					findViewById(R.id.bStopScan).setEnabled(true);

					// Start beacon capture service
					startService(intent);
					registerReceiver(broadcastReceiver, new IntentFilter(BroadcastService.BROADCAST_ACTION));

				}
			}
		});

		findViewById(R.id.bStopScan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				adapter.clear();
				getActionBar().setSubtitle("Service is not running");
				tvBusID.setText("Please start the service");
				findViewById(R.id.bStartScan).setEnabled(true);
				findViewById(R.id.bStopScan).setEnabled(false);
				unregisterReceiver(broadcastReceiver);
				stopService(intent);
			}
		});
	}

	// Reponse from bluetooth activation
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// If it's the Bluetooth activation
		if (requestCode == REQUEST_ENABLE_BT) {
			if (resultCode == Activity.RESULT_OK) {
				getActionBar().setSubtitle("Scanning...");
				setProgressBarIndeterminateVisibility(true);
				findViewById(R.id.bStartScan).setEnabled(false);
				findViewById(R.id.bStopScan).setEnabled(true);
			} else {
				Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			receiveDataFromService(intent);
		}
	};

	public void onResume() {
		super.onResume();
	}

	public void onPause() {
		super.onPause();
		// unregisterReceiver(broadcastReceiver);
		// stopService(intent);
	}

	public void onDestroy() {
		unregisterReceiver(broadcastReceiver);
		stopService(intent);
		super.onDestroy();
	}

	private void receiveDataFromService(Intent intent) {

		// verify if the message we received is the one that we want
		if (intent.getAction().equals(BroadcastService.BROADCAST_ACTION)) {
			// get the arrayList we have sent with intent (the one from BroadcastSender)
			ArrayList<Beacon> beacons = intent.getParcelableArrayListExtra("beacons");

			String counter = intent.getStringExtra("counter");
			getActionBar().setSubtitle("Actualization from service : " + counter);

			List<Beacon> list = beacons;
			// Ajoute les beacons à l'affichage
			if (beacons != null) {
				adapter.replaceWith(list);

				ArrayList<Bus> busesDetected = new ArrayList<Bus>();

				// First sorting, on save the bus that are detected with their respective beacons
				for (Bus bus : allBuses) {
					if (bus.isThisBusDetected(beacons))
						busesDetected.add(bus);
				}

				if (busesDetected.size() != 0) {
					// Find in which bus you are
					Bus nearestBus = busesDetected.get(0);
					for (Bus testBus : busesDetected) {
						ArrayList<Beacon> testBusBeacons = testBus.getLastBeaconsDetected();
						ArrayList<Beacon> nearBusBeacons = nearestBus.getLastBeaconsDetected();
						if((Utils.computeAccuracy(testBusBeacons.get(0))+Utils.computeAccuracy(testBusBeacons.get(1))) < 
								(Utils.computeAccuracy(nearBusBeacons.get(0))+Utils.computeAccuracy(nearBusBeacons.get(1)))){
							nearestBus = testBus;
						}
					}
					
					tvBusID.setText("You are inside the bus number "+ nearestBus.getID());
					
				} else {
					tvBusID.setText("You are not in a bus.");
				}

			} else {
				tvBusID.setText("You are not in a bus.");
			}
		}
	}
}