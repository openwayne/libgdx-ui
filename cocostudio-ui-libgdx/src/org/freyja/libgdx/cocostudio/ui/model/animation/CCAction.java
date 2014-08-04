package org.freyja.libgdx.cocostudio.ui.model.animation;

import java.io.Serializable;
import java.util.List;

public class CCAction implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5788157073654011197L;
	String classname;
	String name;
	List<CCActionNode> actionnodelist;

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

	public List<CCActionNode> getActionnodelist() {
		return actionnodelist;
	}

	public void setActionnodelist(List<CCActionNode> actionnodelist) {
		this.actionnodelist = actionnodelist;
	}

}
