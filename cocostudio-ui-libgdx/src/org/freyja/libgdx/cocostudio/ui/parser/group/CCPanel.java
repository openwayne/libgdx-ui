package org.freyja.libgdx.cocostudio.ui.parser.group;

import org.freyja.libgdx.cocostudio.ui.CocoStudioUIEditor;
import org.freyja.libgdx.cocostudio.ui.model.CCOption;
import org.freyja.libgdx.cocostudio.ui.model.CCWidget;
import org.freyja.libgdx.cocostudio.ui.parser.GroupParser;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * @tip 还未支持单色背景属性,背景图片在Cocostudio里面并不是铺满,而是居中
 * @author i see
 * 
 */
public class CCPanel extends GroupParser {

	@Override
	public String getClassName() {
		return "Panel";
	}

	@Override
	public Actor parse(CocoStudioUIEditor editor, CCWidget widget, CCOption option) {
		Table table = new Table();
		if (option.getColorType() == 0) {// 无色(包含无色)色即是空，空即是色。这个bug(panel无色时放入listView大小会丢失)暂时这么干吧
			Pixmap pixmap = new Pixmap((int) option.getWidth(), (int) option.getHeight(), Format.RGBA4444);
			pixmap.setColor(0, 255, 255, 255);//透明颜色
			pixmap.fill();
			table.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(pixmap))));
			pixmap.dispose();
		} else if (option.getColorType() == 2) {// 渐变色
			//TODO 渐变色需要特殊处理
		} else if (option.getColorType() == 1) {// 单色
			Pixmap pixmap = new Pixmap((int) option.getWidth(), (int) option.getHeight(), Format.RGBA4444);
			pixmap.setColor(option.getBgColorR() / 255f, option.getBgColorG() / 255f, option.getBgColorB() / 255f,
					option.getBgColorOpacity() / 255f);
			pixmap.fill();
			table.setBackground(new TextureRegionDrawable(new TextureRegion(new Texture(pixmap))));
			pixmap.dispose();
		}
		if (option.getBackGroundImageData() != null) {// Panel的图片并不是拉伸平铺的!!.但是这里修改为填充
			Drawable tr = editor.findDrawable(option, option.getBackGroundImageData().getPath());
			if (tr != null) {
				tr.setMinHeight(option.getHeight());
				tr.setMinWidth(option.getWidth());
				table.setBackground(tr);
			}
		}
		table.setClip(option.isClipAble());

		return table;
	}
}
