package fi.harism.shaderize;

import java.util.Vector;

public class ObjScene {

	private final Vector<Obj> mBoxes = new Vector<Obj>();

	public ObjScene() {
		ObjBox box = new ObjBox();
		box.setColor(Utils.rand(.5f, 1f), Utils.rand(.5f, 1f),
				Utils.rand(.5f, 1f));
		box.setSize(-10, -10, -10);
		mBoxes.add(box);

		for (int i = 0; i < 10; ++i) {
			box = new ObjBox();
			box.setColor(Utils.rand(.5f, 1f), Utils.rand(.5f, 1f),
					Utils.rand(.5f, 1f));
			box.setPosition(Utils.rand(-8f, 8f), Utils.rand(-8f, 8f),
					Utils.rand(-8f, 8f));
			mBoxes.add(box);
		}
	}

	public Vector<Obj> getBoxes() {
		return mBoxes;
	}

}
