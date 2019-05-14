package com.wmwl.core.common.util;

import android.util.Log;

/**
 * 对android自带日志的一个简单封装，方便调用
 * 
 * @author Pony
 * 
 */
public class LoggerUtil {
	public static final String TAG = "WMWL";
	private static boolean isLogEnable = true;// true打开日志,false关闭日志
	private static boolean isOutLogEnable = true;// 对外调试日志，true打开日志,false关闭日志
	private static long startTime = 0;


	// 调试信息输出
	public static void d(String tag, String msg) {
		if (isLogEnable) {
			Log.d(tag, msg);
//			FileHelp.outPut1(msg) ;
		}
	}

	public static void d(String msg){
		d(TAG,msg);
	}
	
	// 外部调试信息输出
	public static void dOut(String tag, String msg) {
		if (isOutLogEnable) {
			Log.d(tag, msg);
		}
	}

	public static void dOut(String msg){
		dOut(TAG,msg);
	}

	// 错误信息输出
	public static void e(String tag, String msg) {
		if (isLogEnable) {
			Log.e(tag, msg);
//			FileHelp.outPut1(msg) ;
		}
	}

	public static void e(String msg){
		e(TAG,msg);
	}
	
	// 外部错误信息输出
	public static void eOut(String tag, String msg) {
		if (isOutLogEnable) {
			Log.e(tag, msg);
		}
	}

	public static void eOut(String msg){
		e(TAG,msg);
	}

	/*
	 * 记录方法调用的开始时间
	 */
	public static void startTime() {
		startTime = System.currentTimeMillis();
		d(TAG, "start time:" + startTime);
	}

	/*
	 * 记录方法调用的使用时间
	 */
	public static void useTime() {
		long endTime = System.currentTimeMillis();
		d(TAG, "use time:" + (endTime - startTime));
	}
	
	/**
	 * 日志是否可用
	 * @param enabled
	 */
	public static void isLogEnabled(boolean enabled){
		isOutLogEnable = enabled;
	}
}