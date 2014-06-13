package org.freyja.libgdx.cocostudio.ui.parser.group;

import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;
import org.freyja.libgdx.cocostudio.ui.model.CCOption;
import org.freyja.libgdx.cocostudio.ui.model.CCWidget;
import org.freyja.libgdx.cocostudio.ui.widget.ListView;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;

public class CCListView extends CCScrollView {

	@Override
	public String getClassName() {
		return "ListView";
	}

	@Override
	public Actor parse(CocoStudioUIEditor editor, CCWidget widget,
			CCOption option) {
		ScrollPaneStyle style = new ScrollPaneStyle();

		if (option.getBackGroundImageData() != null) {

			style.background = editor.findDrawable(option, option
					.getBackGroundImageData().getPath());
		}

		ListView scrollPane = new ListView(style);
		switch (option.getDirection()) {
		case 1:
			scrollPane.setForceScroll(false, true);
			scrollPane.setScrollingDisabled(true, false);
			break;
		case 2:
			scrollPane.setForceScroll(true, false);
			scrollPane.setScrollingDisabled(false, true);
			break;

		case 3:
			scrollPane.setForceScroll(true, true);
			scrollPane.setScrollingDisabled(true, true);
			break;
		}
		scrollPane.setClamp(true);
		scrollPane.setFlickScroll(true);
		return scrollPane;

	}
}
