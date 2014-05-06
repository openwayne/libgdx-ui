package org.freyja.libgdx.cocostudio.ui.widget;

import org.freyja.libgdx.cocostudio.ui.widget.list.CellWrapper;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

/**
 * 一个支持多列的列表 现在的优化不成功，消耗会大 默认每个格子的space是5
 * 
 * @author jianxiang.zi
 * 
 */
public class ListView extends ScrollPane {
	private ScrollPaneStyle style;
	private Object[] items = new Object[1];
	private Array<CellWrapper> cells = new Array<CellWrapper>();
	private int selectedIndex = 0;
	private float prefWidth, prefHeight;
	// row行数，希望一次看见的行数，实际上现在是通过遮挡来实现的,lines是根据数据算出来的行数
	private int col = 1;
	private int row = 1;
	private int lines;
	private int itemHeight = 0, itemWidth = 0;
	// 两个元素之间的垂直间距
	private float vSpace = 5;
	private float hSpace = 5;
	private boolean selectable = true;
	private boolean isDirty = false;
	private boolean isSelectedDirty = false;
	private int hAlign = Align.center;
	private Table cellTable;

	public ListView(ScrollPaneStyle style) {
		super(new Table(), style);
		cellTable = (Table) getWidget();
	}

	public void setSelectable(boolean value) {
		this.selectable = value;
		isSelectedDirty = true;
	}

	public void setStyle(ScrollPaneStyle style) {
		if (style == null)
			throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		invalidateHierarchy();
	}

	/**
	 * Returns the list's style. Modifying the returned style may not have an
	 * effect until {@link #setStyle(ListStyle)} is called.
	 */
	public ScrollPaneStyle getStyle() {
		return style;
	}

	private Vector2 hitPot = new Vector2();
	private Rectangle hitRect = new Rectangle();
	private int oldIndex;
	@Override
	public Actor hit(float x, float y, boolean touchable) {
		hitRect.set(0, 0, getWidth(), getHeight());
		if (touchable && getTouchable() == Touchable.disabled)
			return null;
		for (int i = 0; i < cells.size; i++) {
			CellWrapper child = cells.get(i);
			Actor dos = child.getGroup();
			if (!dos.isVisible() || !hitRect.contains(hitPot))
				continue;
			dos.parentToLocalCoordinates(hitPot.set(x, y));
			
			Actor hit = dos.hit(hitPot.x, hitPot.y, touchable);
			if (hit != null) {
				CellWrapper oldCell = cells.get(selectedIndex);
				oldCell.cancleCell();
				child.selectCell();
				selectedIndex = child.getIndex();
				return hit;
			}
		}

		return null;
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		if (isDirty) {
			updateItems();
			isDirty = false;
		}

		if (isSelectedDirty) {
			isSelectedDirty = false;
		}
		super.draw(batch, parentAlpha);
	}

	private void updateItems() {
		selectedIndex = 0;
		for (int i = 0; i < cells.size; i++) {
			try {
				CellWrapper t = cells.get(i);
				t.setIndex(i);
				t.setPosition(getItemX(i), getItemY(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		computeSize();
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

	public void setItems(Object[] objects, CellWrapper cell, int itemWidth,
			int itemHeight) {
		if (objects == null)
			throw new IllegalArgumentException("items cannot be null.");

		this.itemWidth = itemWidth;
		this.itemHeight = itemHeight;
		items = objects.clone();
		selectedIndex = 0;
		cellTable.clear();
		cells.clear();
		for (int i = 0; i < items.length; i++) {
			try {
				CellWrapper t = cell.cloneCell();
				t.setIndex(i);
				t.setData(items[i]);
				t.getGroup().setSize(itemWidth, itemHeight);
				t.setPosition(getItemX(i), getItemY(i));
				cellTable.addActor(t.getGroup());
				cells.add(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		isDirty = true;
		computeSize();
	}

	@Override
	public void setSize(float width, float height) {
		cellTable.setSize(width, height);
		super.setSize(width, height);
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
