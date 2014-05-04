package org.freyja.libgdx.cocostudio.ui.widget;

import java.util.HashMap;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

/**
 * 可以分成多屏，通过外部控制可以任意切换page，每个page
 * 
 * @author waynewang
 * 
 */
public class PageView extends ScrollPane {

	/**
	 * 缓存panel 名字跟idx的对应关系
	 */
	private HashMap<Integer, String> cacheIdxName = new HashMap<Integer, String>();

	/**
	 * 只用于regPanel,其他不可用
	 */
	private int regIdxTmp = 0;

	/**
	 * 缓存panel 名字跟actor的对应关系
	 */
	private HashMap<String, Group> cacheNameActor = new HashMap<String, Group>();

	public PageView(Actor widget, ScrollPaneStyle style) {
		super(widget, style);
	}

	/**
	 * 把显示区域移动到第idx
	 * 
	 * @param idx
	 */
	public void setPageIdx(int idx) {
		Group panel = cacheNameActor.get(cacheIdxName.get(idx));
		// 移动到某个panel
		this.scrollX(idx * this.getWidth());
	}

	public void regPanel(Group panel, String panelName) {
		cacheIdxName.put(regIdxTmp++, panelName);
		cacheNameActor.put(panelName, panel);
	}

}
