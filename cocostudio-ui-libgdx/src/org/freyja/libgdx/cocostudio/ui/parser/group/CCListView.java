package org.freyja.libgdx.cocostudio.ui.parser.group;

import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;
import org.freyja.libgdx.cocostudio.ui.model.CCOption;
import org.freyja.libgdx.cocostudio.ui.model.CCWidget;
import org.freyja.libgdx.cocostudio.ui.widget.ListView;
import org.freyja.libgdx.cocostudio.ui.widget.ListView.ListStyle;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

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

		if (option.getColorType() == 0) {// 无颜色

		} else if (option.getColorType() == 1) {// 单色

			Pixmap pixmap = new Pixmap((int) option.getWidth(),
					(int) option.getHeight(), Format.RGBA8888);
			pixmap.setColor(option.getBgColorR() / 255f,
					option.getBgColorG() / 255f, option.getBgColorB() / 255f,
					option.getBgColorOpacity() / 255f);

			pixmap.fill();

			pageView.setBackground(new TextureRegionDrawable(new TextureRegion(
					new Texture(pixmap))));
			pixmap.dispose();
			
			pageView.setColor(option.getBgColorR() / 255f,
					option.getBgColorG() / 255f, option.getBgColorB() / 255f,
					option.getBgColorOpacity() / 255f);
		} else {// 渐变色

		}

		if (option.getBackGroundImageData() != null) {// Panel的图片并不是拉伸平铺的!!.但是这里修改为填充
			Drawable tr = editor.findDrawable(option, option
					.getBackGroundImageData().getPath());
			if (tr != null) {
				Image bg = new Image(tr);
				bg.setPosition((option.getWidth() - bg.getWidth()) / 2,
						(option.getHeight() - bg.getHeight()) / 2);
				// bg.setFillParent(true);
				bg.setTouchable(Touchable.disabled);

				bg.setColor(option.getColorR() / 255f,
						option.getColorG() / 255f, option.getColorB() / 255f,
						option.getOpacity() / 255f);
				pageView.addActor(bg);
			}
		}
		
		return pageView;
	}

}
