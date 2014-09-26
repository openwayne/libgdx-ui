package org.freyja.libgdx.cocostudio.ui.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class AdvancedImageButton extends ImageButton {
	private boolean isBig = false;
	private TTFLabel label = null;

	public AdvancedImageButton(Drawable imageUp) {
		this(imageUp, null);
	}
	
	@Override
	public void setSize(float width, float height) {
		super.setSize(width, height);
		if(getImage() == null) {
			return;
		}
		getImage().setSize(width, height);
		this.getImage().setOrigin(width / 2, height / 2);
		if(label != null) {
			label.setPosition((getWidth() - label.getWidth()) / 2,
					(getHeight() - label.getHeight()) / 2);
		}
	}

	public AdvancedImageButton(Drawable imageUp, Drawable imageDown) {
		this(imageUp, imageDown, null);
	}

	public AdvancedImageButton(Drawable imageUp, Drawable imageDown,
			Drawable imageChecked) {
		super(imageUp, imageDown, imageChecked);
		getImage().setTouchable(Touchable.disabled);
	}

	public AdvancedImageButton(ImageButtonStyle style) {
		super(style);
		getImage().setTouchable(Touchable.disabled);
	}
	
	public void addText(String txt, TTFLabelStyle labelStyle) {
		label = new TTFLabel(txt, labelStyle);
		label.setPosition((getWidth() - label.getWidth()) / 2,
				(getHeight() - label.getHeight()) / 2);
		label.setTouchable(Touchable.disabled);
		addActor(label);
	}
	
	public void modifyText(String txt) {
		if(label == null) {
			Gdx.app.error("AdvancedImageButton", "set text to an null label");
			return;
		}
		label.setText(txt);
		label.setAlignment(Align.center);
		label.setPosition((getWidth() - label.getWidth()) / 2,
				(getHeight() - label.getHeight()) / 2);
	}
	
	public void setLabelColor(Color color) {
		if(label == null) {
			Gdx.app.error("AdvancedImageButton", "set color to an null label");
			return;
		}
		label.setColor(color);
	}

	@Override
	public void setStyle(ButtonStyle style) {
		super.setStyle(style);
		ImageButtonStyle bStyle = (ImageButtonStyle) style;
		bStyle.imageChecked = bStyle.imageDown;
		if (bStyle.imageDown == null) {
			initListener();
		}
	}

	private void initListener() {
		Drawable up = this.getStyle().imageUp;
		if (this.getImage() == null || up == null) {
			return;
		}
		this.getImage().setOrigin(up.getMinWidth() / 2, up.getMinHeight() / 2);

		this.addListener(new ClickListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				toBig();
				super.enter(event, x, y, pointer, fromActor);
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				toSmall();
				super.exit(event, x, y, pointer, toActor);
			}
		});
	}

	public void toBig() {
		if (isBig == true) {
			return;
		}
		isBig = true;
		getImage().addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f));
	}

	public void toSmall() {
		if (isBig == false) {
			return;
		}
		isBig = false;

		getImage().addAction(Actions.scaleTo(1, 1, 0.1f));
	}
	
	
	/**
	 * 获取按钮上的文本
	 * @return
	 */
	public String getText() {
		if (this.label == null) {
			return "";
		} else {
			return label.getText().toString();
		}
	}

}