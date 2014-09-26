package org.freyja.libgdx.cocostudio.ui.widget.list;

import java.util.Collection;

import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SnapshotArray;

public abstract class CellWrapper implements Disposable {
	private CocoStudioUIEditor _editor;
	private Group _group;
	private int _idx;
	private boolean isSelected;

	public void init(FileHandle file, Collection<TextureAtlas> textureAtlas) {
		this._editor = new CocoStudioUIEditor(file, null, null, null, textureAtlas);
		this._group = this.getEditor().createGroup();
		_group.setName(file.name());
	}
	
	public void init(FileHandle file) {
		init(file, null);
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

	public final Group getGroup() {
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
		this.isSelected = true;
	}

	public void cancleCell() {
		System.out.println("取消" + _idx + "选中");
		this.isSelected = false;
	}

	public void full() {
		System.out.println("Too many Cells were selected!!!");
	}

	public boolean isSelected() {
		return isSelected;
	}
	
	@Override
	public void dispose() {
		this._editor.dispose();
	}
}
