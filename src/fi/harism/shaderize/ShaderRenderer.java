package fi.harism.shaderize;

public interface ShaderRenderer {
	public void onDestroy();

	public void onDrawFrame();

	public void onSurfaceChanged(int width, int height);

	public void onSurfaceCreated();
}
