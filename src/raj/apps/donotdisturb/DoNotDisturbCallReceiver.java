package raj.apps.donotdisturb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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

		Log.v(TAG, state);

		if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {

			String phoneNumber = bundle
					.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
			Log.v(TAG, phoneNumber);

			if (phoneNumber.equalsIgnoreCase("8043800383")
					|| phoneNumber.equalsIgnoreCase("7326685854")) {

				Intent serviceIntent = new Intent(context,
						DoNotDisturbCallService.class);
				serviceIntent.putExtra("DoNotDisturbCallReceiver",
						AudioManager.RINGER_MODE_NORMAL);
				context.startService(serviceIntent);
			}
			
		} else if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_OFFHOOK)) {

			Intent serviceIntent = new Intent(context,
					DoNotDisturbCallService.class);
			serviceIntent.putExtra("DoNotDisturbCallReceiver",
					AudioManager.RINGER_MODE_SILENT);
			context.startService(serviceIntent);
		}
	}

}
