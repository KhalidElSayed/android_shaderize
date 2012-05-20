package fi.harism.shaderize;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.ViewGroup;

public abstract class Renderer {

	public boolean mInitilized = false;

	public abstract void onDestroy();

	public abstract void onDrawFrame(ObjFbo fbo, ObjScene scene);

	public abstract void onSurfaceChanged(int width, int height)
			throws Exception;

	public abstract void onSurfaceCreated() throws Exception;

	public abstract void setContext(Context context);

	public abstract void setPreferences(SharedPreferences prefs,
			ViewGroup parent);
}
