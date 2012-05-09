package fi.harism.shaderize;

import java.util.Vector;

public class ObjScene {

	private final Vector<Obj> mBoxes = new Vector<Obj>();

	public ObjScene() {
		ObjBox box = new ObjBox();
		box.setColor(Utils.rand(.5f, 1f), Utils.rand(.5f, 1f),
				Utils.rand(.5f, 1f));
		box.setSize(-10f, -10f, -10f);
		mBoxes.add(box);

		for (int i = 0; i < 100; ++i) {
			box = new ObjBox();
			box.setSize(.4f, .4f, .4f);
			box.setColor(Utils.rand(.5f, 1f), Utils.rand(.5f, 1f),
					Utils.rand(.5f, 1f));
			box.setPosition(Utils.rand(-4f, 4f), Utils.rand(-4f, 4f),
					Utils.rand(-4f, 4f));
			mBoxes.add(box);
		}

	}

	public Vector<Obj> getBoxes() {
		return mBoxes;
	}

}
