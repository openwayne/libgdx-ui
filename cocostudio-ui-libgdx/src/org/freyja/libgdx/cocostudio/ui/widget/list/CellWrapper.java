package org.freyja.libgdx.cocostudio.ui.widget.list;

import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Group;

public abstract class CellWrapper {
	private CocoStudioUIEditor _editor;
	private Group _group;
	private int _idx;
	private boolean isSelected;

	public void init(FileHandle file) {
		this._editor = new CocoStudioUIEditor(file, null, null, null, null);
		this._group = this.getEditor().createGroup();
	}
	
	public CocoStudioUIEditor getEditor() {
		return _editor;
	}

	public void setPosition(float x, float y) {
		_group.setPosition(x, y);
	}

	public void setIndex(int idx) {
		_idx = idx;
	}

	public int getIndex() {
		return _idx;
	}

	public Group getGroup() {
		return _group;
	}

	/**
	 * 设置数据 记得调用init方法
	 * 
	 * @param data
	 */
	public abstract void setData(Object data);

	/**
	 * 复制自身
	 */
	public abstract CellWrapper cloneCell();

	public void selectCell() {
		System.out.println("点中" + _idx + "sell了");
		this.isSelected=true;
	}

	public void cancleCell() {
		System.out.println("取消" + _idx + "选中");
		this.isSelected=false;
	}

	public boolean isSelected() {
		return isSelected;
	}
}
