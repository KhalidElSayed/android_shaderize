package fi.harism.shaderize;

import android.content.Context;

public interface RendererChild {
	public void onDestroy();

	public void onDrawFrame(Fbo fbo, ObjScene scene);

	public void onSurfaceChanged(int width, int height) throws Exception;

	public void onSurfaceCreated(Context context) throws Exception;
}
