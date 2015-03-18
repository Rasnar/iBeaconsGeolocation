/*-------------------------------------------------------------------------
	FILE		: 	Bus.java
	DESCRIPTION	:	Implementation of a bus. Contain every attribute 
					necessary to define a bus.
	AUTHORS		:	Da Silva Andrade David
-------------------------------------------------------------------------*/

package com.hepia.BusLocation;

import java.util.ArrayList;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

public class Bus {

	private String lineNumber;

	private String City;
	private String ID;
	private ArrayList<Beacon> beaconsFrontToBack;
	private ArrayList<Beacon> lastBeaconsDetected;
	private int distanceBeetweenBeacons;
	
	/*-------------------------------------------------------------------------
		DESCRIPTION	:	Constructor
		PARAMS		: 	(String) lineNumber
						(String) City
						(String) ID
						(ArrayList<Beacon>) beaconsFrontBack
						(int) distanceBeetweenBeacons
		RETURN		: 	none
	-------------------------------------------------------------------------*/
	public Bus(String lineNumber, String City, String ID, ArrayList<Beacon> beaconsFrontToBack, int distanceBeetweenBeacons) {
		this.lineNumber = lineNumber;
		this.ID = ID;
		this.beaconsFrontToBack = beaconsFrontToBack;
		this.distanceBeetweenBeacons = distanceBeetweenBeacons;
		this.lastBeaconsDetected = new ArrayList<Beacon>();
	}

	/* Accessors */
	
	public String getLineNumber() {
		return lineNumber;
	}

	public String getCity() {
		return City;
	}

	public String getID() {
		return ID;
	}

	public ArrayList<Beacon> getBeaconsFrontToBack() {
		return beaconsFrontToBack;
	}

	public int getDistanceBeetweenBeacons() {
		return distanceBeetweenBeacons;
	}
	
	public ArrayList<Beacon> getLastBeaconsDetected(){
		return lastBeaconsDetected;
	}

	/*-------------------------------------------------------------------------
		DESCRIPTION	:	This method permit to test if the bus is detected
						in the ArrayList passed in parameters. If the bus is
						indeed detected the method will return true, otherwise
						it will return false.
		PARAMS		: 	(ArrayList<Beacon>) allBeaconsDetected
		RETURN		: 	Boolean
	-------------------------------------------------------------------------*/
	public Boolean isThisBusDetected(ArrayList<Beacon> allBeaconsDetected) {
		// Clear old values
		lastBeaconsDetected.clear();
		
		Boolean beacon_found = false;
		for (Beacon beacInBus : beaconsFrontToBack) {
			for (Beacon beacDetected : allBeaconsDetected) {
				beacon_found = false;
				if ((beacDetected.getProximityUUID().equals(beacInBus.getProximityUUID())) 
						&& (beacDetected.getMajor() == beacInBus.getMajor())
						&& (beacDetected.getMinor() == beacInBus.getMinor())) {
					// Only save the beacon if you are near enough (150% of the distance beetween each beacon
					// in the bus)
					if (Utils.computeAccuracy(beacDetected) < (distanceBeetweenBeacons + distanceBeetweenBeacons / 2)) {						
						lastBeaconsDetected.add(beacDetected);
						beacon_found = true;
						break;
					}
				}
			}
			if (!beacon_found)
				return false;
		}
		return true;
	}
}
