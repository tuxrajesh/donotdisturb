package raj.apps.donotdisturb;

import java.util.Calendar;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DoNotDisturbCallService extends IntentService {

	private final static String TAG = "DoNotDisturbCallService";

	public DoNotDisturbCallService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent");

		Bundle bundle = intent.getExtras();
		String state = bundle.getString(TelephonyManager.EXTRA_STATE);
		String incomingNumber = bundle
				.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

		Context context = getApplicationContext();
		SharedPreferences sharedPref = context.getSharedPreferences(
				DoNotDisturbActivity.PREFERENCE_NAME, Context.MODE_PRIVATE);

		String startTime = sharedPref.getString(
				DoNotDisturbActivity.START_TIME, "23:00");
		String endTime = sharedPref.getString(DoNotDisturbActivity.END_TIME,
				"06:00");
		Boolean enabled = sharedPref.getBoolean(
				DoNotDisturbActivity.ENABLED_FLAG, false);
		Boolean scheduled = sharedPref.getBoolean(
				DoNotDisturbActivity.SCHEDULED_FLAG, false);
		String allowCallsFrom = sharedPref.getString(
				DoNotDisturbActivity.ALLOW_CALLS_FROM, "No one");

		if (!enabled) {
			return;
		} else {
			if (allowCallsFrom == "No one") {
				return;
			} else {
				if (!scheduled) {
					if (state
							.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
						if (isCallAllowedFrom(incomingNumber, allowCallsFrom)) {
							setRingerMode(AudioManager.RINGER_MODE_NORMAL);
						} else {
							setRingerMode(AudioManager.RINGER_MODE_SILENT);
						}
					} else {
						setRingerMode(AudioManager.RINGER_MODE_SILENT);
					}
				} else {
					Calendar currentCalendar = Calendar.getInstance();
					int currentHour = currentCalendar.get(Calendar.HOUR_OF_DAY);
					int currentMinute = currentCalendar.get(Calendar.MINUTE);
					currentCalendar.set(Calendar.HOUR_OF_DAY, currentHour);
					currentCalendar.set(Calendar.MINUTE, currentMinute);

					Calendar startCalendar = Calendar.getInstance();
					startCalendar.set(Calendar.HOUR_OF_DAY,
							Integer.parseInt(startTime.split(":")[0]));
					startCalendar.set(Calendar.MINUTE,
							Integer.parseInt(startTime.split(":")[1]));

					Calendar endCalendar = Calendar.getInstance();
					endCalendar.set(Calendar.HOUR_OF_DAY,
							Integer.parseInt(endTime.split(":")[0]));
					endCalendar.set(Calendar.MINUTE,
							Integer.parseInt(endTime.split(":")[1]));

					if (currentCalendar.after(startCalendar)
							&& endCalendar.after(currentCalendar)) {

						if (state
								.equalsIgnoreCase(TelephonyManager.EXTRA_STATE_RINGING)) {
							if (isCallAllowedFrom(incomingNumber,
									allowCallsFrom)) {
								setRingerMode(AudioManager.RINGER_MODE_NORMAL);
							} else {
								setRingerMode(AudioManager.RINGER_MODE_SILENT);
							}
						} else {
							setRingerMode(AudioManager.RINGER_MODE_SILENT);
						}
					}
				}
			}
		}
	}

	private Boolean isCallAllowedFrom(String incomingNumber,
			String allowCallsFrom) {
		
		int allowCallsGroupId = -99;

		Cursor groupCursor = getContentResolver().query(Groups.CONTENT_URI,
				new String[] { Groups._ID, Groups.TITLE }, null, null, null);

		while (groupCursor.moveToNext()) {
			if (groupCursor.getString(groupCursor.getColumnIndex(Groups.TITLE)) == allowCallsFrom) {
				allowCallsGroupId = groupCursor.getInt(groupCursor
						.getColumnIndex(Groups._ID));
				break;
			}
		}

		Cursor contactsCursor = getContentResolver().query(Data.CONTENT_URI,
				new String[] { ContactsContract.Data.CONTACT_ID },
				GroupMembership.GROUP_ROW_ID + "=?",
				new String[] { Integer.toString(allowCallsGroupId) }, null);
		
		while(contactsCursor.moveToNext()){
			int contactId = contactsCursor.getInt(contactsCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
			
			Cursor phoneCursor = getContentResolver().query(CommonDataKinds.Phone.CONTENT_URI, null, CommonDataKinds.Phone.CONTACT_ID + "=?", new String[] { Integer.toString(contactId) }, null);
			
			while(phoneCursor.moveToNext()){
				String phone = phoneCursor.getString(phoneCursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
				
				if(phone.equals(incomingNumber))
					return true;
			}
		}

		return false;
	}

	private void setRingerMode(int ringerMode) {
		AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		manager.setRingerMode(ringerMode);
	}
}
