package org.freyja.libgdx.cocostudio.ui.widget.list;

import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Group;

public class CellHelper {
	public abstract class CellWrapper extends Group{
		protected CocoStudioUIEditor _editor;
		protected Group _group;
		protected int _idx;


		public void init(FileHandle file) {
			_editor = new CocoStudioUIEditor(file, null, null, null, null);
			_group = _editor.createGroup();
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
		
		public Group getGroup() {
			return _group;
		}
		/**
		 * 设置数据
		 * 记得调用init方法
		 * @param data
		 */
		public abstract void setData(Object data);
		
		/**
		 * 复制自身
		 */
		public abstract CellWrapper cloneCell();
	}
	
}
