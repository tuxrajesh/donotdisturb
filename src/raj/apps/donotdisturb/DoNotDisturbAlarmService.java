package raj.apps.donotdisturb;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class DoNotDisturbAlarmService extends IntentService {

	final static String TAG = "DoNotDisturbAlarmService";
	final static int NOTIFY_ID = 1;

	public DoNotDisturbAlarmService() {
		super(TAG);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand");
		Action action = (Action) intent
				.getSerializableExtra(DoNotDisturbActivity.ACTION);

		handleEnable(action);
		stopSelf();
		return 1;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent");
	}

	private void handleEnable(Action action) {
		NotificationManager notifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (action == Action.START) {
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

			AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			manager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			Log.v(TAG, "Ringer MODE set to Silent");

		} else if (action == Action.END) {
			Log.v(TAG, "Received : " + action.toString());

			notifyMgr.cancel(NOTIFY_ID);

			AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			manager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			Log.v(TAG, "Ringer MODE set to Normal");
		}
	}
}
