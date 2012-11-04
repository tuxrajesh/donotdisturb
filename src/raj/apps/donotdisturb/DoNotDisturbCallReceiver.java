package raj.apps.donotdisturb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Broadcast Receiver class that responds to calls received.
 * This class starts the DoNotDisturbCallService to service calls.
 */
public class DoNotDisturbCallReceiver extends BroadcastReceiver {

	private static final String TAG = "DoNotDisturbCallReceiver";

	/**
	 * onReceive() method: starts the DoNotDisturbCallService with incoming phone number and call state.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "onReceive");

		// get state (RINGING) and incoming phone number
		Bundle bundle = intent.getExtras();
		String state = bundle.getString(TelephonyManager.EXTRA_STATE);
		String phoneNumber = bundle
				.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

		// set the state and incoming number as intent extras to service
		Intent serviceIntent = new Intent(context,
				DoNotDisturbCallService.class);

		serviceIntent.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER,
				phoneNumber);
		serviceIntent.putExtra(TelephonyManager.EXTRA_STATE, state);

		// start the DoNotDisturbCallService service
		context.startService(serviceIntent);
	}

}
