package org.freyja.libgdx.cocostudio.ui.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.ObjectOutputStream;

import org.freyja.libgdx.cocostudio.ui.model.CCExport;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class ObjectToFile {

	public static void toFile(Object obj, String exportName) {
		try {
			FileOutputStream foStream = new FileOutputStream(exportName);// 在当前的项目下创建,dat为代码处理的data文件
			ObjectOutputStream ooStream = new ObjectOutputStream(foStream);
			ooStream.writeObject(obj);// 在用foStream初始化后
			// writeObject(emp)，将对象状态保存称序列化
			// for(int i=0;i<5;i++) FileOutputStream 提供了write(byte b[])
			// write(int b)的方法
			// foStream.write(days[i]);
			foStream.flush(); // 却是用foStream来强制输出和关闭
			// extends from OutputStream :force any buffered
			// output bytes to written out
			foStream.close(); // releases any system resources associated with
			// this stream.
		} catch (Exception e) {
			System.out.println("Error during output: " + e.toString());
		}
	}

	public static void main(String[] args) {
		// 遍历目录
		File dir = new File("/Users/waynewang/tmp/sanguo/android/assets/new_ui");

		File[] jsonDirs = dir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return true;
				}
				return false;
			}
		});

		for (File jsonDir : jsonDirs) {
			File[] jsons = jsonDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".json")) {
						return true;
					}
					return false;
				}
			});

			for (File json : jsons) {
				FileHandle fh = new FileHandle(json);
				String jsonStr = fh.readString("utf-8");
				Json jj = new Json();
				jj.setIgnoreUnknownFields(true);
				CCExport export = jj.fromJson(CCExport.class, jsonStr);
				toFile(export, json.getPath().replace(".json", ".exp"));
			}
		}
	}
}
