package org.freyja.libgdx.cocostudio.ui.util;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class FontUtil {

	static FreeTypeFontGenerator generator;
	static Map<FileHandle, FreeTypeFontGenerator> generators = new HashMap<FileHandle, FreeTypeFontGenerator>();
	static FileHandle fontFile = Gdx.files.internal("DroidSansFallback.ttf");

	public static BitmapFont createFont(String text, int fontSize) {
		return createFont(fontFile, text, fontSize);
	}

	/**
	 * 缓存FreeTypeFontGenerator 对性能有显著提升
	 * 
	 * @param fontHandle
	 * @param text
	 * @param fontSize
	 * @return
	 */
	public static BitmapFont createFont(FileHandle fontHandle, String text,
			int fontSize) {

		if (fontHandle == null) {
			return new BitmapFont();
		}

		BitmapFont font = null;
		// FreeTypeFontGenerator generator = null;
		try {

			generator = generators.get(fontHandle);
			if (generator == null) {
				generator = new FreeTypeFontGenerator(fontHandle);
				generators.put(fontHandle, generator);
			}

			String newText = StringUtil.removeRepeatedChar(text);
			FreeTypeFontParameter param = new FreeTypeFontParameter();
			param.size = fontSize;
			param.characters = newText;
			param.flip = false;
			font = generator.generateFont(param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (font == null) {
			return new BitmapFont();
		}
		return font;

	}
}
