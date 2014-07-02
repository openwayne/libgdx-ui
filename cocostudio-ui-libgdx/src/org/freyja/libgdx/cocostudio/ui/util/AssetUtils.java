package org.freyja.libgdx.cocostudio.ui.util;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;

public class AssetUtils {
	private static Map<AssetType, AssetManager> _assets = new HashMap<AssetType, AssetManager>();
	static {
		
	}
	
	public static void addAssetManager(AssetType type, AssetManager assetManager) {
		AssetManager tmp = _assets.get(type);
		if(tmp != null) {
			Gdx.app.log("AssetUtils", "替换了原有的AssetManager");
		}
		_assets.put(type, assetManager);
	}
	
	public static AssetManager getManager(AssetType type) {
		return _assets.get(type);
	}
	
	
	
	public static enum AssetType {
		HUD,
		BATTLE,
		DIALOG,
		ICON,
	}
}
