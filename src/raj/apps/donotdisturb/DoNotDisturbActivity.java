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
import android.widget.Toast;

public class DoNotDisturbActivity extends Activity {

	private static final long RECURRING_INTERVAL = AlarmManager.INTERVAL_DAY;
	private static final String TAG = "DoNotDisturbActivity";

	public static final String PREFERENCE_NAME = "DoNotDisturbSchedule";
	public static final String START_TIME = "StartTime";
	public static final String END_TIME = "EndTime";
	
	private String mStartTime;
	private String mEndTime;

	private Button mStart;
	private Button mEnd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.donotdisturb_activity);

		mStart = (Button) findViewById(R.id.btn_start_time);
		mEnd = (Button) findViewById(R.id.btn_end_time);

		// Restore preferences
		SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);
		
		mStartTime = sharedPref.getString(START_TIME, "23:00");
		mEndTime = sharedPref.getString(END_TIME, "06:00");
		
		formatAndDisplayTime(mStart, mStartTime);
		formatAndDisplayTime(mEnd, mEndTime);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.donotdisturb_activity, menu);
		return true;
	}
	
	public void onSwitchClick(View view) {
	    boolean on = ((CompoundButton) view).isChecked();
	    
	    if (on) {
	        // Enable Alarm
	    	Toast.makeText(getApplicationContext(), "ON", Toast.LENGTH_SHORT).show();
	    } else {
	        // Disable Alarm
	    	Toast.makeText(getApplicationContext(), "OFF", Toast.LENGTH_SHORT).show();
	    }
	}

	public void onStartTimePickClick(View view) {
		TimePickerFragment startTimeFragment = new TimePickerFragment();
		startTimeFragment.setOnTimePickedListener(new OnTimePickedListener() {

			@Override
			public void onTimePicked(int hourOfDay, int minute) {
				mStartTime = String.format("%d:%d", hourOfDay, minute);
				formatAndDisplayTime(mStart, mStartTime);
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
				formatAndDisplayTime(mEnd, mEndTime);
			}
		});
		endTimeFragment.show(getFragmentManager(), "EndTimePicker");
	}

	public void onApplyClick(View view) {
		createAlarm(this, mStartTime, Action.START);
		createAlarm(this, mEndTime, Action.END);
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
		
		String displayInfo = String.format("Silence Schedule : %s to %s", mStartTime, mEndTime);
		Toast.makeText(getApplicationContext(), displayInfo, Toast.LENGTH_SHORT).show();

		Log.v(TAG, "Alarm Set " + action.toString());
	}
	
	private void formatAndDisplayTime(Button button, String time) {
		
		try {
			SimpleDateFormat inputFormat = new SimpleDateFormat("hh:mm");
			Date date = inputFormat.parse(time);
			
			SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");
			button.setText(outputFormat.format(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
