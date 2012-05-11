package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class PrefsSeekBar implements SeekBar.OnSeekBarChangeListener {

	private Context mContext;
	private int mDefaultValue;
	private Observer mObserver;
	private SharedPreferences mPrefs;
	private int mPrefsKey;
	private View mView;

	public PrefsSeekBar(Context context, ViewGroup parent) {
		mContext = context;

		LayoutInflater inflater = LayoutInflater.from(mContext);
		mView = inflater.inflate(R.layout.prefs_seekbar, parent, false);

		SeekBar seekBar = (SeekBar) mView
				.findViewById(R.id.prefs_seekbar_seekbar);
		seekBar.setOnSeekBarChangeListener(this);
	}

	public View getView() {
		return mView;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (mObserver != null) {
			mPrefs.edit().putInt(mContext.getString(mPrefsKey), progress)
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

		int progress = mPrefs.getInt(mContext.getString(prefsKey),
				mDefaultValue);
		SeekBar seekBar = (SeekBar) mView
				.findViewById(R.id.prefs_seekbar_seekbar);
		seekBar.setProgress(progress);
	}

	public void setText(int resId) {
		TextView textView = (TextView) mView
				.findViewById(R.id.prefs_seekbar_text);
		textView.setText(resId);
	}

	public interface Observer {
		public void onSeekBarChanged(int key, float value);
	}

}
