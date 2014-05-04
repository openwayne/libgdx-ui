package org.freyja.libgdx.cocostudio.ui.widget.list;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public interface ICellWrapper {
	public Table parseJson();
	public void setData(Object data);
}
