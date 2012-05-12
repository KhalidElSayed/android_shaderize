package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class PrefsSeekBar extends LinearLayout implements
		SeekBar.OnSeekBarChangeListener {

	private int mDefaultValue;
	private Observer mObserver;
	private SharedPreferences mPrefs;
	private int mPrefsKey;

	public PrefsSeekBar(Context context) {
		super(context);
	}

	public PrefsSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (mObserver != null) {
			mPrefs.edit().putInt(getContext().getString(mPrefsKey), progress)
					.commit();
			mObserver.onSeekBarChanged(mPrefsKey,
					(float) progress / seekBar.getMax());
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	public void setDefaultValue(int defaultValue) {
		mDefaultValue = defaultValue;
	}

	public void setPrefs(SharedPreferences prefs, int prefsKey,
			Observer observer) {
		mPrefs = prefs;
		mPrefsKey = prefsKey;
		mObserver = observer;

		int progress = mPrefs.getInt(getContext().getString(prefsKey),
				mDefaultValue);
		SeekBar seekBar = (SeekBar) findViewById(R.id.prefs_seekbar_seekbar);
		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setProgress(progress);
	}

	public void setText(int resId) {
		TextView textView = (TextView) findViewById(R.id.prefs_seekbar_text);
		textView.setText(resId);
	}

	public interface Observer {
		public void onSeekBarChanged(int key, float value);
	}

}
