package raj.apps.donotdisturb;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class DoNotDisturbService extends IntentService {

	final static String TAG = "DoNotDisturbService";
	final static int NOTIFY_ID = 1;

	public DoNotDisturbService() {
		super(TAG);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand");
		Action action = (Action) intent
				.getSerializableExtra("DoNotDisturbReceiver");

		NotificationManager notifyMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (action == Action.START) {
			Log.v(TAG, "Received : " + action.toString());

			Intent notificationIntent = new Intent(this,
					DoNotDisturbService.class);
			PendingIntent contentIntent = PendingIntent.getService(this, 0,
					notificationIntent, 0);

			Notification.Builder builder = new Notification.Builder(
					getApplicationContext())
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Do Not Disturb")
					.setContentText("Phone Silenced")
					.setTicker("Ticker: Do Not Disturb")
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
			stopSelf();
			Log.v(TAG, "Ringer MODE set to Normal");
		}
		return 1;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent");
	}

}
