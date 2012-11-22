package raj.apps.donotdisturb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * DoNotDisturbActivity: Settings activity for DoNotDisturb mode.
 */
public class DoNotDisturbActivity extends Activity {

	// private constants
	private static final long RECURRING_INTERVAL = AlarmManager.INTERVAL_DAY;
	private static final String TAG = "DoNotDisturbActivity";

	// public constants
	public static final String KEY_PREF_MODE = "pref_mode";
	public static final String KEY_PREF_SCHEDULED = "pref_scheduled";
	public static final String KEY_PREF_FROM = "pref_from";
	public static final String KEY_PREF_TO = "pref_to";
	public static final String KEY_ALLOW_CALLS_FROM = "pref_allow_calls_from";

	private static OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

	protected DoNotDisturbSettingsFragment mSettingsFragment;

	public static final String ACTION = "Action";
	public static final String NO_ONE = "0 - No one";

	// private variables
	private Boolean mEnabled;
	private Boolean mScheduled;
	private String mFrom;
	private String mTo;

	/**
	 * onCreate(): set/restore the values of controls from SharedPreferences
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSettingsFragment = new DoNotDisturbSettingsFragment();

		// Listener for change to any settings
		onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				Log.v(TAG, String.format("%s : %s",
						"onSharedPreferenceChanged", key));

				mEnabled = sharedPreferences.getBoolean(KEY_PREF_MODE, false);
				mScheduled = sharedPreferences.getBoolean(KEY_PREF_SCHEDULED,
						false);
				mFrom = sharedPreferences.getString(KEY_PREF_FROM, "23:00");
				mTo = sharedPreferences.getString(KEY_PREF_TO, "06:00");
				
				Preference fromPref = mSettingsFragment
						.findPreference(KEY_PREF_FROM);

				if (fromPref != null) {
					fromPref.setSummary(formatTime(mFrom, "hh:mm",
							"hh:mm a"));
				}

				Preference toPref = mSettingsFragment
						.findPreference(KEY_PREF_TO);

				if (toPref != null) {
					toPref.setSummary(formatTime(mTo, "hh:mm",
							"hh:mm a"));
				}
				
				Context context = getApplicationContext();

				if (mEnabled) { // on enabled

					if (mScheduled) { // on enabled and scheduled

						// cancel any existing one-time alarms
						Calendar currentCalendar = Calendar.getInstance();
						changeAlarm(context, currentCalendar, Action.END, true,
								false);

						// set alarm with START for start time
						Calendar startCalendar = getCalendar(mFrom);
						changeAlarm(context, startCalendar, Action.START,
								false, true);

						// set alarm with END for end time
						Calendar endCalendar = getCalendar(mTo);
						changeAlarm(context, endCalendar, Action.END, false,
								true);

					} else { // on enabled and not scheduled

						// cancel any existing scheduled alarms
						Calendar startCalendar = getCalendar(mFrom);
						changeAlarm(context, startCalendar, Action.START, true,
								true);

						Calendar endCalendar = getCalendar(mTo);
						changeAlarm(context, endCalendar, Action.END, true,
								true);

						// set alarm with START for current time
						Calendar currentCalendar = Calendar.getInstance();
						changeAlarm(context, currentCalendar, Action.START,
								false, false);
					}

				} else { // on disabled

					// cancel any existing scheduled alarms
					Calendar startCalendar = getCalendar(mFrom);
					changeAlarm(context, startCalendar, Action.START, true,
							true);

					Calendar endCalendar = getCalendar(mTo);
					changeAlarm(context, endCalendar, Action.END, true, true);

					// set alarm with START for current time
					Calendar currentCalendar = Calendar.getInstance();
					changeAlarm(context, currentCalendar, Action.END, true,
							false);
				}
			}
		};

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		sharedPreferences
				.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

		// Display the Settings Fragment
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, mSettingsFragment).commit();
	}

	/**
	 * onResume() : register sharedPreferenceChange listener
	 */
	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		sharedPreferences
				.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	/**
	 * onPause() : unregister sharedPreferenceChange listener
	 */
	@Override
	protected void onPause() {
		super.onPause();

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		sharedPreferences
				.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	/**
	 * getCalendar() : get the calendar from the string representation of time
	 * 
	 * @param time
	 * @return
	 */
	private Calendar getCalendar(String time) {

		int hourOfDay = Integer.parseInt(time.split(":")[0]);
		int startMinute = Integer.parseInt(time.split(":")[1]);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, startMinute);
		return calendar;
	}

	/**
	 * changeAlarm() : set/cancel an alarm at given time
	 * 
	 * @param context
	 * @param calendar
	 * @param action
	 * @param cancel
	 * @param repeating
	 */
	private void changeAlarm(Context context, Calendar calendar, Action action,
			boolean cancel, boolean repeating) {
		Log.v(TAG, "changeAlarm");

		// Alarm Manager initialize
		AlarmManager manager = (AlarmManager) context
				.getSystemService(ALARM_SERVICE);
		Intent receiverIntent = new Intent(context,
				DoNotDisturbAlarmReceiver.class);
		receiverIntent.putExtra(DoNotDisturbActivity.ACTION, action);

		PendingIntent actionIntent = PendingIntent.getBroadcast(context,
				action.ordinal(), receiverIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		if (cancel) { // cancel alarm

			manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
					actionIntent);

			manager.cancel(actionIntent);

			Log.v(TAG, String.format("%s : %s", "cancelled alarm", calendar
					.getTime().toString()));

		} else { // create alarm

			if (repeating) { // scheduled

				manager.setRepeating(AlarmManager.RTC_WAKEUP,
						calendar.getTimeInMillis(), RECURRING_INTERVAL,
						actionIntent);

				Log.v(TAG, String.format("%s : %s", "created repeating alarm",
						calendar.getTime().toString()));

			} else { // one-time

				manager.set(AlarmManager.RTC_WAKEUP,
						calendar.getTimeInMillis(), actionIntent);

				Log.v(TAG, String.format("%s : %s", "created one-time alarm",
						calendar.getTime().toString()));

			}
		}
	}

	/**
	 * formatTime() : format the given time from input to output format
	 * 
	 * @param inputTime
	 * @param inputFormat
	 * @param outputFormat
	 * @return
	 */
	public static String formatTime(String inputTime, String inputFormat,
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
