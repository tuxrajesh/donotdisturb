package raj.apps.donotdisturb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * BroadcastReceiver that receives DoNotDisturb alarm.
 */
public class DoNotDisturbAlarmReceiver extends BroadcastReceiver {

	final static String TAG = "DoNotDisturbAlarmReceiver";

	/**
	 * onReceive() : start the DoNotDisturbAlarmService
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "onReceive");

		Action action = (Action) intent
				.getSerializableExtra(DoNotDisturbActivity.ACTION);

		Intent serviceIntent = new Intent(context, DoNotDisturbAlarmService.class);
		serviceIntent.putExtra(DoNotDisturbActivity.ACTION, action);
		context.startService(serviceIntent);

	}

}
