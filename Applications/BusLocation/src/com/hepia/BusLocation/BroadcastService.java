/*-------------------------------------------------------------------------
	FILE		: 	BroadcastService.java
	DESCRIPTION	:	Implmentation of a service to apply a median filter
					to the data coming from Estimote iBeacons service.
	AUTHORS		:	Da Silva Andrade David
-------------------------------------------------------------------------*/

package com.hepia.BusLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

public class BroadcastService extends Service {
	private static final String TAG = "BroadcastService";
	private static final int WINDOW_SIZE = 29;
	public static final String BROADCAST_ACTION = "com.hepia.BusLocation.displayevent";
	private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
	private final Handler handler = new Handler();
	Intent intent;

	int nbValues = 0;
	int counter = 0;
	Queue<List<Beacon>> buffer;
	ArrayList<Beacon> broadcastList;

	private BeaconManager beaconManager;

	@Override
	public void onCreate() {
		super.onCreate();
		intent = new Intent(BROADCAST_ACTION);
	}

	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Method called when the service is started
		PARAMS		: 	(Intent) intent
						(Metafile) metafile
						(int) startId
		RETURN		: 	None
	-------------------------------------------------------------------------*/
	public void onStart(Intent intent, int startId) {
		handler.removeCallbacks(sendUpdatesToUI);

		buffer = new LinkedList<List<Beacon>>();

		handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

		Log.d(TAG, "Service started");
		// Configure BeaconManager.
		beaconManager = new BeaconManager(BroadcastService.this);
		beaconManager.setRangingListener(new BeaconManager.RangingListener() {
			@Override
			public void onBeaconsDiscovered(Region region, final List<Beacon> beacons) {
				if (nbValues < WINDOW_SIZE) {
					nbValues++;
					buffer.add(beacons);
				} else {
					buffer.add(beacons); // Add latest beacons
					buffer.remove(); // Remove oldests beacons

					// Create a dictionary
					Map<String, List<Beacon>> allBeacons = new HashMap<String, List<Beacon>>();
					for (List<Beacon> listBeacs : buffer) {
						for (Beacon beac : listBeacs) {
							String key = beac.getProximityUUID() + "_" + beac.getMajor() + "_" + beac.getMinor();
							// One key with multiple values, in this case multiple beacons
							// If the already exist
							if (allBeacons.containsKey(key))
								allBeacons.get(key).add(beac);
							else { // Else, create the entry
								List<Beacon> tmpList = new ArrayList<Beacon>();
								tmpList.add(beac);
								allBeacons.put(key, tmpList);
							}
						}
					}
					broadcastList = new ArrayList<Beacon>();
					for (String key : allBeacons.keySet()) {
						// Order list
						List<Beacon> orderedList = quickSortByRSSI(allBeacons.get(key));
						// Get the median
						broadcastList.add(orderedList.get((int) (orderedList.size() / 2)));
					}
				}
			}
		});

		connectToService();
	}

	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Quicksort list by RSSI value
		PARAMS		: 	(List<Beacon>) input
		RETURN		: 	(List<Beacon>) input value sorted by RSSI
	-------------------------------------------------------------------------*/
	private List<Beacon> quickSortByRSSI(List<Beacon> input) {

		if (input.size() <= 1) {
			return input;
		}

		int middle = (int) Math.ceil((double) input.size() / 2);
		Beacon pivot = input.get(middle);

		List<Beacon> less = new ArrayList<Beacon>();
		List<Beacon> greater = new ArrayList<Beacon>();

		for (int i = 0; i < input.size(); i++) {
			if (input.get(i).getRssi() <= pivot.getRssi()) {
				if (i == middle) {
					continue;
				}
				less.add(input.get(i));
			} else {
				greater.add(input.get(i));
			}
		}

		return concatenate(quickSortByRSSI(less), pivot, quickSortByRSSI(greater));
	}

	private List<Beacon> concatenate(List<Beacon> less, Beacon pivot, List<Beacon> greater) {

		List<Beacon> list = new ArrayList<Beacon>();

		for (int i = 0; i < less.size(); i++) {
			list.add(less.get(i));
		}

		list.add(pivot);

		for (int i = 0; i < greater.size(); i++) {
			list.add(greater.get(i));
		}

		return list;
	}

	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Broadcast method that is called every second to send
						data in broadcast mode
		PARAMS		: 	none
		RETURN		: 	none
	-------------------------------------------------------------------------*/
	private Runnable sendUpdatesToUI = new Runnable() {
		public void run() {
			if (buffer.size() >= WINDOW_SIZE) {
				intent.putExtra("counter", String.valueOf(++counter));
				intent.putParcelableArrayListExtra("beacons", broadcastList);
				sendBroadcast(intent);
			}
			handler.postDelayed(this, 1000); // Recall sendUpdatesToUI after 1 seconds
		}
	};
	
	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Connection to Estimote service
		PARAMS		: 	none
		RETURN		: 	none
	-------------------------------------------------------------------------*/
	private void connectToService() {
		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				try {
					beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
				} catch (RemoteException e) {
					Log.e(TAG, "Cannot start ranging", e);
				}
			}
		});
	}
	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Disconnect from Estimote service
		PARAMS		: 	none
		RETURN		: 	none
	-------------------------------------------------------------------------*/
	public void disconnectFromService() {
		try {
			beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
		} catch (RemoteException e) {
			Log.e(TAG, "Cannot stop ranging", e);
		}
	}

	/*-------------------------------------------------------------------------
		DESCRIPTION	:	When a new client is bind to the service
		PARAMS		: 	(Intent) intent
		RETURN		: 	none
	-------------------------------------------------------------------------*/
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	/*-------------------------------------------------------------------------
		DESCRIPTION	:	When the service is stopped
		PARAMS		: 	none
		RETURN		: 	none
	-------------------------------------------------------------------------*/
	public void onStop() {
		disconnectFromService();
		beaconManager.disconnect();
	}
	
	/*-------------------------------------------------------------------------
		DESCRIPTION	:	When the service is destroyed
		PARAMS		: 	none
		RETURN		: 	none
	-------------------------------------------------------------------------*/
	public void onDestroy() {
		handler.removeCallbacks(sendUpdatesToUI);
		try {
			beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
		} catch (RemoteException e) {
			Log.d(TAG, "Error while stopping ranging", e);
		}
		beaconManager.disconnect();
		super.onDestroy();
	}

}
