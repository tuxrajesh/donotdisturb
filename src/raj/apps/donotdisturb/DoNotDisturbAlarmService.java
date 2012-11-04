package raj.apps.donotdisturb;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

/**
 * Service class to receive and process DoNotDisturb alarm.
 * set ringer mode to silence/ring based on intent
 */
public class DoNotDisturbAlarmService extends IntentService {

	final static String TAG = "DoNotDisturbAlarmService";
	final static int NOTIFY_ID = 1;

	/**
	 * Constructor
	 */
	public DoNotDisturbAlarmService() {
		super(TAG);
	}

	/**
	 * onStartCommand() : set ringer mode to silence/ring based on intent
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand");
		Action action = (Action) intent
				.getSerializableExtra(DoNotDisturbActivity.ACTION);

		performAction(action);
		stopSelf();
		return 1;
	}

	/**
	 * onHandleIntent()
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent");
	}

	/**
	 * performAction() : set ringer mode to silence/ring based on action
	 * @param action
	 */
	private void performAction(Action action) {
		NotificationManager notifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (action == Action.START) { // silence the phone
			Log.v(TAG, "Received : " + action.toString());

			Intent notificationIntent = new Intent(this,
					DoNotDisturbAlarmService.class);
			PendingIntent contentIntent = PendingIntent.getService(
					getApplicationContext(), 0, notificationIntent, 0);

			Notification.Builder builder = new Notification.Builder(
					getApplicationContext())
					.setSmallIcon(R.drawable.ic_stat_notify)
					.setContentTitle("Do Not Disturb")
					.setContentText("Phone Silenced")
					.setTicker("Do Not Disturb")
					.setContentIntent(contentIntent);
			Notification notification = builder.getNotification();
			notifyMgr.notify(NOTIFY_ID, notification);

			// silence phone
			AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			
			Log.v(TAG, "Ringer MODE set to Silent");

		} else if (action == Action.END) { // set phone to ring
			Log.v(TAG, "Received : " + action.toString());

			notifyMgr.cancel(NOTIFY_ID);

			AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			
			Log.v(TAG, "Ringer MODE set to Normal");
		}
	}
}
