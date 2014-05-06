package org.freyja.libgdx.cocostudio.ui.parser.group;

import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;
import org.freyja.libgdx.cocostudio.ui.model.CCOption;
import org.freyja.libgdx.cocostudio.ui.model.CCWidget;
import org.freyja.libgdx.cocostudio.ui.widget.PageView;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class CCPageView extends CCPanel {
	@Override
	public String getClassName() {
		return "PageView";
	}

	@Override
	public Actor parse(CocoStudioUIEditor editor, CCWidget widget,
			CCOption option) {
		PageView table = new PageView();

		if (option.getColorType() == 0) {// 无颜色

		} else if (option.getColorType() == 1) {// 单色

			Pixmap pixmap = new Pixmap((int) option.getWidth(),
					(int) option.getHeight(), Format.RGBA8888);
			pixmap.setColor(option.getBgColorR() / 255f,
					option.getBgColorG() / 255f, option.getBgColorB() / 255f,
					option.getBgColorOpacity() / 255f);

			pixmap.fill();

			table.setBackground(new TextureRegionDrawable(new TextureRegion(
					new Texture(pixmap))));
			pixmap.dispose();
		} else {// 渐变色

		}

		if (option.getBackGroundImageData() != null) {// Panel的图片并不是拉伸平铺的!!.但是这里修改为填充
			Drawable tr = editor.findDrawable(option, option
					.getBackGroundImageData().getPath());
			if (tr != null) {
//				Image bg = new Image(tr);
//				bg.setPosition((option.getWidth() - bg.getWidth()) / 2,
//						(option.getHeight() - bg.getHeight()) / 2);
//				// bg.setFillParent(true);
//				bg.setTouchable(Touchable.disabled);
//
//				bg.setColor(option.getColorR() / 255f,
//						option.getColorG() / 255f, option.getColorB() / 255f,
//						option.getOpacity() / 255f);
//				bg.setSize(option.getWidth(), option.getHeight());
//				table.addActor(bg);
				table.setBackground(tr);
			}
		}
		table.setClip(option.isClipAble());

		return table;
	}
	
	/** 解析group控件,当前控件类型为Group的时候处理与Widget类型处理不同 */
	public Group groupChildrenParse(CocoStudioUIEditor editor, CCWidget widget,
			CCOption option, Group parent, Actor actor) {

		Group group = (Group) actor;

		// 获取当前pageView
		PageView pageView = (PageView) actor;

		// Group 虽然自己不接收事件,但是子控件得接收
		actor.setTouchable(option.isTouchAble() ? Touchable.enabled
				: Touchable.childrenOnly);
		// 必须设置Transform 为true 子控件才会跟着旋转.

		// group.setTransform(true);

		if (option.getScaleX() != 0 || option.getScaleY() != 0
				|| option.getRotation() != 0) {
			group.setTransform(true);
		}

		for (CCWidget childrenWidget : widget.getChildren()) {
			Actor childrenActor = editor.parseWidget(group, childrenWidget);
			if (childrenActor == null) {
				continue;
			}
			group.addActor(childrenActor);
			if (childrenActor instanceof Group) {
				pageView.regPanel((Group) childrenActor, childrenWidget
						.getOptions().getName());
			}
		}
		
		sort(widget, group);

		return group;

	}
}
