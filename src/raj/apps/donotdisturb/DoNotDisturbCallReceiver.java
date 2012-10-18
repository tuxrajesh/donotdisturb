package raj.apps.donotdisturb;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

		SharedPreferences pref = context.getSharedPreferences(
				DoNotDisturbActivity.PREFERENCE_NAME, Context.MODE_PRIVATE);

		String startTime = pref.getString(DoNotDisturbActivity.START_TIME,
				"23:00");
		String endTime = pref.getString(DoNotDisturbActivity.END_TIME, "06:00");
		Boolean enabled = pref.getBoolean(DoNotDisturbActivity.ENABLED_FLAG, false);
		
		if(!enabled) {
			return;
		}

		Calendar currentCalendar = Calendar.getInstance();
		int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
		int currentMinute = currentCalendar.get(Calendar.MINUTE);
		currentCalendar.set(Calendar.HOUR_OF_DAY, currentHour);
		currentCalendar.set(Calendar.MINUTE, currentMinute);

		Calendar startCalendar = Calendar.getInstance();
		startCalendar.set(Calendar.HOUR_OF_DAY,
				Integer.parseInt(startTime.split(":")[0]));
		startCalendar.set(Calendar.MINUTE,
				Integer.parseInt(startTime.split(":")[1]));

		Calendar endCalendar = Calendar.getInstance();
		endCalendar.set(Calendar.HOUR_OF_DAY,
				Integer.parseInt(endTime.split(":")[0]));
		endCalendar.set(Calendar.MINUTE,
				Integer.parseInt(endTime.split(":")[1]));

		if (currentCalendar.after(startCalendar)
				&& endCalendar.after(currentCalendar)) {

			if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {

				String phoneNumber = bundle
						.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
				Log.v(TAG, phoneNumber);

//				if (phoneNumber.equalsIgnoreCase("1234567890")) {
//
//					Intent serviceIntent = new Intent(context,
//							DoNotDisturbCallService.class);
//					serviceIntent.putExtra("DoNotDisturbCallReceiver",
//							AudioManager.RINGER_MODE_NORMAL);
//					context.startService(serviceIntent);
//				}

			} else {

				Intent serviceIntent = new Intent(context,
						DoNotDisturbCallService.class);
				serviceIntent.putExtra("DoNotDisturbCallReceiver",
						AudioManager.RINGER_MODE_SILENT);
				context.startService(serviceIntent);
			}
		}
	}

}
