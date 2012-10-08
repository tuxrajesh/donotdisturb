package raj.apps.donotdisturb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DoNotDisturbReceiver extends BroadcastReceiver {

	final static String TAG = "DoNotDisturbReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v(TAG, "onReceive");

		Action action = (Action) intent
				.getSerializableExtra("DoNotDisturbActivity");

		Intent serviceIntent = new Intent(context, DoNotDisturbService.class);
		serviceIntent.putExtra(TAG, action);
		context.startService(serviceIntent);

	}

}
