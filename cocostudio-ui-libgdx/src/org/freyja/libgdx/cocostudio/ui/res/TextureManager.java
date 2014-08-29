package org.freyja.libgdx.cocostudio.ui.res;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

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
			tmp.refMap.put(clzName, 0);
			return tmp.texture;
		}

		tmp = new RefTexture();

		tmp.texture = new Texture(fileHandle);
		tmp.refMap.put(clzName, 0);
		tmp.file = fileHandle.path();
		tmp.count = 1;
		
		addCache(fileHandle.path(), tmp);
		return tmp.texture;
	}

	public static void disposeTexture(String file) {
		RefTexture tmp = _cache.get(file);

		if (tmp == null) {
			return;
		}

		tmp.count--;

		if (tmp.count <= 0) {
			tmp.texture.dispose();
			_cache.remove(file);
			Gdx.app.error("清空纹理", file);
		}
	}

	private static Array<String> removeArr = new Array<String>();
	public static void cleanModule(String clzName) {
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
			disposeTexture(removeArr.get(i));
		}
		removeArr.clear();
	}

	public static class RefTexture {
		public Map<String, Integer> refMap = new HashMap<String, Integer>();
		public String file;
		public int count = 0;
		public Texture texture;
	}
}
