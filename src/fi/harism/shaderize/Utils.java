package fi.harism.shaderize;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.Context;

public class Utils {

	public static final String loadRawResource(Context context, int resourceId)
			throws Exception {
		InputStream is = context.getResources().openRawResource(resourceId);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		return baos.toString();
	}

	public static final float rand(float min, float max) {
		return min + (float) (Math.random() * (max - min));
	}

}
