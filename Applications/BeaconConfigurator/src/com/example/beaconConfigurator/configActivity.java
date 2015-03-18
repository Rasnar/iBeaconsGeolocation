/*-------------------------------------------------------------------------
	FILE		: 	configActivity.java
	DESCRIPTION	:	This activity is called when the user click on a iBeacon.
					The activity will try to connect to the Beacon, if the 
					connection is dropped then it returns to the main activity.
	AUTHORS		:	Da Silva Andrade David
-------------------------------------------------------------------------*/
package com.example.beaconConfigurator;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.connection.BeaconConnection;
import com.example.testlistebeacons.R;

/**
 * Demo that shows how to connect to beacon and change its minor value.
 * 
 * @author wiktor@estimote.com (Wiktor Gworek)
 */
public class configActivity extends Activity {

	private Beacon beacon;
	private BeaconConnection connection;

	private TextView statusView;
	private TextView beaconDetailsView;
	private EditText uuidEditView;
	private EditText majorEditView;
	private EditText minorEditView;
	private EditText broadcastPowerEditView;
	private EditText intervalEditView;
	private View afterConnectedView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		statusView = (TextView) findViewById(R.id.status);
		beaconDetailsView = (TextView) findViewById(R.id.beacon_details);
		afterConnectedView = findViewById(R.id.after_connected);
		uuidEditView = (EditText) findViewById(R.id.uuid);
		majorEditView = (EditText) findViewById(R.id.major);
		minorEditView = (EditText) findViewById(R.id.minor);
		broadcastPowerEditView = (EditText) findViewById(R.id.broadcastPower);
		intervalEditView = (EditText) findViewById(R.id.interval);

		beacon = getIntent().getParcelableExtra(MainActivity.EXTRAS_BEACON);
		connection = new BeaconConnection(this, beacon, createConnectionCallback());

		findViewById(R.id.updateUUID).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//updateUUID(String.valueOf(minorEditView.getText()));
			}
		});

		findViewById(R.id.updateMajor).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int major = parseFromEditView(majorEditView);
				if (major == -1) {
					showToast("Major must be a number");
				} else {
					updateMajor(major);
				}
			}
		});

		findViewById(R.id.updateMinor).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int minor = parseFromEditView(minorEditView);
				if (minor == -1) {
					showToast("Minor must be a number");
				} else {
					updateMinor(minor);
				}
			}
		});

		findViewById(R.id.updateBroadcastPower).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int broadcastPower = parseFromEditView(broadcastPowerEditView);
				if ((broadcastPower == -30) || (broadcastPower == -20) || (broadcastPower == -16) || (broadcastPower == -12)
						|| (broadcastPower == -8) || (broadcastPower == -4) || (broadcastPower == 0) || (broadcastPower == 4)) {
					updateBroadcastPower(broadcastPower);
				} else {
					showToast("Broadcast power must be a number with one of the following values : -30, -20, -16, -12, -8, -4, 0, 4");
				}
			}
		});

		findViewById(R.id.updateInterval).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int interval = parseFromEditView(intervalEditView);
				if ((interval <= 2000) && (interval >= 50)) {
					updateInterval(interval);
				} else {
					showToast("Interval must be a number between 50 and 2000");
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!connection.isConnected()) {
			statusView.setText("Status: Connecting...");
			connection.authenticate();
		}
	}

	@Override
	protected void onDestroy() {
		connection.close();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * @return Parsed integer from edit text view or -1 if cannot be parsed.
	 */
	private int parseFromEditView(EditText editView) {
		try {
			return Integer.parseInt(String.valueOf(editView.getText()));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	private void updateMinor(int minor) {
		// Minor value will be normalized if it is not in the range.
		// Minor should be 16-bit unsigned integer.
		connection.writeMinor(minor, new BeaconConnection.WriteCallback() {
			@Override
			public void onSuccess() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast("Minor value updated");
					}
				});
			}

			@Override
			public void onError() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast("Minor not updated");
					}
				});
			}
		});
	}

	private void updateMajor(int major) {
		// Minor value will be normalized if it is not in the range.
		// Minor should be 16-bit unsigned integer.
		connection.writeMajor(major, new BeaconConnection.WriteCallback() {
			@Override
			public void onSuccess() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast("Major value updated");
					}
				});
			}

			@Override
			public void onError() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast("Major not updated");
					}
				});
			}
		});
	}

	private void updateUUID(String uuid) {
		// Minor value will be normalized if it is not in the range.
		// Minor should be 16bytes write in hexadecimal .
		connection.writeProximityUuid(uuid, new BeaconConnection.WriteCallback() {
			@Override
			public void onSuccess() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast("UUID value updated");
					}
				});
			}

			@Override
			public void onError() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast("UUID not updated");
					}
				});
			}
		});
	}

	private void updateInterval(int interval) {
		connection.writeAdvertisingInterval(interval, new BeaconConnection.WriteCallback() {
			@Override
			public void onSuccess() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast("Interval value updated");
					}
				});
			}

			@Override
			public void onError() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast("Interval value not updated");
					}
				});
			}
		});
	}

	private void updateBroadcastPower(int powerDBM) {
		connection.writeBroadcastingPower(powerDBM, new BeaconConnection.WriteCallback() {
			@Override
			public void onSuccess() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast("Broadcast power value updated");
					}
				});
			}

			@Override
			public void onError() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showToast("Broadcast power  not updated");
					}
				});
			}
		});
	}

	private BeaconConnection.ConnectionCallback createConnectionCallback() {
		return new BeaconConnection.ConnectionCallback() {
			@Override
			public void onAuthenticated(final BeaconConnection.BeaconCharacteristics beaconChars) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						statusView.setText("Status: Connected to beacon");
						StringBuilder sb = new StringBuilder().append("MAC: " + beacon.getMacAddress()).append("\n")
								.append("UUID: " + beacon.getProximityUUID()).append("\n").append("Major: ").append(beacon.getMajor()).append("\n")
								.append("Minor: ").append(beacon.getMinor()).append("\n").append("Advertising interval: ")
								.append(beaconChars.getAdvertisingIntervalMillis()).append("ms\n").append("Broadcasting power: ")
								.append(beaconChars.getBroadcastingPower()).append(" dBm\n").append("Battery: ")
								.append(beaconChars.getBatteryPercent()).append(" %");
						beaconDetailsView.setText(sb.toString());
						minorEditView.setText(String.valueOf(beacon.getMinor()));
						majorEditView.setText(String.valueOf(beacon.getMajor()));
						uuidEditView.setText(String.valueOf(beacon.getProximityUUID()));
						broadcastPowerEditView.setText(String.valueOf(beaconChars.getBroadcastingPower()));
						intervalEditView.setText(String.valueOf(beaconChars.getAdvertisingIntervalMillis()));
						afterConnectedView.setVisibility(View.VISIBLE);
					}
				});
			}

			@Override
			public void onAuthenticationError() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						statusView.setText("Status: Cannot connect to beacon. Authentication problems.");
					}
				});
			}

			@Override
			public void onDisconnected() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						statusView.setText("Status: Disconnected from beacon");
					}
				});
			}
		};
	}

	private void showToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
	}
}
