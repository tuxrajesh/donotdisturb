package raj.apps.donotdisturb;

import java.util.Calendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

/**
 * TimePreference: Preference class used to provide a Time Picker setting
 */
public class TimePreference extends DialogPreference {

	TimePicker timePicker = null;
	final static String DEFAULT_VALUE = "21:00";
	String mTimePicker;

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
	 * getValue() : returns the value of the preference
	 * @return
	 */
	public String getValue(){
		return mTimePicker;
	}

	/**
	 * onDialogClosed() : If positive button was clicked, store the preference
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		
		if (positiveResult) {
			persistString(String.format("%d:%d", timePicker.getCurrentHour(), timePicker.getCurrentMinute()));
		}
	}
	
	/**
	 * onSetInitialValue() : Set the initial value, called on the create of the preference
	 */
	@Override
	protected void onSetInitialValue (boolean restorePersistedValue, Object defaultValue) {
		if(restorePersistedValue){
			mTimePicker = getPersistedString(DEFAULT_VALUE);
		} else {
			mTimePicker = (String)defaultValue;
			persistString(mTimePicker);
		}
	}
	
	/**
	 * onGetDefaultValue() : get the value of the preference
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
	    return a.getString(index);
	}
	
	/**
	 * Copied over from http://developer.android.com/guide/topics/ui/settings.html#CustomSave
	 */
	private static class SavedState extends BaseSavedState {
	    // Member that holds the setting's value
	    String value;

	    public SavedState(Parcelable superState) {
	        super(superState);
	    }

	    public SavedState(Parcel source) {
	        super(source);
	        // Get the current preference's value
	        value = source.readString();
	    }

	    @Override
	    public void writeToParcel(Parcel dest, int flags) {
	        super.writeToParcel(dest, flags);
	        // Write the preference's value
	        dest.writeString(value);
	    }	    
	}
	
	/**
	 * onSaveInstanceState() : save the preference
	 */
	@Override
	protected Parcelable onSaveInstanceState() {
	    final Parcelable superState = super.onSaveInstanceState();
	    // Check whether this Preference is persistent (continually saved)
	    if (isPersistent()) {
	        // No need to save instance state since it's persistent, use superclass state
	        return superState;
	    }

	    // Create instance of custom BaseSavedState
	    final SavedState myState = new SavedState(superState);
	    // Set the state's value with the class member that holds current setting value
	    myState.value = mTimePicker;
	    return myState;
	}

	/**
	 * onRestoreInstanceState() : restore the preference
	 */
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
	    // Check whether we saved the state in onSaveInstanceState
	    if (state == null || !state.getClass().equals(SavedState.class)) {
	        // Didn't save the state, so call superclass
	        super.onRestoreInstanceState(state);
	        return;
	    }

	    // Cast state to custom BaseSavedState and pass to superclass
	    SavedState myState = (SavedState) state;
	    super.onRestoreInstanceState(myState.getSuperState());
	    
	    // Set this Preference's widget to reflect the restored state
	    mTimePicker= myState.value;
	}
}
