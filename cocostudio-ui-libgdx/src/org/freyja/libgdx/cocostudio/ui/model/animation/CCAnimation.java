package org.freyja.libgdx.cocostudio.ui.model.animation;

import java.io.Serializable;
import java.util.List;

public class CCAnimation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7469741851314928324L;

	String classname;

	String name;

	List<CCAction> actionlist;

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<CCAction> getActionlist() {
		return actionlist;
	}

	public void setActionlist(List<CCAction> actionlist) {
		this.actionlist = actionlist;
	}

}
