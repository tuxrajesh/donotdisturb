package raj.apps.donotdisturb;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

public class DoNotDisturbCallService extends IntentService {

	private final static String TAG = "DoNotDisturbCallService";
	
	public DoNotDisturbCallService() {
		super(TAG);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v(TAG, "onStartCommand");
		
		int ringerMode = intent.getIntExtra("DoNotDisturbCallReceiver", AudioManager.RINGER_MODE_NORMAL);
		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		manager.setRingerMode(ringerMode);
		
		Log.v(TAG, "Received " + ringerMode);
		
		return startId;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent");
	}
}
