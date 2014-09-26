package org.freyja.libgdx.cocostudio.ui.res;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

public class TextureManager {

	private static Map<String, RefTexture> _cache = new HashMap<String, RefTexture>();
	
//	static {
//		Timer.schedule(new Task() {
//			
//			@Override
//			public void run() {
//				debug();
//			}
//		}, 0, 1);
//	}
	
	private static void addCache(String key, RefTexture texture) {
		_cache.put(key, texture);
	}

	public static void debug() {
		System.err.println("TextureManager 剩余纹理: " + _cache.size());
	}
	
	public static Texture getTexture(String clzName, String fileName) {
		return getTexture(clzName, Gdx.files.internal(fileName));
	}
	
	public static Texture getTexture(String clzName, FileHandle fileHandle) {
		RefTexture tmp = _cache.get(fileHandle.path());

		if (tmp != null) {
			tmp.count++;
			Gdx.app.debug("同步加载资源", tmp.file + " ==> " + tmp.count);
			tmp.refMap.put(clzName, 0);
			return tmp.texture;
		}

		tmp = new RefTexture();

		tmp.texture = new Texture(fileHandle);
		tmp.refMap.put(clzName, 0);
		tmp.file = fileHandle.path();
		tmp.count = 1;
		
		Gdx.app.debug("同步加载资源", tmp.file + " ==> " + tmp.count);

		addCache(fileHandle.path(), tmp);
		return tmp.texture;
	}

	public static void disposeTexture(String file) {
		disposeTexture(file, false);
	}
	public static void disposeTexture(String file, boolean full) {
		RefTexture tmp = _cache.get(file);

		if (tmp == null) {
			return;
		}
		
		if (full) {
			tmp.count = -1;
		} else {
			tmp.count--;
		}
		if (tmp.count <= 0) {
			tmp.texture.dispose();
			_cache.remove(file);
			Gdx.app.debug("清空纹理 Unload (dispose)  : ", file + " ==> " + tmp.count);
		} else {
			Gdx.app.debug("清空纹理 Unload (decrement) : ", file + " ==> " + tmp.count);
		}
	}

	private static Array<String> removeArr = new Array<String>();
	
	public static void cleanModule(String clzName) {
		cleanModule(clzName, false);
	}
	
	public static void cleanModule(String clzName, boolean full) {
		Iterator<String> iter = _cache.keySet().iterator();
		removeArr.clear();
		String key;
		RefTexture refTexture;
		while (iter.hasNext()) {

			key = iter.next();

			refTexture = _cache.get(key);

			Integer ref = refTexture.refMap.get(clzName);
			if (ref != null) {
				removeArr.add(refTexture.file);
			}
		}

		for (int i = 0; i < removeArr.size; i++) {
			disposeTexture(removeArr.get(i), full);
		}
		removeArr.clear();
	}

	public static class RefTexture {
		public Map<String, Integer> refMap = new HashMap<String, Integer>();
		public String file;
		public int count = 1;
		public Texture texture;
	}
}
