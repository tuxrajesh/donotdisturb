package raj.apps.donotdisturb;

import java.util.Calendar;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.widget.TimePicker;

/**
 * TimePickerFragment is a dialog fragment that can be re-used to allow picking time
 */
public class TimePickerFragment extends DialogFragment {

	public static String TAG = "TimePickerFragment";

	// onTimePickedListener
	private static OnTimePickedListener mOnTimePickedListener;

	/**
	 * onCreateDialog(): set the time as current time
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current time as the default values for the picker
		final Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), timeSetListener, hour,
				minute, false);
	}

	/**
	 * setOnTimePickedListener(): set listener for onTimePicked event
	 * @param listener
	 */
	public void setOnTimePickedListener(OnTimePickedListener listener) {
		mOnTimePickedListener = listener;
	}

	private OnTimeSetListener timeSetListener = new OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			mOnTimePickedListener.onTimePicked(hourOfDay, minute);
		}
	};

	/**
	 * Interface for the onTimePicked event and handler
	 */
	public interface OnTimePickedListener {
		public void onTimePicked(int hourOfDay, int minute);
	}

}