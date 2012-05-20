package fi.harism.shaderize;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final static int MENU_ID_MAIN = 0;
	private final static int MENU_ID_PREFS = 2;
	private final static int MENU_ID_SHADER = 1;
	private final static int MENU_IDS[] = { R.id.menu_main, R.id.menu_shader,
			R.id.menu_prefs };

	private final static StructRenderer SHADERS[] = {
			new StructRenderer(R.string.shader_flat_name,
					R.string.shader_flat_info, RendererFlat.class.getName()),
			new StructRenderer(R.string.shader_lightning_name,
					R.string.shader_lightning_info,
					RendererLightning.class.getName()),
			new StructRenderer(R.string.shader_rounded_name,
					R.string.shader_rounded_info,
					RendererRounded.class.getName()),
			new StructRenderer(R.string.shader_fxaa_name,
					R.string.shader_fxaa_info, RendererFxaa.class.getName()),
			new StructRenderer(R.string.shader_bloom_name,
					R.string.shader_bloom_info, RendererBloom.class.getName()),
			new StructRenderer(R.string.shader_dof_name,
					R.string.shader_dof_info, RendererDof.class.getName()),
			new StructRenderer(R.string.shader_hex_name,
					R.string.shader_hex_info, RendererHex.class.getName()) };

	private GLSurfaceView mGLSurfaceView;
	private MainRenderer mMainRenderer;

	private MainMenus mMenuHandler;
	private TextSwitcher mTextSwitcherInfo;
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
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Force full screen view.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		setContentView(R.layout.main);

		mTextViewTitle = (TextView) findViewById(R.id.text_title);
		mTextSwitcherInfo = (TextSwitcher) findViewById(R.id.text_info);

		AnimationSet textIn = new AnimationSet(true);
		textIn.addAnimation(new AlphaAnimation(0f, 1f));
		textIn.addAnimation(new TranslateAnimation(-200, 0, 0, 0));
		textIn.setDuration(1000);
		mTextSwitcherInfo.setInAnimation(textIn);

		AnimationSet textOut = new AnimationSet(true);
		textOut.addAnimation(new AlphaAnimation(1f, 0f));
		textOut.addAnimation(new TranslateAnimation(0, -200, 0, 0));
		textOut.setDuration(1000);
		mTextSwitcherInfo.setOutAnimation(textOut);

		View menus[] = new View[MENU_IDS.length];
		for (int i = 0; i < menus.length; ++i) {
			menus[i] = findViewById(MENU_IDS[i]);
		}
		mMenuHandler = new MainMenus(menus);

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
		for (StructRenderer shader : SHADERS) {
			TextView tv = (TextView) getLayoutInflater().inflate(
					R.layout.menu_button, viewGroup, false);
			tv.setId(shader.mTitleId);
			tv.setText(shader.mTitleId);
			tv.setOnClickListener(menuShaderListener);
			viewGroup.addView(tv);
		}

		mMainRenderer = new MainRenderer();
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

	private void setShader(StructRenderer shader) throws Exception {
		Renderer renderer = (Renderer) Class.forName(shader.mClassName)
				.newInstance();
		mTextSwitcherInfo.setText(getText(shader.mInfoId));

		ViewGroup content = (ViewGroup) findViewById(R.id.menu_prefs_content);
		content.removeAllViews();

		renderer.setContext(this);

		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		renderer.setPreferences(prefs, content);

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

	private static class StructRenderer {
		public String mClassName;
		public int mInfoId;
		public int mTitleId;

		public StructRenderer(int titleId, int infoId, String className) {
			mTitleId = titleId;
			mInfoId = infoId;
			mClassName = className;
		}
	}
}
