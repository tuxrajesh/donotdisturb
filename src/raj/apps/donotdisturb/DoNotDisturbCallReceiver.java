package raj.apps.donotdisturb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DoNotDisturbCallReceiver extends BroadcastReceiver {

	private static final String TAG = "DoNotDisturbCallReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "onReceive");

		Bundle bundle = intent.getExtras();
		String state = bundle.getString(TelephonyManager.EXTRA_STATE);
		String phoneNumber = bundle
				.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

		Intent serviceIntent = new Intent(context,
				DoNotDisturbCallService.class);

		serviceIntent.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER,
				phoneNumber);
		serviceIntent.putExtra(TelephonyManager.EXTRA_STATE, state);

		context.startService(serviceIntent);
	}

}
