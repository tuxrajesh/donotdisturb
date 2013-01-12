package raj.apps.donotdisturb;

import java.util.ArrayList;

import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.provider.ContactsContract.Groups;

/**
 * Settings fragment for Do Not Disturb application
 */
public class DoNotDisturbSettingsFragment extends PreferenceFragment {

	private static final String KEY_ALLOW_CALLS_FROM = "pref_allow_calls_from";

	private ListPreference mAllowCallsFromList;

	/**
	 * onCreate() : add the Preferences resource with default values
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load preferences from XML resource
		addPreferencesFromResource(R.xml.preferences);

		// Set summary on the From and To Time
		TimePreference fromTime = (TimePreference)findPreference(DoNotDisturbActivity.KEY_PREF_FROM);
		fromTime.setSummary(DoNotDisturbActivity.formatTime(fromTime.getValue(), "hh:mm", "hh:mm a"));
		
		TimePreference toTime = (TimePreference)findPreference(DoNotDisturbActivity.KEY_PREF_TO);
		toTime.setSummary(DoNotDisturbActivity.formatTime(toTime.getValue(), "hh:mm", "hh:mm a"));

		// Setup entries for the list preference
		ArrayList<String> allowFrom = getAllowCallsList();
		mAllowCallsFromList = (ListPreference) findPreference(KEY_ALLOW_CALLS_FROM);

		CharSequence[] entries = allowFrom.toArray(new CharSequence[allowFrom
				.size()]);
		CharSequence[] entryValues = allowFrom
				.toArray(new CharSequence[allowFrom.size()]);

		if (mAllowCallsFromList != null) {

			mAllowCallsFromList.setEntries(entries);
			mAllowCallsFromList.setEntryValues(entryValues);

			if (mAllowCallsFromList.getValue() == null) {
				mAllowCallsFromList.setValueIndex(0);
			}

			mAllowCallsFromList.setSummary(mAllowCallsFromList.getValue()
					.toString());

			mAllowCallsFromList
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							mAllowCallsFromList.setSummary(mAllowCallsFromList
									.getEntries()[mAllowCallsFromList
									.findIndexOfValue(newValue.toString())]);
							return true;
						}
					});
		}
	}

	/**
	 * getAllowCallsList() : get the groups from the phone contacts
	 * 
	 * @return
	 */
	private ArrayList<String> getAllowCallsList() {
		ArrayList<String> contactGroups = new ArrayList<String>();

		contactGroups.add(DoNotDisturbActivity.NO_ONE);

		Cursor groupCursor = getActivity().getContentResolver().query(
				Groups.CONTENT_URI, new String[] { Groups._ID, Groups.TITLE },
				null, null, null);
		while (groupCursor.moveToNext()) {
			contactGroups.add(groupCursor.getString(groupCursor
					.getColumnIndex(Groups._ID))
					+ " - "
					+ groupCursor.getString(groupCursor
							.getColumnIndex(Groups.TITLE)));
		}

		return contactGroups;
	}
}
