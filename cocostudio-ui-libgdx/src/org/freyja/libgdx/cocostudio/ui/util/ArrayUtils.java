package org.freyja.libgdx.cocostudio.ui.util;

import com.badlogic.gdx.utils.Array;

public class ArrayUtils {
	/**
	 * 查找某个元素在数组中的位置
	 * 
	 * @param items
	 * @param value
	 * @param identity
	 * @return
	 */
	public static <T> int indexOf(T[] items, T value, boolean identity) {
		if (identity || value == null) {
			for (int i = 0, n = items.length; i < n; i++)
				if (items[i] == value)
					return i;
		} else {
			for (int i = 0, n = items.length; i < n; i++)
				if (value.equals(items[i]))
					return i;
		}
		return -1;
	}

	public static int indexOf(int[] items, int value) {
		for (int i = 0, n = items.length; i < n; i++)
			if (items[i] == value)
				return i;
		return -1;
	}

	public static <T> T[] remove(T[] items, T value, boolean identity) {
		Array<T> newArr = new Array<T>();
		newArr.addAll(items);
		newArr.removeValue(value, identity);
		return newArr.toArray();
	}

	public static int[] remove(int[] items, int value) {
		Array<Integer> newArr = new Array<Integer>();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != value) {
				newArr.add(items[i]);
			}
		}
		int[] arr = new int[newArr.size];
		System.arraycopy(newArr.items, 0, arr, 0, newArr.size);
		return arr;
	}
}
