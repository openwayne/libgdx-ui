package org.freyja.libgdx.cocostudio.ui.model;

import java.io.Serializable;

public class CCWidgetData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8238598317738682399L;
	private String path;
	private String plistFile;
	private int resourceType;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPlistFile() {
		return plistFile;
	}

	public void setPlistFile(String plistFile) {
		this.plistFile = plistFile;
	}

	public int getResourceType() {
		return resourceType;
	}

	public void setResourceType(int resourceType) {
		this.resourceType = resourceType;
	}

}
