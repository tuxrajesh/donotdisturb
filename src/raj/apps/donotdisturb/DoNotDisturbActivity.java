package raj.apps.donotdisturb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class DoNotDisturbActivity extends Activity {

	private static final long RECURRING_INTERVAL = AlarmManager.INTERVAL_DAY;
	private static final String TAG = "DoNotDisturbActivity";

	public static final String PREFERENCE_NAME = "DoNotDisturbSchedule";
	public static final String START_TIME = "StartTime";
	public static final String END_TIME = "EndTime";
	public static final String ENABLED_FLAG = "EnabledFlag";

	private String mStartTime;
	private String mEndTime;

	private Button mStart;
	private Button mEnd;
	private Switch mEnable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.donotdisturb_activity);

		mStart = (Button) findViewById(R.id.btn_start_time);
		mEnd = (Button) findViewById(R.id.btn_end_time);
		mEnable = (Switch) findViewById(R.id.swt_enable);
		

		// Restore preferences
		SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);

		mStartTime = sharedPref.getString(START_TIME, "23:00");
		mEndTime = sharedPref.getString(END_TIME, "06:00");

		mEnable.setChecked(sharedPref.getBoolean(ENABLED_FLAG, false));
		mStart.setText(formatTime(mStartTime, "hh:mm", "hh:mm a"));
		mEnd.setText(formatTime(mEndTime, "hh:mm", "hh:mm a"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.donotdisturb_activity, menu);
		return true;
	}

	public void onSwitchClick(View view) {
		boolean on = ((CompoundButton) view).isChecked();

		// Get Preferences
		SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);

		mStartTime = sharedPref.getString(START_TIME, "23:00");
		mEndTime = sharedPref.getString(END_TIME, "06:00");
		
		String toastMessage = null;

		if (on) {
			handleAlarm(this, mStartTime, Action.START, true);
			handleAlarm(this, mEndTime, Action.END, true);
			
			toastMessage = String.format("Schedule set between %s and %s",
					formatTime(mStartTime, "hh:mm", "hh:mm a"),
					formatTime(mEndTime, "hh:mm", "hh:mm a"));
		} else {
			handleAlarm(this, mStartTime, Action.START, false);
			handleAlarm(this, mEndTime, Action.END, false);
			
			toastMessage = String.format(
					"Schedule cancelled between %s and %s",
					formatTime(mStartTime, "hh:mm", "hh:mm a"),
					formatTime(mEndTime, "hh:mm", "hh:mm a"));
		}
		
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putBoolean(ENABLED_FLAG, on);
		editor.commit();
		
		Toast.makeText(getApplicationContext(), toastMessage,
				Toast.LENGTH_LONG).show();
	}

	public void onStartTimePickClick(View view) {
		TimePickerFragment startTimeFragment = new TimePickerFragment();
		startTimeFragment.setOnTimePickedListener(new OnTimePickedListener() {

			@Override
			public void onTimePicked(int hourOfDay, int minute) {
				mStartTime = String.format("%d:%d", hourOfDay, minute);
				mStart.setText(formatTime(mStartTime, "hh:mm", "hh:mm a"));
			}
		});
		startTimeFragment.show(getFragmentManager(), "StartTimePicker");
	}

	public void onEndTimePickClick(View view) {
		TimePickerFragment endTimeFragment = new TimePickerFragment();
		endTimeFragment.setOnTimePickedListener(new OnTimePickedListener() {

			@Override
			public void onTimePicked(int hourOfDay, int minute) {
				mEndTime = String.format("%d:%d", hourOfDay, minute);
				mEnd.setText(formatTime(mEndTime, "hh:mm", "hh:mm a"));
			}
		});
		endTimeFragment.show(getFragmentManager(), "EndTimePicker");
	}

	public void onApplyClick(View view) {
		SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putString(START_TIME, mStartTime);
		editor.putString(END_TIME, mEndTime);
		editor.commit();
		
		Toast.makeText(getApplicationContext(), "Schedule Saved", Toast.LENGTH_LONG).show();
	}

	private void handleAlarm(Context context, String time, Action action,
			Boolean enable) {
		Log.v(TAG, "handleAlarm");

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

		if (enable) {
			manager.setRepeating(AlarmManager.RTC_WAKEUP,
					calendar.getTimeInMillis(), RECURRING_INTERVAL,
					actionIntent);
		} else {

			manager.cancel(actionIntent);
		}
	}

	private String formatTime(String inputTime, String inputFormat,
			String outputFormat) {
		String outputTime = null;

		try {
			SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormat);
			Date date = inputDateFormat.parse(inputTime);
			SimpleDateFormat outputDateFormat = new SimpleDateFormat(
					outputFormat);
			outputTime = outputDateFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return outputTime;
	}
}
