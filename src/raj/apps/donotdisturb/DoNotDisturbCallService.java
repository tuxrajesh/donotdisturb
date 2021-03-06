package raj.apps.donotdisturb;

import java.util.Calendar;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Service class to respond to call being received. This service is used to
 * ring/silence calls when DoNotDisturb mode is enabled.
 */
public class DoNotDisturbCallService extends IntentService {

	private final static String TAG = "DoNotDisturbCallService";

	/**
	 * Constructor
	 */
	public DoNotDisturbCallService() {
		super(TAG);
	}

	/**
	 * onHandleIntent(): get state and incoming number and ring/silence phone.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent");

		// get state and incoming number
		Bundle bundle = intent.getExtras();
		String state = bundle.getString(TelephonyManager.EXTRA_STATE);
		String incomingNumber = bundle
				.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

		Context context = getApplicationContext();

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);

		// get sharedPreferences from DoNotDisturbActivity
		Boolean enabled = sharedPref.getBoolean(
				DoNotDisturbActivity.KEY_PREF_MODE, false);
		Boolean scheduled = sharedPref.getBoolean(
				DoNotDisturbActivity.KEY_PREF_SCHEDULED, false);
		String fromTime = sharedPref.getString(
				DoNotDisturbActivity.KEY_PREF_FROM, "23:00");
		String toTime = sharedPref.getString(DoNotDisturbActivity.KEY_PREF_TO,
				"06:00");
		String allowCallsFrom = sharedPref.getString(
				DoNotDisturbActivity.KEY_ALLOW_CALLS_FROM, DoNotDisturbActivity.NO_ONE);

		// DoNotDisturb mode NOT_ENABLED, do nothing
		if (!enabled) {
			return;
		} else { // DoNotDisturb mode ENABLED

			// allowCallsFrom = No one; return
			if (allowCallsFrom.equalsIgnoreCase(DoNotDisturbActivity.NO_ONE)) {
				return;
			} else { // allowCallsFrom = User Selection

				// not scheduled;
				if (!scheduled) {

					setPhoneRingerMode(state, incomingNumber, allowCallsFrom);

				} else { // scheduled

					// get schedule to check if current time falls within
					// schedule
					Calendar currentCalendar = Calendar.getInstance();
					int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
					int currentMinute = currentCalendar.get(Calendar.MINUTE);
					currentCalendar.set(Calendar.HOUR_OF_DAY, currentHour);
					currentCalendar.set(Calendar.MINUTE, currentMinute);

					Calendar startCalendar = Calendar.getInstance();
					startCalendar.set(Calendar.HOUR_OF_DAY,
							Integer.parseInt(fromTime.split(":")[0]));
					startCalendar.set(Calendar.MINUTE,
							Integer.parseInt(fromTime.split(":")[1]));

					Calendar endCalendar = Calendar.getInstance();
					endCalendar.set(Calendar.HOUR_OF_DAY,
							Integer.parseInt(toTime.split(":")[0]));
					endCalendar.set(Calendar.MINUTE,
							Integer.parseInt(toTime.split(":")[1]));

					// Scenario #1: s = 23, e = 6, c = 2
					if (startCalendar.getTime().after(endCalendar.getTime())) {
						if (currentCalendar.getTime().before(
								endCalendar.getTime())) {

							setPhoneRingerMode(state, incomingNumber,
									allowCallsFrom);
						}
					} else { // Scenario #2: s = 3, e = 5, c = 4
						if (currentCalendar.after(startCalendar)
								&& endCalendar.after(currentCalendar)) {

							setPhoneRingerMode(state, incomingNumber,
									allowCallsFrom);
						}
					}
				}
			}
		}
	}

	/**
	 * setPhoneRingerMode() : sets the phone's ringer mode based on input
	 * passed.
	 * 
	 * @param state
	 * @param incomingNumber
	 * @param allowCallsFrom
	 */
	private void setPhoneRingerMode(String state, String incomingNumber,
			String allowCallsFrom) {

		// if state is RINGING, then worry about incoming number
		if (state.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {

			// if calls is allowed from number, ring
			if (isCallAllowedFrom(incomingNumber, allowCallsFrom)) {
				setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			} else { // call not allowed, silence
				setRingerMode(AudioManager.RINGER_MODE_SILENT);
			}
		} else { // state is not RINGING , silence
			setRingerMode(AudioManager.RINGER_MODE_SILENT);
		}
	}

	/**
	 * isCallAllowedFrom(): check if call is allowed from number based on user
	 * selection
	 * 
	 * @param incomingNumber
	 * @param allowCallsFrom
	 * @return
	 */
	private Boolean isCallAllowedFrom(String incomingNumber,
			String allowCallsFrom) {

		int allowCallsGroupId = 0;

		// get allowed group ID
		Cursor groupCursor = getContentResolver().query(Groups.CONTENT_URI,
				new String[] { Groups._ID, Groups.TITLE }, null, null, null);

		while (groupCursor.moveToNext()) {
			String groupTitle = groupCursor.getString(groupCursor
					.getColumnIndex(Groups._ID))
					+ " - "
					+ groupCursor.getString(groupCursor
							.getColumnIndex(Groups.TITLE));
			if (groupTitle.equalsIgnoreCase(allowCallsFrom)) {
				allowCallsGroupId = groupCursor.getInt(groupCursor
						.getColumnIndex(Groups._ID));
				break;
			}
		}

		// allows group is no-one; return
		if (allowCallsGroupId == 0) {
			return false;
		}

		// get contact with incoming number
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(incomingNumber));

		Cursor contactsCursor = getContentResolver().query(
				uri,
				new String[] { PhoneLookup.NUMBER, Contacts.LOOKUP_KEY,
						Contacts._ID }, null, null, null);

		while (contactsCursor.moveToNext()) {

			String contactLookupKey = contactsCursor.getString(contactsCursor
					.getColumnIndex(Contacts.LOOKUP_KEY));

			// if contact's group is allowed, return true
			Cursor contactGroup = getContentResolver().query(
					Data.CONTENT_URI,
					new String[] { GroupMembership.GROUP_ROW_ID },
					Data.LOOKUP_KEY + "=?" + " AND " + Data.MIMETYPE + "='"
							+ GroupMembership.CONTENT_ITEM_TYPE + "'",
					new String[] { contactLookupKey }, null);

			if (contactGroup.moveToNext()) {
				int contactGroupId = contactGroup.getInt(contactGroup
						.getColumnIndex(GroupMembership.GROUP_ROW_ID));
				if (contactGroupId == allowCallsGroupId) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * setRingerMode() : set AudioManager setting
	 * 
	 * @param ringerMode
	 */
	private void setRingerMode(int ringerMode) {
		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		manager.setRingerMode(ringerMode);
	}
}
