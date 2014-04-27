package org.freyja.libgdx.cocostudio.ui.widget;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class AdvancedImageButton extends ImageButton {
	private boolean isBig = false;

	public AdvancedImageButton(Drawable imageUp) {
		super(imageUp);
	}

	public AdvancedImageButton(Drawable imageUp, Drawable imageDown) {
		super(imageUp, imageDown);
	}

	public AdvancedImageButton(Drawable imageUp, Drawable imageDown,
			Drawable imageChecked) {
		super(imageUp, imageDown, imageChecked);
	}

	public AdvancedImageButton(ImageButtonStyle style) {
		super(style);
	}

	@Override
	public void setStyle(ButtonStyle style) {
		super.setStyle(style);
		ImageButtonStyle bStyle = (ImageButtonStyle) style;
		if (bStyle.imageDown == null) {
			initListener();
		}
	}

	private void initListener() {
		Drawable up = this.getStyle().imageUp;
		if (this.getImage() == null) {
			return;
		}
		this.getImage().setOrigin(up.getMinWidth() / 2, up.getMinHeight() / 2);
		this.addListener(new ClickListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer,
					Actor fromActor) {
				toBig();
				super.enter(event, x, y, pointer, fromActor);
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer,
					Actor toActor) {
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
		setScale(1.5f);
		getImage().addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f));
	}

	public void toSmall() {
		if (isBig == false) {
			return;
		}
		isBig = false;
		setScale(1.0f);

		getImage().addAction(Actions.scaleTo(1, 1, 0.1f));
	}
}
