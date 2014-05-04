package org.freyja.libgdx.cocostudio.ui.widget;

import org.freyja.libgdx.cocostudio.ui.widget.list.BaseListCell;
import org.freyja.libgdx.cocostudio.ui.widget.list.CellClickedListener.CellClickedEvent;
import org.freyja.libgdx.cocostudio.ui.widget.list.CellHelper.CellWrapper;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.SnapshotArray;

/**
 * 一个支持多列的列表 现在的优化不成功，消耗会大 默认每个格子的space是5
 * 
 * @author jianxiang.zi
 * 
 */
public class ListView extends Table implements Cullable {
	private ListStyle style;
	private Object[] items;
	private int selectedIndex;
	private Rectangle cullingArea;
	private float prefWidth, prefHeight;
	// row行数，希望一次看见的行数，实际上现在是通过遮挡来实现的,lines是根据数据算出来的行数
	private int col = 1;
	private int row = 1;
	private int lines;
	private int itemHeight = 0, itemWidth = 0;
	// 两个元素之间的垂直间距
	private float vSpace = 5;
	private float hSpace = 5;
	private Array<Integer> selectedIndecies;
	private boolean multiSelectable;
	private boolean selectable = true;
	private boolean isDirty = false;
	private boolean isSelectedDirty = false;
	/**
	 * 那几个cell的容器，这样用来保证cell是放在下面的
	 */
	private Table cellTable;
	private int hAlign = Align.center;

	public ListView(ListStyle style) {
		setStyle(style);
		selectedIndecies = new Array<Integer>();
		cellTable = new Table();
		cullingArea = new Rectangle();
		addActor(cellTable);

		addCaptureListener(new InputListener() {

			@Override
			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
			}

			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				if (pointer == 0 && button != 0)
					return false;
				return true;
			}
		});
	}

	int oldIndex;

	void touchUp(float x, float y) {
		oldIndex = selectedIndex;
		Array<Integer> oldIndecies = new Array<Integer>();
		System.arraycopy(selectedIndecies.items, 0, oldIndecies.items, 0,
				selectedIndecies.size);
		// 点中的行
		int r = (int) ((getHeight() - y) / (itemHeight + vSpace));
		// 点中的列
		int c = (int) (x / (itemWidth + hSpace));
		selectedIndex = r * col + c;
		if (selectedIndex < 0 || selectedIndex > items.length - 1) {
			selectedIndex = oldIndex;
			return;
		}
		// selectedIndex = Math.max(0, selectedIndex);
		// selectedIndex = Math.min(items.length - 1, selectedIndex);
		System.out.println("选中index:" + selectedIndex);
		if (this.multiSelectable) {
			// 多选情况下的处理
			if (selectedIndecies.contains(selectedIndex, true)) {
				selectedIndecies.removeValue(selectedIndex, true);
			} else {
				selectedIndecies.add(selectedIndex);
			}
		}
		ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
		if (fire(changeEvent)) {
			// 事件被取消，单选的时候回复原来的选择
			selectedIndex = oldIndex;
			if (multiSelectable) {
				selectedIndecies = oldIndecies;
			}
		}
		if (selectable) {
			// 更新那个选中状态
			isDirty = true;
		}
		Pools.free(changeEvent);
	}

	public void cellClicked(BaseListCell cell) {
		int oldIndex = selectedIndex;
		Array<Integer> oldIndecies = new Array<Integer>();
		oldIndecies.addAll(selectedIndecies);

		selectedIndex = cell.getIndex();

		if (this.multiSelectable) {
			// 多选情况下的处理
			if (selectedIndecies.contains(selectedIndex, true)) {
				selectedIndecies.removeValue(selectedIndex, true);
			} else {
				selectedIndecies.add(selectedIndex);
			}
		}
		ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
		if (fire(changeEvent)) {
			// 事件被取消，单选的时候回复原来的选择
			selectedIndex = oldIndex;
			if (multiSelectable) {
				System.arraycopy(oldIndecies.items, 0, selectedIndecies.items,
						0, oldIndecies.size);
			}
		}
		if (selectable) {
			// 更新那个选中状态
			isSelectedDirty = true;
		}
		Pools.free(changeEvent);

		CellClickedEvent clickEvent = Pools.obtain(CellClickedEvent.class);
		clickEvent.cell = cell;
		fire(clickEvent);
		Pools.free(clickEvent);
	}

	public void setMultiSelectable(boolean value) {
		this.multiSelectable = value;
		// 刚转换成多选的时候要清掉
		if (multiSelectable) {
			this.selectedIndecies.clear();
		} else if (items.length > 0) {
			// 由多选转换成单选的时候给个默认选中
			// this.setSelectedIndex(0);
		}
		isSelectedDirty = true;
	}

	public void setSelectable(boolean value) {
		this.selectable = value;
		isSelectedDirty = true;
	}

	public void setStyle(ListStyle style) {
		if (style == null)
			throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		this.pad(0);
		invalidateHierarchy();
	}

	/**
	 * Returns the list's style. Modifying the returned style may not have an
	 * effect until {@link #setStyle(ListStyle)} is called.
	 */
	public ListStyle getStyle() {
		return style;
	}

	/**
	 * 用了裁剪的方法
	 */
	private void updateItem2() {
		SnapshotArray<Actor> cellList = cellTable.getChildren();
		for (int i = 0; i < cellList.size; i++) {
			Actor tmp = cellList.get(i);

			float tmpY = getItemY(i);
			// 看不见的部分的优化处理
			if (cullingArea == null
					|| (tmpY < cullingArea.y + cullingArea.height && tmpY
							+ itemHeight > cullingArea.y)) {
				tmp.setVisible(true);
			} else if (tmpY + itemHeight < cullingArea.y) {
				tmp.setVisible(false);
				;
			}
		}
	}

	private void updateSelected() {
		for (Actor actor : cellTable.getChildren()) {
			if (!(actor instanceof BaseListCell)) {
				continue;
			}
			BaseListCell tmp = (BaseListCell) actor;
			if (selectable) {
				if (multiSelectable) {
					if (selectedIndecies.contains(tmp.getIndex(), true)) {
						tmp.setSelected(true);
					} else {
						tmp.setSelected(false);
					}
				} else if (tmp.getIndex() == selectedIndex) {
					tmp.setSelected(true);
				} else {
					tmp.setSelected(false);
				}
			}
		}
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		if (isDirty) {
			updateItem2();
			isDirty = false;
			isSelectedDirty = true;
		}
		if (isSelectedDirty) {
			updateSelected();
			isSelectedDirty = false;
		}
		super.draw(batch, parentAlpha);
	}

	/**
	 * 选中的数据
	 * 
	 * @return
	 */
	public Object[] getSelections() {
		Object[] retItems = new Object[selectedIndecies.size];
		for (int i = 0; i < retItems.length; i++) {
			retItems[i] = items[selectedIndecies.get(i)];
		}
		return retItems;
	}

	/**
	 * @return The index of the currently selected item. The top item has an
	 *         index of 0.
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}

	/**
	 * 设置选中的索引，允许小于0，小于0表示没有选中
	 * 
	 * @param index
	 */
	public void setSelectedIndex(int index) {
		if (index >= items.length)
			index = items.length - 1;
		oldIndex = selectedIndex;
		selectedIndex = index;
		isSelectedDirty = true;
	}

	public int getOldSelectIndex() {
		return oldIndex;
	}

	public Integer[] getSelectedIndecies() {
		return selectedIndecies.toArray(Integer.class);
	}

	public void setSelectedIndecies(Integer[] indecies) {
		selectedIndecies.clear();
		if (indecies.length > 0)
			selectedIndecies.addAll(indecies);
		isSelectedDirty = true;
	}

	public void setSelections(Object[] its) {
		selectedIndecies.clear();
		for (int i = 0, n = its.length; i < n; i++) {
			if (indexOf(items, its[i], true) >= 0) {
				selectedIndecies.add(i);
			}
		}
		isSelectedDirty = true;
	}

	/**
	 * @return 返回被选中的那个数据.
	 */
	public Object getSelection() {
		if (items.length == 0 || selectedIndex < 0)
			return null;
		return items[selectedIndex];
	}

	/** @return The index of the item that was selected, or -1. */
	public int setSelection(Object item) {
		selectedIndex = -1;
		for (int i = 0, n = items.length; i < n; i++) {
			if (items[i].equals(item)) {
				selectedIndex = i;
				break;
			}
		}
		isSelectedDirty = true;
		return selectedIndex;
	}

	public void setItems(Object[] objects, CellWrapper cell, int itemWidth, int itemHeight) {
		if (objects == null)
			throw new IllegalArgumentException("items cannot be null.");
		
		this.itemWidth = itemWidth;
		this.itemHeight = itemHeight;
		items = objects.clone();
		selectedIndex = 0;
		cellTable.clear();
		for (int i = 0; i < items.length; i++) {
			try {
				CellWrapper t = cell.clone();
				t.setIndex(i);
				t.setSize(itemWidth, itemHeight);
				t.setData(items[i]);
				t.setPosition(getItemX(i), getItemY(i));
				cellTable.addActor(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		isDirty = true;
		computeSize();
	}

	private void computeSize() {
		prefWidth = (itemWidth + hSpace) * col - hSpace;
		// 行数
		lines = (items.length + col - 1) / col;
		prefHeight = lines * itemHeight + (lines - 1) * vSpace;
		float mHeight = row * itemHeight + (row - 1) * vSpace;
		prefHeight = Math.max(prefHeight, mHeight);
		invalidateHierarchy();
	}

	/**
	 * 设置垂直间距
	 * 
	 * @param value
	 */
	public void setVSpace(float value) {
		vSpace = value;
		isDirty = true;
		computeSize();
	}

	/**
	 * 设置水平间距
	 * 
	 * @param value
	 */
	public void setHSpace(float value) {
		hSpace = value;
		isDirty = true;
		computeSize();
	}

	public void setColumn(int column) {
		this.col = column;
		isDirty = true;
		computeSize();
	}

	public void setRow(int row) {
		this.row = row;
		isDirty = true;
		computeSize();
	}

	public Object[] getItems() {
		return items;
	}

	public void setPreWidth(float w) {
		prefWidth = w;
		isDirty = true;
	}

	public void setPreHeight(float h) {
		prefHeight = h;
		isDirty = true;
	}

	public float getPrefWidth() {
		return prefWidth;
	}

	public float getPrefHeight() {
		return prefHeight;
	}

	public void setCullingArea(Rectangle culling) {
		if (cullingArea.x != culling.x || cullingArea.y != culling.y
				|| cullingArea.width != culling.width
				|| cullingArea.height != culling.height) {
			cullingArea.set(culling);
			isDirty = true;
		}
	}

	/**
	 * 获取被选中的cell的在列表中的y值（从左下角开始算起）
	 * 
	 * @return
	 */
	public float getSelectionY() {
		return getItemY(selectedIndex);
	}

	/**
	 * 获取被选中的格子的x值
	 * 
	 * @return
	 */
	public float getSelectionX() {
		return getItemX(selectedIndex);
	}

	/**
	 * 获取某个格子的y值
	 * 
	 * @param index
	 * @return
	 */
	private float getItemY(int index) {
		int line = index / col;
		float y = line * itemHeight;
		y += line * vSpace;
		return prefHeight - y - itemHeight;
	}

	/**
	 * 获取某个格子的x值
	 * 
	 * @param index
	 * @return
	 */
	private float getItemX(int index) {
		int c = index % col;
		float x = c * itemWidth;
		x += c * hSpace;
		switch (hAlign) {
		case Align.left:
			break;
		case Align.right:
			if (prefWidth < getWidth()) {
				x += getWidth() - prefWidth;
			}
			break;
		case Align.center:
		default:
			if (prefWidth < getWidth()) {
				x += (getWidth() - prefWidth) / 2;
			}
			break;
		}
		return x;
	}

	public void reflushData() {
		isDirty = true;
	}

	public void setHAlignment(int align) {
		this.hAlign = align;
	}


	/**
	 * The style for a list, see {@link List}.
	 * 
	 * @author mzechner
	 * @author Nathan Sweet
	 */
	static public class ListStyle {
		public Drawable selectionDrawable;
		public Drawable bgDrawable;

		public ListStyle() {

		}

		public ListStyle(ListStyle style) {
			this.selectionDrawable = style.selectionDrawable;
			this.bgDrawable = style.selectionDrawable;
		}

		public ListStyle(Drawable bg, Drawable select) {
			this.bgDrawable = bg;
			this.selectionDrawable = select;
		}
	}

	/**
	 * TODO 为了方便我把arrayUtils里的方法提过来了，等平稳了我去整理框架 TODO authby wayne 查找某个元素在数组中的位置
	 * 
	 * @param tItems
	 * @param value
	 * @param identity
	 * @return
	 */
	private <T> int indexOf(T[] tItems, T value, boolean identity) {
		if (identity || value == null) {
			for (int i = 0, n = tItems.length; i < n; i++)
				if (tItems[i] == value)
					return i;
		} else {
			for (int i = 0, n = tItems.length; i < n; i++)
				if (value.equals(tItems[i]))
					return i;
		}
		return -1;
	}
}
