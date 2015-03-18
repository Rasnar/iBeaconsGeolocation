/*-------------------------------------------------------------------------
	FILE		: 	SaveBeaconsOnFile.java
	DESCRIPTION	:	This file implement a class to save beacons on a file.
	AUTHORS		:	Da Silva Andrade David
-------------------------------------------------------------------------*/
package com.example.acquisitionbeacons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

public class SaveBeaconsOnFile {

	private ArrayList<Beacon> beacons;
	long startTime;

	public SaveBeaconsOnFile() {
		beacons = new ArrayList<Beacon>();
	}

	public void startCapture() {
		startTime = System.currentTimeMillis();
	}

	public void write(Collection<Beacon> newBeacons, GPSTracker gps) {
		this.beacons.clear();
		this.beacons.addAll(newBeacons);
		
		// Refresh longitude and latitude from GPS
		gps.getLocation();

		File directory = new File(Environment.getExternalStorageDirectory() + File.separator + "BEACONS" + File.separator + new Date(startTime));
		// have the object build the directory structure, if needed.
		directory.mkdirs();
		for (Beacon beac : beacons) {
			File outputFile = new File(directory, "beacon_" + beac.getProximityUUID() + "_" + beac.getMajor() + "_" + beac.getMinor());

			try {
				double distance = Math.round(Utils.computeAccuracy(beac) * 100);
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)));
				out.println(beac.getMinor() + ";" + beac.getRssi() + ";" + Long.toString(System.currentTimeMillis() - startTime) + ";"
						+ distance / 100 + ";" + gps.getLatitude() + ";" + gps.getLongitude());
				out.close();
			} catch (IOException e) {
				Log.e("SaveBeaconsOnFile", "CANNOT CREATE FILE", e);
			}
		}
	}
}
