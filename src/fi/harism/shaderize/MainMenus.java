package fi.harism.shaderize;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

public class MainMenus {

	private static final int STATE_IN = 1;
	private static final int STATE_OUT = 2;

	private View mMenus[];
	private int mStates[];

	MainMenus(View menus[]) {
		mMenus = menus;
		mStates = new int[menus.length];
		for (int i = 0; i < mMenus.length; ++i) {
			mMenus[i].setVisibility(View.GONE);
			mStates[i] = STATE_OUT;
		}
	}

	public void hide() {
		for (int i = 0; i < mStates.length; ++i) {
			if (mStates[i] == STATE_IN) {
				setVisible(i, false, true);
			}
		}
	}

	public boolean isVisible() {
		boolean visible = false;
		for (int state : mStates) {
			visible |= state == STATE_IN;
		}
		return visible;
	}

	public boolean isVisible(int menu) {
		return mStates[menu] == STATE_IN;
	}

	public void setVisible(final int menu, boolean visible, boolean fadeRight) {
		if (visible && mStates[menu] != STATE_IN) {
			mStates[menu] = STATE_IN;
			mMenus[menu].setVisibility(View.VISIBLE);

			AnimationSet anim = new AnimationSet(true);
			anim.addAnimation(new AlphaAnimation(0f, 1f));
			if (fadeRight) {
				anim.addAnimation(new TranslateAnimation(100f, 0f, 0f, 0f));
			} else {
				anim.addAnimation(new TranslateAnimation(-100f, 0f, 0f, 0f));
			}

			mMenus[menu].setAnimation(anim);
			anim.setDuration(500);
			anim.startNow();
		} else if (!visible && mStates[menu] != STATE_OUT) {
			mStates[menu] = STATE_OUT;

			AnimationSet anim = new AnimationSet(true);
			anim.addAnimation(new AlphaAnimation(1f, 0f));
			if (fadeRight) {
				anim.addAnimation(new TranslateAnimation(0f, 100f, 0f, 0f));
			} else {
				anim.addAnimation(new TranslateAnimation(0f, -100f, 0f, 0f));
			}

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
