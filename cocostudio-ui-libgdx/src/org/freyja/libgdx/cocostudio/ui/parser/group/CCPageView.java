package org.freyja.libgdx.cocostudio.ui.parser.group;

import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;
import org.freyja.libgdx.cocostudio.ui.model.CCOption;
import org.freyja.libgdx.cocostudio.ui.model.CCWidget;
import org.freyja.libgdx.cocostudio.ui.widget.PageView;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;

public class CCPageView extends CCScrollView {
	@Override
	public String getClassName() {
		return "PageView";
	}

	@Override
	public Actor parse(CocoStudioUIEditor editor, CCWidget widget,
			CCOption option) {
		ScrollPaneStyle style = new ScrollPaneStyle();

		if (option.getBackGroundImageData() != null) {

			style.background = editor.findDrawable(option, option
					.getBackGroundImageData().getPath());
		}

		PageView pageView = new PageView(null, style);

		pageView.setForceScroll(false, false);
		pageView.setClamp(false);
		pageView.setFlickScroll(option.isBounceEnable());
		return pageView;
	}

	/** 解析group控件,当前控件类型为Group的时候处理与Widget类型处理不同 */
	public Group groupChildrenParse(CocoStudioUIEditor editor, CCWidget widget,
			CCOption option, Group parent, Actor actor) {
		Table table = new Table();

		// 获取当前pageView
		PageView pageView = (PageView) actor;

		// Group 虽然自己不接收事件,但是子控件得接收
		actor.setTouchable(option.isTouchAble() ? Touchable.enabled
				: Touchable.childrenOnly);
		// 必须设置Transform 为true 子控件才会跟着旋转.

		// group.setTransform(true);

		if (option.getScaleX() != 0 || option.getScaleY() != 0
				|| option.getRotation() != 0) {
			pageView.setTransform(true);
		}

		for (CCWidget childrenWidget : widget.getChildren()) {
			Actor childrenActor = editor.parseWidget(pageView, childrenWidget);
			if (childrenActor == null) {
				continue;
			}
			table.addActor(childrenActor);
			if (childrenActor instanceof Group) {
				pageView.regPanel((Group) childrenActor, childrenWidget
						.getOptions().getName());
			}
		}
		
		sort(widget, pageView);
		pageView.setWidget(table);
		return pageView;

	}
}
