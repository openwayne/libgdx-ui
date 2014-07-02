package org.freyja.libgdx.cocostudio.ui.res;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

public class AdvancedTexture extends Texture {

	public AdvancedTexture(FileHandle file, Texture texture) {
		super(texture.getTextureData());
	}

	
}
