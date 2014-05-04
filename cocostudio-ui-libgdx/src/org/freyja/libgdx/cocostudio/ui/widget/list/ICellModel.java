package org.freyja.libgdx.cocostudio.ui.widget.list;

import com.badlogic.gdx.scenes.scene2d.ui.Table;

public interface ICellModel {
	public void setData();
	public Table createCell();
}
