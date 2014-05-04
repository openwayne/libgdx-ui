package org.freyja.libgdx.cocostudio.ui.widget.list;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

/**
 * 点击列表中的某一项了
 * 
 * @author jianxiang.zi
 * 
 */
public abstract class CellClickedListener implements EventListener {

	@Override
	public boolean handle(Event event) {
		if (!(event instanceof CellClickedEvent))
			return false;
		cellClicked((CellClickedEvent) event, event.getTarget());
		return true;
	}

	abstract public void cellClicked(CellClickedEvent event, Actor actor);

	static public class CellClickedEvent extends Event {
		public BaseListCell cell;
	}
}
