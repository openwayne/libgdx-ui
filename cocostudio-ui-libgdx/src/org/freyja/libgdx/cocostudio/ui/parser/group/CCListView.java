package org.freyja.libgdx.cocostudio.ui.parser.group;

import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;
import org.freyja.libgdx.cocostudio.ui.model.CCOption;
import org.freyja.libgdx.cocostudio.ui.model.CCWidget;
import org.freyja.libgdx.cocostudio.ui.widget.ListView;
import org.freyja.libgdx.cocostudio.ui.widget.ListView.ListViewStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class CCListView extends CCScrollView {

	@Override
	public String getClassName() {
		return "ListView";
	}

	@Override
	public Actor parse(CocoStudioUIEditor editor, CCWidget widget,
			CCOption option) {
		ListViewStyle style = new ListViewStyle();

		if (option.getBackGroundImageData() != null) {

			style.background = editor.findDrawable(option, option
					.getBackGroundImageData().getPath());
		}

		ListView scrollPane = new ListView(style);
		switch (option.getDirection()) {
		case 1:
			scrollPane.setForceScroll(false, true);
			// scrollPane.setScrollingDisabled(true,true);
			break;
		case 2:
			scrollPane.setForceScroll(true, false);
			// scrollPane.setScrollingDisabled(false, false);
			break;

		case 3:
			scrollPane.setForceScroll(true, true);
			// scrollPane.setScrollingDisabled(false, false);
			break;
		}
		scrollPane.setClamp(false);
		scrollPane.setFlickScroll(option.isBounceEnable());
		return scrollPane;
	}
}
