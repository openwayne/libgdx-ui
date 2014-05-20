package org.freyja.libgdx.cocostudio.ui.widget;

import org.freyja.libgdx.cocostudio.ui.widget.list.CellWrapper;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Array;

/**
 * A group that scrolls a child widget using scrollbars and/or mouse or touch
 * dragging.
 * <p>
 * The widget is sized to its preferred size. If the widget's preferred width or
 * height is less than the size of this scroll pane, it is set to the size of
 * this scroll pane. Scrollbars appear when the widget is larger than the scroll
 * pane.
 * <p>
 * The scroll pane's preferred size is that of the child widget. At this size,
 * the child widget will not need to scroll, so the scroll pane is typically
 * sized by ignoring the preferred size in one or both directions.
 * 
 * @author mzechner
 * @author Nathan Sweet
 */
public class ListView extends WidgetGroup {
	private ListViewStyle style;
	private Actor widget;

	final Rectangle scrollBounds = new Rectangle();
	private final Rectangle widgetAreaBounds = new Rectangle();
	private final Rectangle widgetCullingArea = new Rectangle();
	private final Rectangle scissorBounds = new Rectangle();

	boolean scrollX, scrollY;
	float maxX, maxY;
	final Vector2 lastPoint = new Vector2();
	float areaWidth, areaHeight;
	boolean cancelTouchFocus = true;

	boolean isDraged = false;

	float velocityX, velocityY;
	private float overscrollDistance = 0, overscrollSpeedMin = 30,
			overscrollSpeedMax = 200;

	public ListView() {
		this(new ListViewStyle());
	}

	public ListView(Skin skin) {
		this(skin.get(ListViewStyle.class));
	}

	public ListView(Skin skin, String styleName) {
		this(skin.get(styleName, ListViewStyle.class));
	}

	public ListView(ListViewStyle style) {
		if (style == null)
			throw new IllegalArgumentException("style cannot be null.");

		cellTable = new CellTable();
		cellTable.top().left().pad(5);

		this.style = style;
		setWidget(cellTable);
		setWidth(150);
		setHeight(150);

		addCaptureListener(new InputListener() {
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				isDraged = false;

				if (pointer == 0 && button != 0)
					return false;
				getStage().setScrollFocus(ListView.this);

				lastPoint.set(x, y);
				return true;
			}

			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				// TODO 检测是否要滚动还是回弹

				if (isDraged) {
					isDraged = false;
					return;
				}
				hitItem(x, y, selectable);
			}

			public void touchDragged(InputEvent event, float x, float y,
					int pointer) {
				isDraged = true;
				event.stop();
				float deltaX = 0;
				float deltaY = 0;

				if (scrollX) {
					deltaX = x - lastPoint.x;
					System.out.println("x : " + deltaX);
				}
				if (scrollY) {
					deltaY = y - lastPoint.y;
					System.out.println("y : " + deltaY);
				}

				lastPoint.set(x, y);
				setPos(deltaX, deltaY);
			}

		});

		addListener(new InputListener() {
			public boolean scrolled(InputEvent event, float x, float y,
					int amount) {
				if (scrollY)
					setPos(0, getMouseWheelY() * amount);
				else if (scrollX) //
					setPos(getMouseWheelX() * amount, 0);
				return true;
			}
		});
	}

	public void setStyle(ListViewStyle style) {
		if (style == null)
			throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		invalidateHierarchy();
	}

	/**
	 * Returns the scroll pane's style. Modifying the returned style may not
	 * have an effect until {@link #setStyle(ScrollPaneStyle)} is called.
	 */
	public ListViewStyle getStyle() {
		return style;
	}

	private void setPos(float px, float py) {
		float x = widget.getX();
		float y = widget.getY();
		if (scrollX) {
			x += px;
			// 需要判断是不是已经超过边界了,超过边界后最多overscrollDistance长度
			if (x > overscrollDistance) {
				x = overscrollDistance;
			} else if (x < -(maxX + overscrollDistance)) {
				x = -(maxX + overscrollDistance);
			}
		}

		if (scrollY) {
			y += py;
			if (y < -overscrollDistance) {
				y = -overscrollDistance;
			} else if (y > maxY + overscrollDistance) {
				y = maxY + overscrollDistance;
			}
		}

		System.out.println("widget x -> " + x + ", y -> " + y);
		widget.setPosition(x, y);
		scrollBounds.setPosition(x, y);
	}

	public void act(float delta) {
		super.act(delta);

		if (isSelectedDirty) {
			isSelectedDirty = false;
			CellWrapper cell = cells.get(selectedIndex);
			cell.selectCell();
		}

	}

	public void layout() {
		final Drawable bg = style.background;

		float bgLeftWidth = 0, bgRightWidth = 0, bgTopHeight = 0, bgBottomHeight = 0;
		if (bg != null) {
			bgLeftWidth = bg.getLeftWidth();
			bgRightWidth = bg.getRightWidth();
			bgTopHeight = bg.getTopHeight();
			bgBottomHeight = bg.getBottomHeight();
		}

		float width = getWidth();
		float height = getHeight();

		// Get available space size by subtracting background's padded area.
		areaWidth = width - bgLeftWidth - bgRightWidth;
		areaHeight = height - bgTopHeight - bgBottomHeight;

		if (widget == null)
			return;

		// Get widget's desired width.
		float widgetWidth, widgetHeight;
		if (widget instanceof Layout) {
			Layout layout = (Layout) widget;
			widgetWidth = layout.getPrefWidth();
			widgetHeight = layout.getPrefHeight();
		} else {
			widgetWidth = widget.getWidth();
			widgetHeight = widget.getHeight();
		}

		// Set the widget area bounds.

		// If the widget is smaller than the available space, make it take up
		// the available space.
		widgetWidth = Math.max(areaWidth, widgetWidth);
		widgetHeight = Math.max(areaHeight, widgetHeight);

		widgetAreaBounds
				.set(bgLeftWidth, bgBottomHeight, areaWidth, areaHeight);

		maxX = widgetWidth - areaWidth;
		maxY = widgetHeight - areaHeight;

		scrollBounds.set(cellTable.getX(), cellTable.getY(), areaWidth,
				areaHeight);

		widget.setSize(widgetWidth, widgetHeight);
		if (widget instanceof Layout)
			((Layout) widget).validate();
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		if (widget == null)
			return;

		// validate();

		// Setup transform for this group.
		applyTransform(batch, computeTransform());

		// Calculate the widget's position depending on the scroll state and
		// available widget area.

		if (widget instanceof Cullable) {
			widgetCullingArea.x = -widget.getX() + widgetAreaBounds.x;
			widgetCullingArea.y = -widget.getY() + widgetAreaBounds.y;
			widgetCullingArea.width = widgetAreaBounds.width;
			widgetCullingArea.height = widgetAreaBounds.height;
			((Cullable) widget).setCullingArea(widgetCullingArea);
		}

		// Caculate the scissor bounds based on the batch transform, the
		// available widget area and the camera transform. We need to
		// project those to screen coordinates for OpenGL ES to consume.
		getStage().calculateScissors(widgetAreaBounds, scissorBounds);

		// Draw the background ninepatch.
		Color color = getColor();
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (style.background != null)
			style.background.draw(batch, 0, 0, getWidth(), getHeight());
		batch.flush();

		// Enable scissors for widget area and draw the widget.
		if (ScissorStack.pushScissors(scissorBounds)) {
			drawChildren(batch, parentAlpha);
			ScissorStack.popScissors();
		}

		resetTransform(batch);
	}

	public float getPrefWidth() {
		if (widget instanceof Layout) {
			float width = ((Layout) widget).getPrefWidth();
			if (style.background != null)
				width += style.background.getLeftWidth()
						+ style.background.getRightWidth();
			return width;
		}
		return 150;
	}

	public float getPrefHeight() {
		if (widget instanceof Layout) {
			float height = ((Layout) widget).getPrefHeight();
			if (style.background != null)
				height += style.background.getTopHeight()
						+ style.background.getBottomHeight();
			return height;
		}
		return 150;
	}

	public float getMinWidth() {
		return 0;
	}

	public float getMinHeight() {
		return 0;
	}

	/**
	 * Sets the {@link Actor} embedded in this scroll pane.
	 * 
	 * @param widget
	 *            May be null to remove any current actor.
	 */
	public void setWidget(Actor widget) {
		if (widget == this)
			throw new IllegalArgumentException("widget cannot be same object");
		if (this.widget != null)
			super.removeActor(this.widget);
		this.widget = widget;
		if (widget != null)
			super.addActor(widget);
	}

	/** Returns the actor embedded in this scroll pane, or null. */
	public Actor getWidget() {
		return widget;
	}

	/** @deprecated */
	public void addActor(Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
	}

	/** @deprecated */
	public void addActorAt(int index, Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
	}

	/** @deprecated */
	public void addActorBefore(Actor actorBefore, Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
	}

	/** @deprecated */
	public void addActorAfter(Actor actorAfter, Actor actor) {
		throw new UnsupportedOperationException("Use ScrollPane#setWidget.");
	}

	public boolean removeActor(Actor actor) {
		if (actor != widget)
			return false;
		setWidget(null);
		return true;
	}

	/**
	 * Returns the amount to scroll horizontally when the mouse wheel is
	 * scrolled.
	 */
	protected float getMouseWheelX() {
		return Math.max(areaWidth * 0.9f, maxX * 0.1f) / 4;
	}

	/**
	 * Returns the amount to scroll vertically when the mouse wheel is scrolled.
	 */
	protected float getMouseWheelY() {
		return Math.max(areaHeight * 0.9f, maxY * 0.1f) / 4;
	}

	/** Returns the maximum scroll value in the x direction. */
	public float getMaxX() {
		return maxX;
	}

	/** Returns the maximum scroll value in the y direction. */
	public float getMaxY() {
		return maxY;
	}

	public boolean isScrollX() {
		return scrollX;
	}

	public boolean isScrollY() {
		return scrollY;
	}

	public void setVelocityX(float velocityX) {
		this.velocityX = velocityX;
	}

	public void setVelocityY(float velocityY) {
		this.velocityY = velocityY;
	}

	/** Gets the flick scroll y velocity. */
	public float getVelocityY() {
		return velocityY;
	}

	/**
	 * For flick scroll, sets the overscroll distance in pixels and the speed it
	 * returns to the widget's bounds in seconds. Default is 50, 30, 200.
	 */
	public void setupOverscroll(float distance, float speedMin, float speedMax) {
		overscrollDistance = distance;
		overscrollSpeedMin = speedMin;
		overscrollSpeedMax = speedMax;
	}

	/**
	 * Forces enabling scrollbars (for non-flick scroll) and overscrolling (for
	 * flick scroll) in a direction, even if the contents do not exceed the
	 * bounds in that direction.
	 */
	public void setForceScroll(boolean x, boolean y) {
		scrollX = x;
		scrollY = y;
	}

	public boolean isForceScrollX() {
		return scrollX;
	}

	public boolean isForceScrollY() {
		return scrollY;
	}

	/**
	 * When true (default), the {@link Stage#cancelTouchFocus()} touch focus} is
	 * cancelled when flick scrolling begins. This causes widgets inside the
	 * scrollpane that have received touchDown to receive touchUp when flick
	 * scrolling begins.
	 */
	public void setCancelTouchFocus(boolean cancelTouchFocus) {
		this.cancelTouchFocus = cancelTouchFocus;
	}

	/**
	 * The style for a scroll pane, see {@link ScrollPane}.
	 * 
	 * @author mzechner
	 * @author Nathan Sweet
	 */
	static public class ListViewStyle {
		/** Optional. */
		public Drawable background;

		public ListViewStyle() {
		}

		public ListViewStyle(Drawable background) {
			this.background = background;
		}

		public ListViewStyle(ListViewStyle style) {
			this.background = style.background;
		}
	}

	private Object[] items = new Object[1];
	private Array<CellWrapper> cells = new Array<CellWrapper>();
	private int selectedIndex = 0;
	private int itemHeight = 0, itemWidth = 0;

	public int getItemHeight() {
		return itemHeight;
	}

	public int getItemWidth() {
		return itemWidth;
	}

	private boolean isSelectedDirty = false;
	private CellTable cellTable;
	private boolean selectable = true;

	private boolean horv = true;
	private int v = 1;
	private int h = 1;

	/**
	 * 默认为1
	 * 
	 * @param col
	 */
	public void setCol(int col) {
		if (col <= v) {
			return;
		}
		v = col;
		layoutTable();
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
		cellTable.clear();
		cellTable.bottom().left();
		int half = (int) Math.ceil(cells.size / h);
		int idx = 0;
		for (int i = 0; i < cells.size; i++) {
			idx++;
			if (idx > half) {
				idx = 0;
				cellTable.row();
			}
			cellTable.add(cells.get(i).getGroup());
		}
		cellTable.pack();

	}

	private void layoutV(int v) {
		cellTable.clear();
		for (int i = 0; i < cells.size; i++) {
			if (i != 1 && i != 0 && i % v == 0) {
				cellTable.row();
			}
			cellTable.add(cells.get(i).getGroup());
			if (v == 1) {
				cellTable.row();
			}
		}
		cellTable.pack();
		// cellTable.setPosition(cellTable.getX(),
		// getHeight() - cellTable.getPrefHeight());
	}

	public Actor hit(float x, float y, boolean touchable) {
		if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
			return null;
		return super.hit(x, y, touchable);
	}

	private void layoutTable() {
		cellTable.clear();
		for (int i = 0; i < cells.size; i++) {
			if (i != 1 && i != 0 && i % v == 0) {
				cellTable.row();
			}
			cellTable.add(cells.get(i).getGroup());
			if (v == 1) {
				cellTable.row();
			}
		}
		cellTable.pack();
		// cellTable.setPosition(cellTable.getX(),
		// getHeight() - cellTable.getPrefHeight());

	}

	public void setSelectable(boolean value) {
		this.selectable = value;
		isSelectedDirty = true;
	}

	private Vector2 hitPot = new Vector2();
	private Vector2 listPot = new Vector2();
	private int oldIndex;

	private Actor hitItem(float x, float y, boolean touchable) {
		cellTable.parentToLocalCoordinates(listPot.set(x, y));

		Actor hitActor = cellTable.hitTest(listPot.x, listPot.y, touchable);

		if (hitActor == null) {
			return null;
		}
		if (touchable && getTouchable() == Touchable.disabled)
			return null;

		for (int i = 0; i < cells.size; i++) {
			CellWrapper child = cells.get(i);
			Actor dos = child.getGroup();
			if (hitActor.equals(dos) == false)
				continue;

			Actor hit = dos.hit(hitPot.x, hitPot.y, touchable);
			if (hit != null) {
				CellWrapper oldCell = cells.get(selectedIndex);
				if (oldCell.equals(child)) {
					if (oldCell.isSelected()) {
						oldCell.cancleCell();
					} else {
						oldCell.selectCell();
					}
				} else {
					oldCell.cancleCell();
					child.selectCell();
					selectedIndex = child.getIndex();
				}

				return hit;
			}
		}

		return null;
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
		items = objects;
		selectedIndex = 0;

		cells.clear();
		for (int i = 0; i < items.length; i++) {
			try {
				CellWrapper t = cell.cloneCell();
				t.setIndex(i);
				t.setData(items[i]);
				t.getGroup().setSize(itemWidth, itemHeight);
				cells.add(t);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		layoutTable();
	}

	@Override
	public void setSize(float width, float height) {
		cellTable.setSize(width, height);
		super.setSize(width, height);
	}

	public Object[] getItems() {
		return items;
	}

	private class CellTable extends Table {
		private Vector2 cPoint = new Vector2();

		public Actor hitTest(float x, float y, boolean touchable) {
			if (this.getClip()) {
				if (touchable && getTouchable() == Touchable.disabled)
					return null;
				if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
					return null;
			}

			if (touchable && getTouchable() == Touchable.disabled)
				return null;
			Array<Actor> children = this.getChildren();
			for (int i = children.size - 1; i >= 0; i--) {
				Actor child = children.get(i);
				if (!child.isVisible())
					continue;
				child.parentToLocalCoordinates(cPoint.set(x, y));
				Actor hit = null;

				if (child.getTouchable() == Touchable.enabled) {
					if (cPoint.x >= 0 && cPoint.x < child.getWidth()
							&& cPoint.y >= 0 && cPoint.y < child.getHeight()) {
						hit = child;
						child.hit(cPoint.x, cPoint.y, touchable);
					}
				}
				if (hit != null)
					return hit;
			}
			return null;
		}
	}
}
