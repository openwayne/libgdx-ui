package org.freyja.libgdx.cocostudio.ui.widget;

import org.freyja.libgdx.cocostudio.ui.parser.widget.CCLabel;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;

public class TTFLabelStyle extends LabelStyle {

	private FileHandle fontFileHandle;

	private int fontSize;
//	private static BitmapFont defaultBmf = new BitmapFont();
	
	public TTFLabelStyle(Color fontColor, FileHandle fontFileHandle,
			int fontSize) {
		super(CCLabel.sharedFont, fontColor);
		this.fontFileHandle = fontFileHandle;
		this.fontSize = fontSize;
		this.font = CCLabel.sharedFont;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public FileHandle getFontFileHandle() {
		return fontFileHandle;
	}

	public void setFontFileHandle(FileHandle fontFileHandle) {
		this.fontFileHandle = fontFileHandle;
	}

	public void clearFont() {
//		if(!font.equals(defaultBmf)) {
//			font.dispose();
//		}
	}

}
