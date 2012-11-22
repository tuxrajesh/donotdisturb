package raj.apps.donotdisturb;

import java.util.Calendar;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

/**
 * TimePreference: Preference class used to provide a Time Picker setting
 */
public class TimePreference extends DialogPreference {

	private TimePicker timePicker = null;

	/**
	 * Constructor
	 * 
	 * @param context
	 * @param attrs
	 */
	public TimePreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setPositiveButtonText("Set");
		setNegativeButtonText("Cancel");

	}

	/**
	 * onCreateDialogView() : Create the TimePicker
	 */
	@Override
	public View onCreateDialogView() {
		timePicker = new TimePicker(getContext());
		return timePicker;
	}

	/**
	 * onBindDialogView() : Set the initial value as current time.
	 */
	@Override
	public void onBindDialogView(View view) {
		super.onBindDialogView(view);

		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);
		timePicker.setIs24HourView(false);
	}

	/**
	 * onDialogClosed() : If positive button was clicked, store the preference
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			persistString(String.format("%d:%d", timePicker.getCurrentHour(), timePicker.getCurrentMinute()));
		}
	}
}
