package fi.harism.shaderize;

import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;

public class MainMenu {

	public static final int MENU_MAIN = 0;
	public static final int MENU_SHADER = 1;

	private static final int STATE_IN = 1;
	private static final int STATE_OUT = 2;

	private Button mButtonAbout;
	private Button mButtonPreferences;

	private Button mButtonShader;
	private final View mMenus[] = new View[2];
	private final int mStates[] = { STATE_OUT, STATE_OUT };

	MainMenu(View mainMenu, View shaderMenu) {
		mMenus[MENU_MAIN] = mainMenu;
		mMenus[MENU_SHADER] = shaderMenu;
		for (View view : mMenus) {
			view.setVisibility(View.GONE);
		}

		mButtonAbout = (Button) mainMenu.findViewById(R.id.button_about);
		mButtonAbout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog dialog = new Dialog(mMenus[0].getContext());
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.about);
				dialog.show();
				setVisible(MENU_MAIN, false);
			}
		});

		mButtonPreferences = (Button) mainMenu
				.findViewById(R.id.button_preferences);

		mButtonShader = (Button) mainMenu.findViewById(R.id.button_shader);
		mButtonShader.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setVisible(MENU_MAIN, false);
				setVisible(MENU_SHADER, true);
			}
		});
	}

	public boolean isVisible(int menu) {
		return mStates[menu] == STATE_IN;
	}

	public void setVisible(final int menu, boolean visible) {
		if (visible && mStates[menu] != STATE_IN) {
			mStates[menu] = STATE_IN;
			mMenus[menu].setVisibility(View.VISIBLE);
			AlphaAnimation anim = new AlphaAnimation(0f, 1f);
			mMenus[menu].setAnimation(anim);
			anim.setDuration(500);
			anim.startNow();
		} else if (!visible && mStates[menu] != STATE_OUT) {
			mStates[menu] = STATE_OUT;
			AlphaAnimation anim = new AlphaAnimation(1f, 0f);
			mMenus[menu].setAnimation(anim);
			anim.setDuration(500);
			anim.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationEnd(Animation anim) {
					mMenus[menu].setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation anim) {
				}

				@Override
				public void onAnimationStart(Animation anim) {
				}
			});
			anim.startNow();
			mMenus[menu].invalidate();
		}
	}

}
