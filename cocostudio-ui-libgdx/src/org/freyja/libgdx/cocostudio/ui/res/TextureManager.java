package org.freyja.libgdx.cocostudio.ui.res;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class TextureManager {

	private static Map<String, RefTexture> _cache = new HashMap<String, RefTexture>();
	private static void addCache(String key, RefTexture texture) {
		_cache.put(key, texture);
	}
	
	

	/********************************DEBUG**********************************/
//	static {
//		Timer.schedule(new Task() {
//			
//			@Override
//			public void run() {
//				debug();
//			}
//		}, 0, 1);
//	}

	public static void debug() {
		System.err.println("TextureManager 剩余纹理: " + _cache.size());
		float memAll = 0;
		for (Iterator<RefTexture> iterator = _cache.values().iterator(); iterator.hasNext();) {
			RefTexture type = iterator.next();
			float memSize = 1.0f * (pow2(type.texture.getWidth()) * pow2(type.texture.getHeight()) * 4) / (1000 * 1024);
			System.err.println("TextureManager info : 文件名(" + type.file
					+ ") 数量 : " + type.count + " 占用内存 : " + memSize + "M"
					+ " 宽:" + type.texture.getWidth() + " 高:" + type.texture.getHeight());
			memAll += memSize;
		}
		System.err.println("TextureManager 总共内存占用:" + memAll + "M");
	}
	
	private static float pow2(int num) {
		int i = 0;
		while((2 <<(i-1))< num){
			i++;
		}
		
		return (2 <<(i-1));
	}
	/********************************DEBUG**********************************/

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
	
	
	/*********************************************************************************************/
	
	private static HashMap<String, RefAtlas> atlasMap = new HashMap<String, RefAtlas>();

	public static TextureAtlas getAtlas(String path) {
		RefAtlas atlas = atlasMap.get(path);
		if(atlas != null) {
			atlas.count++;
			Gdx.app.debug("同步加载资源", path + " ==> " + atlas.count);

			return atlas;
		}
		atlas = new RefAtlas(Gdx.files.internal(path));
		Gdx.app.debug("同步加载资源", path + " ==> " + atlas.count);
		atlasMap.put(path, atlas);
		return atlas;
	}

	public static void disposeAtlas(String path) {
		RefAtlas atlas = atlasMap.get(path);
		if(atlas == null) {
			return;
		}
		atlas.count--;

		if(atlas.count > 0) {
			Gdx.app.debug("清空Atlas Unload (decrement) : ", path + " ==> " + atlas.count);
			return;
		}
		
		Gdx.app.debug("清空Atlas Unload (dispose)  : ", path + " ==> " + atlas.count);
		atlas = atlasMap.remove(path);
		atlas.dispose();
	}
	
	public static class RefAtlas extends TextureAtlas{
		public int count = 1;
		public RefAtlas (String internalPackFile) {
			super(internalPackFile);
		}
		
		public RefAtlas (FileHandle packFile) {
			super(packFile);
		}
	}
	
}
