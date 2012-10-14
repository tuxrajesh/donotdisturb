package raj.apps.donotdisturb;

import java.util.Calendar;

import raj.apps.donotdisturb.TimePickerFragment.OnTimePickedListener;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class DoNotDisturbActivity extends Activity {

	private static final long RECURRING_INTERVAL = AlarmManager.INTERVAL_DAY;
	private static final String TAG = "DoNotDisturbActivity";

	public static final String PREFERENCE_NAME = "DoNotDisturbSchedule";
	public static final String START_TIME = "StartTime";
	public static final String END_TIME = "EndTime";

	private Button mStartTime;
	private Button mEndTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.donotdisturb_activity);

		mStartTime = (Button) findViewById(R.id.btn_start_time);
		mEndTime = (Button) findViewById(R.id.btn_end_time);

		// Restore preferences
		SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);
		mStartTime.setText(sharedPref.getString(START_TIME, "23:00"));
		mEndTime.setText(sharedPref.getString(END_TIME, "06:00"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.donotdisturb_activity, menu);
		return true;
	}

	public void onStartTimePickClick(View view) {
		TimePickerFragment startTimeFragment = new TimePickerFragment();
		startTimeFragment.setOnTimePickedListener(new OnTimePickedListener() {

			@Override
			public void onTimePicked(int hourOfDay, int minute) {
				String timeOfDay = String.format("%d:%d", hourOfDay, minute);
				mStartTime.setText(timeOfDay);
			}
		});
		startTimeFragment.show(getFragmentManager(), "StartTimePicker");
	}

	public void onEndTimePickClick(View view) {
		TimePickerFragment endTimeFragment = new TimePickerFragment();
		endTimeFragment.setOnTimePickedListener(new OnTimePickedListener() {

			@Override
			public void onTimePicked(int hourOfDay, int minute) {
				String timeOfDay = String.format("%d:%d", hourOfDay, minute);
				mEndTime.setText(timeOfDay);
			}
		});
		endTimeFragment.show(getFragmentManager(), "EndTimePicker");
	}

	public void onApplyClick(View view) {
		createAlarm(this, mStartTime.getText().toString(), Action.START);
		createAlarm(this, mEndTime.getText().toString(), Action.END);
	}

	private void createAlarm(Context context, String time, Action action) {

		SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();

		if (action == Action.START) {
			editor.putString(START_TIME, time);
		} else {
			editor.putString(END_TIME, time);
		}

		editor.commit();

		int hourOfDay = Integer.parseInt(time.split(":")[0]);
		int startMinute = Integer.parseInt(time.split(":")[1]);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, startMinute);

		AlarmManager manager = (AlarmManager) context
				.getSystemService(ALARM_SERVICE);
		Intent receiverIntent = new Intent(context,
				DoNotDisturbAlarmReceiver.class);
		receiverIntent.putExtra(TAG, action);

		PendingIntent actionIntent = PendingIntent.getBroadcast(context,
				action.ordinal(), receiverIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		manager.setRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(), RECURRING_INTERVAL, actionIntent);

		Log.v(TAG, "Alarm Set " + action.toString());
	}
}
