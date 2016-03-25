package com.bbq.w.library;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.SharedPreferences;

public class SpUtils {
	
	private static final String SP_NAME = SpUtils.class.getCanonicalName();
	private static Method sApplyMethod;

	static {
		try {
			Class<?>[] arrayOfClass = new Class[0];
			sApplyMethod = SharedPreferences.Editor.class.getMethod("apply",
					arrayOfClass);
		} catch (NoSuchMethodException localNoSuchMethodException) {
			sApplyMethod = null;
		}
	}

	private static void apply(SharedPreferences.Editor editor) {
		if (sApplyMethod != null) {
			try {
				Method localMethod = sApplyMethod;
				Object[] arrayOfObject = new Object[0];
				localMethod.invoke(editor, arrayOfObject);
				return;
			} catch (IllegalAccessException e) {
				LogLib.e(e);
			} catch (InvocationTargetException e) {
				LogLib.e(e);
			}
		}

		editor.commit();
	}

	public static void saveString(Context cxt, String key, String value) {
		apply(cxt.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit()
				.putString(key, value));
	}

	public static String getString(Context cxt, String key) {
		return cxt.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getString(
				key, null);
	}
	
	public static void saveInt(Context cxt, String key, int value) {
		apply(cxt.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).edit()
				.putInt(key, value));
	}

	public static int getInt(Context cxt, String key, int defaultValue) {
		return cxt.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getInt(
				key, defaultValue);
	}
}
