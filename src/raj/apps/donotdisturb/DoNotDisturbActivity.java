package raj.apps.donotdisturb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import raj.apps.donotdisturb.TimePickerFragment.OnTimePickedListener;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.Groups;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

/**
 * DoNotDisturbActivity: Setting activity for DoNotDisturb mode.
 */
public class DoNotDisturbActivity extends Activity {

	// private constants
	private static final long RECURRING_INTERVAL = AlarmManager.INTERVAL_DAY;
	private static final String TAG = "DoNotDisturbActivity";

	// public constants
	public static final String ACTION = "Action";
	public static final String PREFERENCE_NAME = "DoNotDisturbSchedule";
	public static final String START_TIME = "StartTime";
	public static final String END_TIME = "EndTime";
	public static final String ENABLED_FLAG = "EnabledFlag";
	public static final String SCHEDULED_FLAG = "ScheduledFlag";
	public static final String ALLOW_CALLS_FROM = "AllowCallsFrom";
	public static final String NO_ONE = "-1 - No one";

	// private variables
	private Boolean mEnabled;
	private Boolean mScheduled;
	private String mStartTime;
	private String mEndTime;
	private String mAllowCalls;

	private Switch mEnable;
	private CheckBox mSchedule;
	private Button mStart;
	private Button mEnd;
	private Spinner mAllowCallFrom;

	/**
	 * onCreate(): set/restore the values of controls from SharedPreferences
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.donotdisturb_activity);

		mEnable = (Switch) findViewById(R.id.swt_enable);
		mSchedule = (CheckBox) findViewById(R.id.chk_scheduled);
		mStart = (Button) findViewById(R.id.btn_start_time);
		mEnd = (Button) findViewById(R.id.btn_end_time);
		mAllowCallFrom = (Spinner) findViewById(R.id.snr_allow_calls);

		// Restore preferences
		SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);

		mEnabled = sharedPref.getBoolean(ENABLED_FLAG, false);
		mScheduled = sharedPref.getBoolean(SCHEDULED_FLAG, false);
		mStartTime = sharedPref.getString(START_TIME, "23:00");
		mEndTime = sharedPref.getString(END_TIME, "06:00");
		mAllowCalls = sharedPref.getString(ALLOW_CALLS_FROM, "No one");

		mEnable.setChecked(mEnabled);
		mSchedule.setChecked(mScheduled);
		mStart.setText(formatTime(mStartTime, "hh:mm", "hh:mm a"));
		mEnd.setText(formatTime(mEndTime, "hh:mm", "hh:mm a"));
		
		// allowCallFrom spinner handler
		mAllowCallFrom.setOnItemSelectedListener(new OnItemSelectedListener() {

			/**
			 * onItemSelected() : add to the SharedPreference
			 */
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mAllowCalls = parent.getItemAtPosition(position).toString();
				
				SharedPreferences sharedPref = getSharedPreferences(
						PREFERENCE_NAME, MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();

				editor.putString(ALLOW_CALLS_FROM, mAllowCalls);
				editor.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
			
		});

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getApplicationContext(), R.layout.spinner, getAllowCallsList());
		adapter.setDropDownViewResource(R.layout.spinner_dropdown);
		mAllowCallFrom.setAdapter(adapter);
		mAllowCallFrom.setSelection(adapter.getPosition(mAllowCalls));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.donotdisturb_activity, menu);
		return true;
	}

	/**
	 * onEnableClick(): handles the DoNotDisturb Mode enable/disable
	 * @param view
	 */
	public void onEnableClick(View view) {
		boolean on = ((CompoundButton) view).isChecked();

		SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);

		// enable DoNotDisturb mode
		if (on) {
			handleEnable(Action.START);
		} else { // disable DoNotDisturb mode
			handleEnable(Action.END);
		}

		// persist the enable/disable preference
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putBoolean(ENABLED_FLAG, on);
		editor.commit();
	}

	/**
	 * onScheduledClick() : enable/disable schedule
	 * @param view
	 */
	public void onScheduledClick(View view) {
		boolean on = ((CheckBox) view).isChecked();

		// Get Preferences
		SharedPreferences sharedPref = getSharedPreferences(PREFERENCE_NAME,
				MODE_PRIVATE);

		mStartTime = sharedPref.getString(START_TIME, "23:00");
		mEndTime = sharedPref.getString(END_TIME, "06:00");

		String toastMessage = null;

		if (on) { // onEnable : trigger alarm to enable DoNotDisturb mode

			handleEnable(Action.END);
			handleAlarm(this, mStartTime, Action.START, true);
			handleAlarm(this, mEndTime, Action.END, true);

			toastMessage = String.format("Schedule set between %s and %s",
					formatTime(mStartTime, "hh:mm", "hh:mm a"),
					formatTime(mEndTime, "hh:mm", "hh:mm a"));

		} else { // onDisable : trigger alarm to disable DoNotDisturb mode

			handleEnable(Action.START);
			handleAlarm(this, mStartTime, Action.START, false);
			handleAlarm(this, mEndTime, Action.END, false);

			toastMessage = "Schedule Cancelled";

		}
		
		// persist preferences
		SharedPreferences.Editor editor = sharedPref.edit();

		editor.putBoolean(SCHEDULED_FLAG, on);
		editor.commit();

		Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG)
				.show();
	}

	/**
	 * onStartTimePickClick() : set the Start Time for Schedule
	 * @param view
	 */
	public void onStartTimePickClick(View view) {
		
		// create the TimePickerFragment dialog
		TimePickerFragment startTimeFragment = new TimePickerFragment();
		
		// setOnTimePick handler
		startTimeFragment.setOnTimePickedListener(new OnTimePickedListener() {

			/**
			 * onTimePicked(): persist preference and re-schedule alarm
			 */
			@Override
			public void onTimePicked(int hourOfDay, int minute) {
				mStartTime = String.format("%d:%d", hourOfDay, minute);
				mStart.setText(formatTime(mStartTime, "hh:mm", "hh:mm a"));

				SharedPreferences sharedPref = getSharedPreferences(
						PREFERENCE_NAME, MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();

				editor.putString(START_TIME, mStartTime);
				editor.commit();

				handleAlarm(getApplicationContext(), mStartTime, Action.START,
						false);
			}
		});
		startTimeFragment.show(getFragmentManager(), "StartTimePicker");
	}

	/**
	 * onEndTimePickClick(): set the End Time of the schedule
	 * @param view
	 */
	public void onEndTimePickClick(View view) {
		
		// create the TimePickerFragment dialog
		TimePickerFragment endTimeFragment = new TimePickerFragment();
		
		// setOnTimePick handler
		endTimeFragment.setOnTimePickedListener(new OnTimePickedListener() {

			/**
			 * onTimePicked(): persist preference and re-schedule alarm
			 */
			@Override
			public void onTimePicked(int hourOfDay, int minute) {
				mEndTime = String.format("%d:%d", hourOfDay, minute);
				mEnd.setText(formatTime(mEndTime, "hh:mm", "hh:mm a"));

				SharedPreferences sharedPref = getSharedPreferences(
						PREFERENCE_NAME, MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPref.edit();

				editor.putString(END_TIME, mEndTime);
				editor.commit();

				handleAlarm(getApplicationContext(), mEndTime, Action.START,
						false);
			}
		});
		endTimeFragment.show(getFragmentManager(), "EndTimePicker");
	}

	/**
	 * handleEnable(): trigger the DoNotDisturbAlarmService with intended action
	 * @param action
	 */
	private void handleEnable(Action action) {
		Context context = getApplicationContext();
		Intent serviceIntent = new Intent(getApplicationContext(),
				DoNotDisturbAlarmService.class);
		serviceIntent.putExtra(DoNotDisturbActivity.ACTION, action);
		context.startService(serviceIntent);
	}

	/**
	 * handleAlarm() : cancel/set the recurring alarm for DoNotDisturb mode
	 * @param context
	 * @param time
	 * @param action
	 * @param enable
	 */
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
		receiverIntent.putExtra(DoNotDisturbActivity.ACTION, action);

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

	/**
	 * formatTime() : format the given time from input to output format
	 * @param inputTime
	 * @param inputFormat
	 * @param outputFormat
	 * @return
	 */
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

	/**
	 * getAllowCallsList() : get the groups from the phone contacts
	 * @return
	 */
	private ArrayList<String> getAllowCallsList() {
		ArrayList<String> contactGroups = new ArrayList<String>();
		
		contactGroups.add("-1 - No one");

		Cursor groupCursor = getContentResolver().query(Groups.CONTENT_URI,
				new String[] { Groups._ID, Groups.TITLE }, null, null, null);
		while (groupCursor.moveToNext()) {
			contactGroups.add(groupCursor.getString(groupCursor
					.getColumnIndex(Groups._ID)) + " - " + groupCursor.getString(groupCursor
					.getColumnIndex(Groups.TITLE)));
		}

		return contactGroups;
	}

}
