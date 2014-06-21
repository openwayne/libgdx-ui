package org.freyja.libgdx.cocostudio.ui.widget;

import org.freyja.libgdx.cocostudio.ui.widget.list.CellWrapper;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

public class ListView extends ScrollPane {
	private CellTable container = new CellTable();
	private Object[] items;
	private Array<CellWrapper> cells = new Array<CellWrapper>();

	private int v = 1, h = 1;
	private boolean horv;
	private Vector2 clickPoint = new Vector2();
	private boolean isSelected = false;

	/**
	 * 一次可选中的数量 默认为1
	 */
	private int selectAmount = 1;
	private Array<CellWrapper> selectArray = new Array<CellWrapper>();

	/**
	 * 选中后是否可取消,默认取消
	 */
	private boolean isToggle = false;
	private int itemWidth;
	private int itemHeight;

	public ListView(ScrollPaneStyle style) {
		super(null, style);
		this.setWidget(container);
		container.pad(10).defaults().expandX().space(4);
		this.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				hitItem(x, y);
			}
		});

	}

	public void setItems(Object[] objects, CellWrapper cell, int itemWidth,
			int itemHeight) {
		if (objects == null)
			throw new IllegalArgumentException("items cannot be null.");
		items = objects;
		this.itemWidth = itemWidth;
		this.itemHeight = itemHeight;
		cells.clear();
		container.clear();
		for (int i = 0; i < items.length; i++) {
			CellWrapper t = cell.cloneCell();
			t.setIndex(i);
			t.setData(items[i]);
			t.getGroup().setSize(itemWidth, itemHeight);
			cells.add(t);
		}
	}

	/**
	 * 
	 * @param horv
	 *            横向优先设置为true，纵向优先设置为false
	 * @param v
	 *            纵向列数
	 * @param h
	 *            横向行数
	 */
	public void layoutCell(boolean horv, int v, int h) {
		this.horv = horv;
		this.v = v;
		this.h = h;
		if (horv) {
			// 横向
			layoutH(h);
		} else {
			// 纵向
			layoutV(v);
		}
	}

	/**
	 * 横向平铺
	 * 
	 * @param h
	 */
	private void layoutH(int h) {
		container.clear();
		container.bottom().left();
		int half = (int) Math.ceil(cells.size / h);
		int idx = 0;
		for (int i = 0; i < cells.size; i++) {
			idx++;
			if (idx > half) {
				idx = 0;
				container.row();
			}
			container.add(cells.get(i).getGroup());
		}

		int cellsWidth = cells.size * itemWidth;
		// 为了保证现实没那么操蛋,这里增加一个填充的东西
		if (cellsWidth < this.getWidth()) {
			container.add(new BlankTable(this.getWidth() - cellsWidth,
					itemHeight));
		}

		container.pack();

	}

	private void layoutV(int v) {
		container.clear();
		for (int i = 0; i < cells.size; i++) {
			if (i != 1 && i != 0 && i % v == 0) {
				container.row();
			}
			container.add(cells.get(i).getGroup());
			if (v == 1) {
				container.row();
			}
		}

		int cellsHeight = cells.size * itemHeight;
		// 为了保证现实没那么操蛋,这里增加一个填充的东西
		if (cellsHeight < this.getHeight()) {
			container.add(new BlankTable(itemWidth, this.getHeight()
					- cellsHeight));
		}
		container.pack();
	}

	public void setSelectedIndex(int idx) {
		// idx是从0开始
		if (idx <= 0) {
			idx = 0;
		}
		hitLogic(cells.get(idx));
		// 这里是个数,不是idx
		locate(idx);
	}

	private void locate(int idx) {
		if (horv) {
			// 横向 true，
			// 先考虑在最左边的情况
			int firstPageItems = (int) Math.floor(this.getWidth() / itemWidth);

			if (firstPageItems >= idx) {
				// 不用额外滚动
				scrollX(0);
				return;
			}

			// 目前只考虑最简单的情况,把那个移动到最后一个
			int itemEndPos = (idx + 1) * itemWidth + 100;
			int delta = (int) (itemEndPos - this.getWidth());
			scrollX(delta);
		} else {
			// 纵向 false
			// 先考虑在最左边的情况
			int firstPageItems = (int) Math
					.floor(this.getHeight() / itemHeight);

			if (firstPageItems >= idx) {
				// 不用额外滚动
				scrollY(0);
				return;
			}

			// 目前只考虑最简单的情况,把那个移动到最后一个
			int itemEndPos = (idx + 1) * itemHeight + 100;
			int delta = (int) (itemEndPos - this.getHeight());
			scrollY(delta);

		}
	}

	public Object getSelection() {
		return null;
	}

	private void hitItem(float x, float y) {
		clickPoint = container.parentToLocalCoordinates(clickPoint.set(x, y));

		Actor hitActor = container.hitTest(clickPoint.x, clickPoint.y);

		if (hitActor == null) {
			return;
		}

		CellWrapper hitCell = null;
		for (int i = 0; i < cells.size; i++) {
			CellWrapper child = cells.get(i);
			Actor dos = child.getGroup();
			if (hitActor.equals(dos) == true) {
				hitCell = child;
				break;
			}
		}

		if (hitCell != null)
			hitLogic(hitCell);

	}

	private void hitLogic(CellWrapper hitCell) {

		boolean isOld = false;
		for (int i = 0; i < selectArray.size; i++) {
			CellWrapper tmp = selectArray.get(i);
			if (tmp.equals(hitCell)) {
				isOld = true;

				if (isToggle()) {
					return;
				}

				if (tmp.isSelected()) {
					tmp.cancleCell();
					selectArray.removeIndex(i);
				} else {
					tmp.selectCell();
					selectArray.add(hitCell);
				}

				break;
			}
		}

		if (isOld == false) {
			if (selectArray.size < selectAmount) {
				hitCell.selectCell();
				selectArray.add(hitCell);
			} else {
				if (selectAmount == 1) {
					selectArray.removeIndex(0).cancleCell();
					hitCell.selectCell();
					selectArray.add(hitCell);
				} else {
					hitCell.full();
				}
			}
		}
	}

	public boolean isSelected() {
		return isSelected;
	}

	public int getSelectAmount() {
		return selectAmount;
	}

	/**
	 * 是否可选择,最大可选个数
	 * 
	 * @param isSelected
	 *            true 可选择 false 不可选择
	 * @param selectAmount
	 *            最大可选个数 默认为1
	 */
	public void setSelected(boolean isSelected, int selectAmount) {
		this.isSelected = isSelected;
		if (selectAmount <= 1) {
			selectAmount = 1;
		} else {
			this.selectAmount = selectAmount;
		}
	}

	public boolean isToggle() {
		return isToggle;
	}

	/**
	 * 选中后是否可取消,默认取消
	 * 
	 * @param isToggle
	 *            true 不可取消 false 可取消
	 */
	public void setToggle(boolean isToggle) {
		this.isToggle = isToggle;
	}

	private class BlankTable extends Actor {
		public BlankTable(float width, float height) {
			super();
			this.setSize(width, height);
		}
	}

	private class CellTable extends Table {
		private Vector2 cPoint = new Vector2();

		public Actor hitTest(float x, float y) {
			Array<Actor> children = this.getChildren();
			for (int i = children.size - 1; i >= 0; i--) {
				Actor child = children.get(i);
				if (!child.isVisible())
					continue;
				cPoint = child.parentToLocalCoordinates(cPoint.set(x, y));
				Actor hit = null;

				if (cPoint.x >= 0 && cPoint.x < child.getWidth()
						&& cPoint.y >= 0 && cPoint.y < child.getHeight()) {
					hit = child;
					child.hit(cPoint.x, cPoint.y, true);
				}

				if (hit != null) {
					if ((hit instanceof BlankTable) == true) {
						return null;
					}
					return hit;
				}
			}
			return null;
		}
	}
}
