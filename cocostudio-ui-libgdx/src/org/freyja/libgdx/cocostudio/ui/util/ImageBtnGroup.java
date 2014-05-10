package org.freyja.libgdx.cocostudio.ui.util;

import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.utils.Array;

public class ImageBtnGroup extends ButtonGroup {
	@Override
	public void setChecked(String text) {
		new IllegalAccessException();
	}
	
	/**
	 * 选中那个位置的button
	 * @param idx
	 */
	public void setChecked(int idx) {
		Array<Button> buttons = getButtons();
		Button button = buttons.get(idx);
		button.setChecked(true);
	}
}
