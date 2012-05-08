package fi.harism.shaderize;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Button mButtonMenu;
	private GLSurfaceView mGLSurfaceView;
	private MainMenu mMainMenu;

	private MainRenderer mMainRenderer;
	private TextView mTextViewInfo;
	private TextView mTextViewTitle;

	private Timer mTimerFramesPerSecond;

	@Override
	public void onBackPressed() {
		if (mMainMenu.isVisible(MainMenu.MENU_MAIN)) {
			mMainMenu.setVisible(MainMenu.MENU_MAIN, false);
		} else if (mMainMenu.isVisible(MainMenu.MENU_SHADER)) {
			mMainMenu.setVisible(MainMenu.MENU_MAIN, true);
			mMainMenu.setVisible(MainMenu.MENU_SHADER, false);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);

		mTextViewTitle = (TextView) findViewById(R.id.text_title);
		mTextViewInfo = (TextView) findViewById(R.id.text_info);
		mMainMenu = new MainMenu(findViewById(R.id.menu_main),
				findViewById(R.id.menu_shader));

		mButtonMenu = (Button) findViewById(R.id.button_menu);
		mButtonMenu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View button) {
				if (!mMainMenu.isVisible(MainMenu.MENU_MAIN)) {
					mMainMenu.setVisible(MainMenu.MENU_MAIN, true);
				} else {
					mMainMenu.setVisible(MainMenu.MENU_MAIN, false);
				}
				mMainMenu.setVisible(MainMenu.MENU_SHADER, false);
			}
		});

		mMainRenderer = new MainRenderer();
		mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setRenderer(mMainRenderer);
		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

		mTimerFramesPerSecond = new Timer();
		mTimerFramesPerSecond.schedule(new FramesPerSecondTask(), 0, 1000);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mTimerFramesPerSecond.cancel();
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		if (mMainMenu.isVisible(MainMenu.MENU_MAIN)) {
			mMainMenu.setVisible(MainMenu.MENU_MAIN, false);
			return true;
		} else if (mMainMenu.isVisible(MainMenu.MENU_SHADER)) {
			mMainMenu.setVisible(MainMenu.MENU_SHADER, false);
			return true;
		}
		return super.onTouchEvent(me);
	}

	private class FramesPerSecondTask extends TimerTask {
		@Override
		public void run() {
			if (Looper.myLooper() != Looper.getMainLooper()) {
				runOnUiThread(this);
				return;
			}

			String text = getResources().getString(R.string.title,
					mMainRenderer.getFramesPerSecond());
			mTextViewTitle.setText(text);
		}
	}
}
