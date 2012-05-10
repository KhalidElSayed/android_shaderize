package fi.harism.shaderize;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final static int MENU_ID_MAIN = 0;
	private final static int MENU_ID_PREFS = 2;
	private final static int MENU_ID_SHADER = 1;
	private final static int MENU_IDS[] = { R.id.menu_main, R.id.menu_shader,
			R.id.menu_prefs };

	private final static StructShader SHADERS[] = {
			new StructShader(R.string.shader_flat_name,
					R.string.shader_flat_info, R.layout.prefs_shader1,
					RendererFlat.class.getName()),
			new StructShader(R.string.shader_bloom_name,
					R.string.shader_bloom_info, R.layout.prefs_shader2,
					RendererBloom.class.getName()) };

	// private Button mButtonMenu;
	private GLSurfaceView mGLSurfaceView;
	private RendererMain mMainRenderer;

	private MenuHandler mMenuHandler;
	private TextView mTextViewInfo;
	private TextView mTextViewTitle;

	private Timer mTimerFramesPerSecond;

	@Override
	public void onBackPressed() {
		if (mMenuHandler.isVisible(MENU_ID_MAIN)) {
			mMenuHandler.setVisible(MENU_ID_MAIN, false, true);

			Button buttonMenu = (Button) findViewById(R.id.button_menu);
			AlphaAnimation animIn = new AlphaAnimation(0f, 1f);
			animIn.setDuration(500);
			buttonMenu.setAnimation(animIn);
			buttonMenu.setVisibility(View.VISIBLE);
			animIn.startNow();

			final Button buttonBack = (Button) findViewById(R.id.button_back);
			AlphaAnimation animOut = new AlphaAnimation(1f, 0f);
			animOut.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationEnd(Animation anim) {
					buttonBack.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation anim) {
				}

				@Override
				public void onAnimationStart(Animation anim) {
				}
			});
			animOut.setDuration(500);
			buttonBack.setAnimation(animOut);
			animOut.startNow();
			buttonBack.invalidate();

		} else if (mMenuHandler.isVisible(MENU_ID_SHADER)) {
			mMenuHandler.setVisible(MENU_ID_SHADER, false, true);
			mMenuHandler.setVisible(MENU_ID_MAIN, true, false);
		} else if (mMenuHandler.isVisible(MENU_ID_PREFS)) {
			mMenuHandler.setVisible(MENU_ID_PREFS, false, true);
			mMenuHandler.setVisible(MENU_ID_MAIN, true, false);
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

		View menus[] = new View[MENU_IDS.length];
		for (int i = 0; i < menus.length; ++i) {
			menus[i] = findViewById(MENU_IDS[i]);
		}
		mMenuHandler = new MenuHandler(menus);

		/**
		 * Setup Menu -button.
		 */
		Button button = (Button) findViewById(R.id.button_menu);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View button) {
				if (mMenuHandler.isVisible()) {
					onBackPressed();
				} else {
					mMenuHandler.setVisible(MENU_ID_MAIN, true, true);

					Button buttonBack = (Button) findViewById(R.id.button_back);
					AlphaAnimation animIn = new AlphaAnimation(0f, 1f);
					animIn.setDuration(500);
					buttonBack.setAnimation(animIn);
					buttonBack.setVisibility(View.VISIBLE);
					animIn.startNow();

					final Button buttonMenu = (Button) findViewById(R.id.button_menu);
					AlphaAnimation animOut = new AlphaAnimation(1f, 0f);
					animOut.setAnimationListener(new Animation.AnimationListener() {
						@Override
						public void onAnimationEnd(Animation anim) {
							buttonMenu.setVisibility(View.GONE);
						}

						@Override
						public void onAnimationRepeat(Animation anim) {
						}

						@Override
						public void onAnimationStart(Animation anim) {
						}
					});
					animOut.setDuration(500);
					buttonMenu.setAnimation(animOut);
					animOut.startNow();
					buttonMenu.invalidate();
				}
			}
		});
		button = (Button) findViewById(R.id.button_back);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View button) {
				if (mMenuHandler.isVisible()) {
					onBackPressed();
				}
			}
		});

		/**
		 * Setup Main menu buttons.
		 */
		button = (Button) findViewById(R.id.button_shader);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMenuHandler.setVisible(MENU_ID_MAIN, false, false);
				mMenuHandler.setVisible(MENU_ID_SHADER, true, true);
			}
		});
		button = (Button) findViewById(R.id.button_prefs);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMenuHandler.setVisible(MENU_ID_MAIN, false, false);
				mMenuHandler.setVisible(MENU_ID_PREFS, true, true);
			}
		});
		button = (Button) findViewById(R.id.button_about);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog dialog = new Dialog(MainActivity.this);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.about);
				dialog.show();
				onBackPressed();
			}
		});
		button = (Button) findViewById(R.id.button_quit);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		/**
		 * Generate shader menu.
		 */
		MenuShaderListener menuShaderListener = new MenuShaderListener();
		ViewGroup viewGroup = (ViewGroup) findViewById(R.id.menu_shader_content);
		for (StructShader shader : SHADERS) {
			TextView tv = (TextView) getLayoutInflater().inflate(
					R.layout.menu_button, viewGroup, false);
			tv.setId(shader.mTitleId);
			tv.setText(shader.mTitleId);
			tv.setOnClickListener(menuShaderListener);
			viewGroup.addView(tv);
		}

		mMainRenderer = new RendererMain();
		mMainRenderer.setContext(this);

		mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsurfaceview);
		mGLSurfaceView.setEGLContextClientVersion(2);
		mGLSurfaceView.setRenderer(mMainRenderer);
		mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

		mTimerFramesPerSecond = new Timer();
		mTimerFramesPerSecond.schedule(new FramesPerSecondTask(), 0, 1000);

		try {
			SharedPreferences prefs = getPreferences(MODE_PRIVATE);
			int shaderValue = prefs.getInt(
					getString(R.string.prefs_key_shader), 0);
			setShader(SHADERS[shaderValue]);
		} catch (Exception ex) {
			Toast.makeText(this, "Unable instantiate shader.",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mTimerFramesPerSecond.cancel();
		mMainRenderer.setContext(null);
	}

	private void setShader(StructShader shader) throws Exception {
		RendererFilter renderer = (RendererFilter) Class.forName(
				shader.mClassName).newInstance();
		mTextViewInfo.setText(shader.mInfoId);

		ViewGroup scroll = (ViewGroup) findViewById(R.id.menu_prefs_content);
		scroll.removeAllViews();
		View view = getLayoutInflater().inflate(shader.mPrefsId, scroll, false);
		scroll.addView(view);

		mMainRenderer.setRendererFilter(renderer);
	}

	private class FramesPerSecondTask extends TimerTask {
		@Override
		public void run() {
			if (Looper.myLooper() != Looper.getMainLooper()) {
				runOnUiThread(this);
				return;
			}

			String text = getString(R.string.title,
					mMainRenderer.getFramesPerSecond());
			mTextViewTitle.setText(text);
		}
	}

	private class MenuShaderListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			try {
				for (int i = 0; i < SHADERS.length; ++i) {
					if (v.getId() == SHADERS[i].mTitleId) {
						setShader(SHADERS[i]);

						SharedPreferences prefs = getPreferences(MODE_PRIVATE);
						prefs.edit()
								.putInt(getString(R.string.prefs_key_shader), i)
								.commit();

						return;
					}
				}
			} catch (Exception ex) {
				Toast.makeText(MainActivity.this,
						"Unable to instantiate shader.", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private static class StructShader {
		public String mClassName;
		public int mInfoId;
		public int mPrefsId;
		public int mTitleId;

		public StructShader(int titleId, int infoId, int prefsId,
				String className) {
			mTitleId = titleId;
			mInfoId = infoId;
			mPrefsId = prefsId;
			mClassName = className;
		}
	}
}
