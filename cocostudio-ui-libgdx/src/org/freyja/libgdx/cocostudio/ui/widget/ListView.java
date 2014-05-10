package org.freyja.libgdx.cocostudio.ui.widget;

import org.freyja.libgdx.cocostudio.ui.widget.list.CellWrapper;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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
	private ActorGestureListener flickScrollListener;

	boolean scrollX, scrollY;
	float amountX, amountY;
	float visualAmountX, visualAmountY;
	float maxX, maxY;
	boolean touchScrollH, touchScrollV;
	final Vector2 lastPoint = new Vector2();
	float areaWidth, areaHeight;
	private boolean smoothScrolling = true;
	float fadeAlpha, fadeAlphaSeconds = 1, fadeDelay, fadeDelaySeconds = 1;
	boolean cancelTouchFocus = true;

	boolean flickScroll = true;
	float velocityX, velocityY;
	float flingTimer;
	private boolean overscrollX = true, overscrollY = true;
	float flingTime = 1f;
	private float overscrollDistance = 50, overscrollSpeedMin = 30,
			overscrollSpeedMax = 200;
	private boolean forceScrollX, forceScrollY;
	private boolean disableX, disableY;
	private boolean clamp = true;
	int draggingPointer = -1;

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

		cellTable = new Table();
		cellTable.top().left().pad(5);

		this.style = style;
		setWidget(cellTable);
		setWidth(150);
		setHeight(150);

		addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				hitItem(x, y, selectable);
				super.clicked(event, x, y);
			}
		});

		addCaptureListener(new InputListener() {
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				if (draggingPointer != -1)
					return false;
				if (pointer == 0 && button != 0)
					return false;
				getStage().setScrollFocus(ListView.this);

				if (!flickScroll)
					resetFade();

				if (fadeAlpha == 0)
					return false;

				if (scrollX && scrollBounds.contains(x, y)) {
					resetFade();
					touchScrollH = true;
					draggingPointer = pointer;
					// setScrollX(amountX + areaWidth * (x < 0 ? -1 : 1));
				}
				if (scrollY && scrollBounds.contains(x, y)) {
					resetFade();
					touchScrollV = true;
					draggingPointer = pointer;
					// setScrollY(amountY + areaHeight * (y < 0 ? 1 : -1));
				}
				lastPoint.set(x, y);
				return true;
			}

			public void touchUp(InputEvent event, float x, float y,
					int pointer, int button) {
				if (pointer != draggingPointer)
					return;
				cancel();
			}

			public void touchDragged(InputEvent event, float x, float y,
					int pointer) {
				event.stop();

				if (pointer != draggingPointer)
					return;
				if (touchScrollH) {
					float delta = x - lastPoint.x;

					// setScrollPercentX((delta - cellTable.getX()) / maxX);
					scrollX(-delta);
				}
				if (touchScrollV) {
					float delta = y - lastPoint.y;
					// setScrollPercentY(1 - ((delta - cellTable.getY()) /
					// maxY));
					scrollY(delta);
				}
				lastPoint.set(x, y);
			}

			public boolean mouseMoved(InputEvent event, float x, float y) {
				if (!flickScroll)
					resetFade();
				return false;
			}
		});

		flickScrollListener = new ActorGestureListener() {
			public void pan(InputEvent event, float x, float y, float deltaX,
					float deltaY) {
				resetFade();
				amountX -= deltaX;
				amountY += deltaY;
				clamp();
				cancelTouchFocusedChild(event);
			}

			public void fling(InputEvent event, float x, float y, int button) {
				if (Math.abs(x) > 150) {
					flingTimer = flingTime;
					velocityX = x;
					cancelTouchFocusedChild(event);
				}
				if (Math.abs(y) > 150) {
					flingTimer = flingTime;
					velocityY = -y;
					cancelTouchFocusedChild(event);
				}
			}

			public boolean handle(Event event) {
				if (super.handle(event)) {
					if (((InputEvent) event).getType() == InputEvent.Type.touchDown)
						flingTimer = 0;
					return true;
				}
				return false;
			}
		};
		addListener(flickScrollListener);

		addListener(new InputListener() {
			public boolean scrolled(InputEvent event, float x, float y,
					int amount) {
				resetFade();
				if (scrollY)
					setScrollY(amountY + getMouseWheelY() * amount);
				else if (scrollX) //
					setScrollX(amountX + getMouseWheelX() * amount);
				return true;
			}
		});
	}

	void resetFade() {
		fadeAlpha = fadeAlphaSeconds;
		fadeDelay = fadeDelaySeconds;
	}

	void cancelTouchFocusedChild(InputEvent event) {
		if (!cancelTouchFocus)
			return;
		Stage stage = getStage();
		if (stage != null)
			stage.cancelTouchFocus(flickScrollListener, this);
	}

	/** If currently scrolling by tracking a touch down, stop scrolling. */
	public void cancel() {
		draggingPointer = -1;
		touchScrollH = false;
		touchScrollV = false;
		flickScrollListener.getGestureDetector().cancel();
	}

	void clamp() {
		if (!clamp)
			return;
		scrollX(overscrollX ? MathUtils.clamp(amountX, -overscrollDistance,
				maxX + overscrollDistance) : MathUtils.clamp(amountX, 0, maxX));
		scrollY(overscrollY ? MathUtils.clamp(amountY, -overscrollDistance,
				maxY + overscrollDistance) : MathUtils.clamp(amountY, 0, maxY));
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

	public void act(float delta) {
		super.act(delta);

		if (isSelectedDirty) {
			isSelectedDirty = false;
			CellWrapper cell = cells.get(selectedIndex);
			cell.selectCell();
		}

		boolean panning = flickScrollListener.getGestureDetector().isPanning();

		if (fadeAlpha > 0 && !panning && !touchScrollH && !touchScrollV) {
			fadeDelay -= delta;
			if (fadeDelay <= 0)
				fadeAlpha = Math.max(0, fadeAlpha - delta);
		}

		if (flingTimer > 0) {
			resetFade();

			float alpha = flingTimer / flingTime;
			amountX -= velocityX * alpha * delta;
			amountY -= velocityY * alpha * delta;
			clamp();

			// Stop fling if hit overscroll distance.
			if (amountX == -overscrollDistance)
				velocityX = 0;
			if (amountX >= maxX + overscrollDistance)
				velocityX = 0;
			if (amountY == -overscrollDistance)
				velocityY = 0;
			if (amountY >= maxY + overscrollDistance)
				velocityY = 0;

			flingTimer -= delta;
			if (flingTimer <= 0) {
				velocityX = 0;
				velocityY = 0;
			}
		}

		if (smoothScrolling && flingTimer <= 0 && !touchScrollH
				&& !touchScrollV && !panning) {
			if (visualAmountX != amountX) {
				if (visualAmountX < amountX)
					visualScrollX(Math.min(
							amountX,
							visualAmountX
									+ Math.max(150 * delta,
											(amountX - visualAmountX) * 5
													* delta)));
				else
					visualScrollX(Math.max(
							amountX,
							visualAmountX
									- Math.max(150 * delta,
											(visualAmountX - amountX) * 5
													* delta)));
			}
			if (visualAmountY != amountY) {
				if (visualAmountY < amountY)
					visualScrollY(Math.min(
							amountY,
							visualAmountY
									+ Math.max(150 * delta,
											(amountY - visualAmountY) * 5
													* delta)));
				else
					visualScrollY(Math.max(
							amountY,
							visualAmountY
									- Math.max(150 * delta,
											(visualAmountY - amountY) * 5
													* delta)));
			}
		} else {
			if (visualAmountX != amountX)
				visualScrollX(amountX);
			if (visualAmountY != amountY)
				visualScrollY(amountY);
		}

		if (!panning) {
			if (overscrollX && scrollX) {
				if (amountX < 0) {
					resetFade();
					amountX += (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin)
							* -amountX / overscrollDistance)
							* delta;
					if (amountX > 0)
						scrollX(0);
				} else if (amountX > maxX) {
					resetFade();
					amountX -= (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin)
							* -(maxX - amountX) / overscrollDistance)
							* delta;
					if (amountX < maxX)
						scrollX(maxX);
				}
			}
			if (overscrollY && scrollY) {
				if (amountY < 0) {
					resetFade();
					amountY += (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin)
							* -amountY / overscrollDistance)
							* delta;
					if (amountY > 0)
						scrollY(0);
				} else if (amountY > maxY) {
					resetFade();
					amountY -= (overscrollSpeedMin + (overscrollSpeedMax - overscrollSpeedMin)
							* -(maxY - amountY) / overscrollDistance)
							* delta;
					if (amountY < maxY)
						scrollY(maxY);
				}
			}
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

		// Determine if horizontal/vertical scrollbars are needed.
		scrollX = forceScrollX || (widgetWidth > areaWidth && !disableX);
		scrollY = forceScrollY || (widgetHeight > areaHeight && !disableY);

		// Set the widget area bounds.
		widgetAreaBounds
				.set(bgLeftWidth, bgBottomHeight, areaWidth, areaHeight);

		// If the widget is smaller than the available space, make it take up
		// the available space.
		widgetWidth = disableX ? width : Math.max(areaWidth, widgetWidth);
		widgetHeight = disableY ? height : Math.max(areaHeight, widgetHeight);

		maxX = widgetWidth - areaWidth;
		maxY = widgetHeight - areaHeight;

		scrollX(MathUtils.clamp(amountX, 0, maxX));
		scrollY(MathUtils.clamp(amountY, 0, maxY));

		scrollBounds.set(cellTable.getX(), cellTable.getY(),
				cellTable.getWidth(), cellTable.getHeight());

		widget.setSize(widgetWidth, widgetHeight);
		if (widget instanceof Layout)
			((Layout) widget).validate();
	}

	@Override
	public void draw(SpriteBatch batch, float parentAlpha) {
		if (widget == null)
			return;

		validate();

		// Setup transform for this group.
		applyTransform(batch, computeTransform());

		// Calculate the widget's position depending on the scroll state and
		// available widget area.
		float y = widgetAreaBounds.y;
		if (!scrollY)
			y -= (int) maxY;
		else
			y -= (int) (maxY - visualAmountY);

		float x = widgetAreaBounds.x;
		if (scrollX)
			x -= (int) visualAmountX;
		widget.setPosition(x, y);
		scrollBounds.setPosition(x, y);

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

		// Render scrollbars and knobs on top.
		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha
				* Interpolation.fade.apply(fadeAlpha / fadeAlphaSeconds));

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

	public Actor hit(float x, float y, boolean touchable) {
		if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight())
			return null;
		if (scrollX && scrollBounds.contains(x, y))
			return this;
		if (scrollY && scrollBounds.contains(x, y))
			return this;
		return super.hit(x, y, touchable);
	}

	/** Called whenever the x scroll amount is changed. */
	protected void scrollX(float pixelsX) {
		this.amountX = pixelsX;
	}

	/** Called whenever the y scroll amount is changed. */
	protected void scrollY(float pixelsY) {
		this.amountY = pixelsY;
	}

	/** Called whenever the visual x scroll amount is changed. */
	protected void visualScrollX(float pixelsX) {
		this.visualAmountX = pixelsX;
	}

	/** Called whenever the visual y scroll amount is changed. */
	protected void visualScrollY(float pixelsY) {
		this.visualAmountY = pixelsY;
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

	public void setScrollX(float pixels) {
		scrollX(MathUtils.clamp(pixels, 0, maxX));
	}

	/** Returns the x scroll position in pixels. */
	public float getScrollX() {
		return amountX;
	}

	public void setScrollY(float pixels) {
		scrollY(MathUtils.clamp(pixels, 0, maxY));
	}

	/** Returns the y scroll position in pixels. */
	public float getScrollY() {
		return amountY;
	}

	/**
	 * Sets the visual scroll amount equal to the scroll amount. This can be
	 * used when setting the scroll amount without animating.
	 */
	public void updateVisualScroll() {
		visualAmountX = amountX;
		visualAmountY = amountY;
	}

	public float getVisualScrollX() {
		return !scrollX ? 0 : visualAmountX;
	}

	public float getVisualScrollY() {
		return !scrollY ? 0 : visualAmountY;
	}

	public float getScrollPercentX() {
		return MathUtils.clamp(amountX / maxX, 0, 1);
	}

	public void setScrollPercentX(float percentX) {
		scrollX(maxX * MathUtils.clamp(percentX, 0, 1));
		System.out.println(percentX);
	}

	public float getScrollPercentY() {
		return MathUtils.clamp(amountY / maxY, 0, 1);
	}

	public void setScrollPercentY(float percentY) {
		scrollY(maxY * MathUtils.clamp(percentY, 0, 1));
	}

	public void setFlickScroll(boolean flickScroll) {
		if (this.flickScroll == flickScroll)
			return;
		this.flickScroll = flickScroll;
		if (flickScroll)
			addListener(flickScrollListener);
		else
			removeListener(flickScrollListener);
		invalidate();
	}

	/**
	 * Sets the scroll offset so the specified rectangle is fully in view, if
	 * possible. Coordinates are in the scroll pane widget's coordinate system.
	 */
	public void scrollTo(float x, float y, float width, float height) {
		float amountX = this.amountX;
		if (x + width > amountX + areaWidth)
			amountX = x + width - areaWidth;
		if (x < amountX)
			amountX = x;
		scrollX(MathUtils.clamp(amountX, 0, maxX));

		float amountY = this.amountY;
		if (amountY > maxY - y - height + areaHeight)
			amountY = maxY - y - height + areaHeight;
		if (amountY < maxY - y)
			amountY = maxY - y;
		scrollY(MathUtils.clamp(amountY, 0, maxY));
	}

	/**
	 * Sets the scroll offset so the specified rectangle is fully in view and
	 * centered vertically in the scroll pane, if possible. Coordinates are in
	 * the scroll pane widget's coordinate system.
	 */
	public void scrollToCenter(float x, float y, float width, float height) {
		float amountX = this.amountX;
		if (x + width > amountX + areaWidth)
			amountX = x + width - areaWidth;
		if (x < amountX)
			amountX = x;
		scrollX(MathUtils.clamp(amountX, 0, maxX));

		float amountY = this.amountY;
		float centerY = maxY - y + areaHeight / 2 - height / 2;
		if (amountY < centerY - areaHeight / 4
				|| amountY > centerY + areaHeight / 4)
			amountY = centerY;
		scrollY(MathUtils.clamp(amountY, 0, maxY));
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

	/**
	 * Disables scrolling in a direction. The widget will be sized to the
	 * FlickScrollPane in the disabled direction.
	 */
	public void setScrollingDisabled(boolean x, boolean y) {
		disableX = x;
		disableY = y;
	}

	public boolean isDragging() {
		return draggingPointer != -1;
	}

	public boolean isPanning() {
		return flickScrollListener.getGestureDetector().isPanning();
	}

	public boolean isFlinging() {
		return flingTimer > 0;
	}

	public void setVelocityX(float velocityX) {
		this.velocityX = velocityX;
	}

	/** Gets the flick scroll y velocity. */
	public float getVelocityX() {
		if (flingTimer <= 0)
			return 0;
		float alpha = flingTimer / flingTime;
		alpha = alpha * alpha * alpha;
		return velocityX * alpha * alpha * alpha;
	}

	public void setVelocityY(float velocityY) {
		this.velocityY = velocityY;
	}

	/** Gets the flick scroll y velocity. */
	public float getVelocityY() {
		return velocityY;
	}

	/**
	 * For flick scroll, if true the widget can be scrolled slightly past its
	 * bounds and will animate back to its bounds when scrolling is stopped.
	 * Default is true.
	 */
	public void setOverscroll(boolean overscrollX, boolean overscrollY) {
		this.overscrollX = overscrollX;
		this.overscrollY = overscrollY;
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
		forceScrollX = x;
		forceScrollY = y;
	}

	public boolean isForceScrollX() {
		return forceScrollX;
	}

	public boolean isForceScrollY() {
		return forceScrollY;
	}

	/**
	 * For flick scroll, sets the amount of time in seconds that a fling will
	 * continue to scroll. Default is 1.
	 */
	public void setFlingTime(float flingTime) {
		this.flingTime = flingTime;
	}

	/**
	 * For flick scroll, prevents scrolling out of the widget's bounds. Default
	 * is true.
	 */
	public void setClamp(boolean clamp) {
		this.clamp = clamp;
	}

	public void setupFadeScrollBars(float fadeAlphaSeconds,
			float fadeDelaySeconds) {
		this.fadeAlphaSeconds = fadeAlphaSeconds;
		this.fadeDelaySeconds = fadeDelaySeconds;
	}

	public void setSmoothScrolling(boolean smoothScrolling) {
		this.smoothScrolling = smoothScrolling;
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
	private Table cellTable;
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
	}

	public void setSelectable(boolean value) {
		this.selectable = value;
		isSelectedDirty = true;
	}

	private Vector2 hitPot = new Vector2();
	private Rectangle hitRect = new Rectangle();
	private int oldIndex;

	private Actor hitItem(float x, float y, boolean touchable) {
		hitRect.set(0, 0, getWidth(), getHeight());
		if (touchable && getTouchable() == Touchable.disabled)
			return null;
		for (int i = 0; i < cells.size; i++) {
			CellWrapper child = cells.get(i);
			Actor dos = child.getGroup();
			if (!dos.isVisible() || !hitRect.contains(x, y))
				continue;
			dos.parentToLocalCoordinates(hitPot.set(x, y));

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
			} else {
				System.out.println("what`s the fuck ,you hit....."
						+ selectedIndex);
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
		items = objects.clone();
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
}
