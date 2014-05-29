package org.freyja.libgdx.cocostudio.ui.parser.widget;

import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;
import org.freyja.libgdx.cocostudio.ui.model.CCOption;
import org.freyja.libgdx.cocostudio.ui.model.CCWidget;
import org.freyja.libgdx.cocostudio.ui.parser.WidgetParser;
import org.freyja.libgdx.cocostudio.ui.widget.ProgressBar;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class CCLoadingBar extends WidgetParser {

	@Override
	public String getClassName() {
		return "LoadingBar";
	}

	@Override
	public Actor parse(CocoStudioUIEditor editor, CCWidget widget,
			CCOption option) {

		ProgressBar mProgressBar = new ProgressBar(1, 100, false, editor.findDrawable(option, option.getTextureData()
				.getPath()));

		mProgressBar.setSize(option.getWidth() * option.getScaleX(),
				option.getHeight() * option.getScaleY());
		return mProgressBar;

	}

}
