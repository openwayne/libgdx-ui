package org.freyja.libgdx.cocostudio.ui.widget;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class ProgressBar extends Table {
	private float min, max;
	private float value;
	final boolean vertical;

	private Image bgImg;
	
	public ProgressBar(float min, float max, boolean vertical, Drawable bg) {
		if (min > max)
			throw new IllegalArgumentException("min must be > max: " + min
					+ " > " + max);
		if (bg == null)
			throw new IllegalArgumentException("backgroud is null");
		this.min = min;
		this.max = max;
		bgImg = new Image(bg);
		
		bgImg.setOrigin(0, 0);
		this.addActor(bgImg);
		this.vertical = vertical;
		this.value = min;
		this.setClip(true);
	}

	@Override
	public void setSize(float width, float height) {
		bgImg.setSize(width, height);
		super.setSize(width, height);
	}

	public float getValue() {
		return value;
	}

	public boolean setValue(float value) {
		value = clamp(value);
		float oldValue = this.value;
		if (value == oldValue)
			return false;
		this.value = value;
		if(vertical) {
			super.setSize(bgImg.getWidth(), bgImg.getHeight() * (this.value / this.max));
		} else {
			super.setSize(bgImg.getWidth() * (this.value / this.max), bgImg.getHeight());
		}
		
		return true;
	}

	/**
	 * Clamps the value to the progress bar's min/max range. This can be
	 * overridden to allow a range different from the progress bar knob's range.
	 */
	protected float clamp(float value) {
		return MathUtils.clamp(value, min, max);
	}

	/**
	 * Sets the range of this progress bar. The progress bar's current value is
	 * clamped to the range.
	 */
	public void setRange(float min, float max) {
		if (min > max)
			throw new IllegalArgumentException("min must be <= max");
		this.min = min;
		this.max = max;
		if (value < min)
			setValue(min);
		else if (value > max)
			setValue(max);
	}

	public float getMinValue() {
		return this.min;
	}

	public float getMaxValue() {
		return this.max;
	}
}
