package raj.apps.donotdisturb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DoNotDisturbAlarmReceiver extends BroadcastReceiver {

	final static String TAG = "DoNotDisturbAlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "onReceive");

		Action action = (Action) intent
				.getSerializableExtra("DoNotDisturbActivity");

		Intent serviceIntent = new Intent(context, DoNotDisturbAlarmService.class);
		serviceIntent.putExtra(TAG, action);
		context.startService(serviceIntent);

	}

}
