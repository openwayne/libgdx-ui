package org.freyja.libgdx.cocostudio.ui.util;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

public class ImageBtnGroup extends ButtonGroup {

	private Button[] btns; 
	public ImageBtnGroup(Button... buttons) {
		super(buttons);
		btns = buttons;
		for (int i = 0; i < buttons.length; i++) {
			Button btn = buttons[i];
			btn.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					setChecked(getIdx(event.getTarget()));
					super.clicked(event, x, y);
				}
			});
		}
	}
	
	public int getIdx(Actor actor) {
		for (int i = 0; i < btns.length; i++) {
			Button btn = btns[i];
			if(btn.equals(actor)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void setChecked(String text) {
		new IllegalAccessException();
	}

	/**
	 * 选中那个位置的button
	 * 
	 * @param idx
	 */
	public void setChecked(int idx) {
		Array<Button> buttons = getButtons();
		Button button = buttons.get(idx);
		button.setChecked(true);
	}
	
	/**
	 * 返回ImageBtnGroup中处于被选中状态Button的index
	 * 
	 * @return
	 */
	public int getCheckedIdx(){
		for (int i = 0; i < btns.length; i++) {
			if (btns[i].isChecked()) {
				return i;
			}
		}
		return -1;
	}
}
