package org.freyja.libgdx.cocostudio.ui.parser.group;

import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;
import org.freyja.libgdx.cocostudio.ui.model.CCOption;
import org.freyja.libgdx.cocostudio.ui.model.CCWidget;
import org.freyja.libgdx.cocostudio.ui.widget.ListView;
import org.freyja.libgdx.cocostudio.ui.widget.ListView.ListStyle;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class CCListView extends CCScrollView {
	@Override
	public String getClassName() {
		return "ListView";
	}

	@Override
	public Actor parse(CocoStudioUIEditor editor, CCWidget widget,
			CCOption option) {
		ListStyle style = new ListStyle();

		if (option.getBackGroundImageData() != null) {

			style.bgDrawable = editor.findDrawable(option, option
					.getBackGroundImageData().getPath());
		}
		
		ListView pageView = new ListView(style);

		return pageView;
	}

}
