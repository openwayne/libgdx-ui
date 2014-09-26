package org.freyja.libgdx.cocostudio.ui.widget;

import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * 可以分成多屏，通过外部控制可以任意切换page，每个page
 * 
 * @author waynewang
 * 
 */
public class PageView extends Table {

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

	private int curIdx = 0;

	/**
	 * 把显示区域移动到第idx
	 * 
	 * @param idx
	 */
	public void setPageIdx(int idx) {
		int moveIdx = curIdx - idx;
		curIdx = idx;
		Iterator<String> it = cacheNameActor.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			Group group = cacheNameActor.get(key);
			group.setVisible(false);
			group.setPosition(group.getX() + getWidth() * moveIdx, group.getY());
		}
		Group panel = cacheNameActor.get(cacheIdxName.get(idx));
		panel.setVisible(true);

	}

	@Override
	public void setSize(float width, float height) {
		// this.setClip(true);
		// this.setCullingArea(new Rectangle(0, 0, width, height));
		super.setSize(width, height);
	}

	public void regPanel(Group panel, String panelName) {
		cacheIdxName.put(regIdxTmp++, panelName);
		cacheNameActor.put(panelName, panel);
	}
	
	@Override
	public void dispose() {
		cacheIdxName.clear();
		cacheNameActor.clear();
		super.dispose();
	}
}
