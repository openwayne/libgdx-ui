package org.freyja.libgdx.cocostudio.ui.util;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class FileToObject {
	public static Object toObj(String fileName) {
		Object obj = null;
		try {
//			FileInputStream fiStream = new FileInputStream(fileName);
			FileHandle handle = Gdx.files.internal(fileName);
			ObjectInputStream oiStream = new ObjectInputStream(handle.read());
			obj = oiStream.readObject(); // 同样用ObjectInputStream方法实现
			// ,读取文件中对象还原称反序列化
			// FileInputStream 只能read(byte b[])
//			fiStream.close(); // 用FileInputStream来关闭 //releases system resources
			// associated with this stream.
			System.out.println(obj);// Object类中指明类的toString碰到“println”之类的输出方法时会自动调用，不用显式打出来
		} catch (Exception e) {
			System.out.println("Error during input: " + e.toString());
		}
		return obj;
	}
}
