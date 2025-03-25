package com.agenew.widget;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import android.util.Log;

/**
 * 
 * @author 王雷) 工具类，把一些平时编程时常用到的函数集中写在了这里
 */
public class Utils {
	private static final String TAG = "Utils";
	private static Handler mSubThreadHandler = null;
	private static HandlerThread mSubThread = null;
	public static final boolean DEBUG = !"user".equals(Build.TYPE);

	/**
	 * 打印函数栈，多用于debug
	 */
	public static void getTrace(Throwable throwable) {
		Log.i(TAG, getTraceString(throwable));
	}

	public static void getTrace(StackTraceElement[] ste) {
		Log.i(TAG, getTraceString(ste));
	}

	public static void getTrace() {
		Log.i(TAG, getTraceString(new Throwable()));
	}

	public static String getTraceString(Throwable throwable) {
		return getTraceString(throwable.getStackTrace());
	}

	public static String getTraceString(StackTraceElement[] ste) {
		StringBuilder sb = new StringBuilder();
		int i, total;

		total = ste.length;

		for (i = 0; i < total; i++) {
			sb.append("WL_DEBUG ste[");
			sb.append(i);
			sb.append("]");
			sb.append(ste[i].toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * 判断当前界面是否是桌面
	 * 
	 * @param context 上下文
	 * @return true：是桌面；false：不是桌面
	 */
	public static boolean isHome(Context context) {
		boolean result = false;
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ResolveInfo> apps = context.getPackageManager()
				.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_HOME), 0);
		List<RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);

		for (ActivityManager.RunningTaskInfo amTask : runningTasks) {
			String className = amTask.topActivity.getClassName();

			for (ResolveInfo info : apps) {
				if (info.activityInfo.name.equals(className)) {
					result = true;
					break;
				}
			}

			if (result) {
				break;
			}
		}

		return result;
	}

	/**
	 * 判断当前界面是否是锁屏界面
	 * 
	 * @param context 上下文
	 * @return true：是锁屏界面；false：不是锁屏界面
	 */
	public static boolean isLock(Context context) {
		boolean result = false;
		KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		result = keyguardManager.inKeyguardRestrictedInputMode();
		return result;
	}

	/**
	 * 获取系统CPU数量
	 * 
	 * @return 系统CPU数量
	 */
	public static int getCPUCount() {
		int result = 1;

		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			File dir = new File("/sys/devices/system/cpu/");
			File[] files = dir.listFiles(new CpuFilter());
			result = files.length;
		} catch (Exception e) {
			Log.e(TAG, "WL_DEBUG getCPUCount error : " + e);
		}

		return result;
	}

	/**
	 * 使用透明色清理画布
	 * 
	 * @param canvas 画布
	 */
	public static void clearCanvas(Canvas canvas) {
		Paint clipPaint = new Paint();
		clipPaint.setAntiAlias(true);
		clipPaint.setStyle(Paint.Style.STROKE);
		clipPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		canvas.drawPaint(clipPaint);
	}

	/**
	 * 通过电话号码查询联系人姓名
	 * 
	 * @param context 上下文
	 * @param address 电话号码
	 * @return 联系人姓名，未查到时为null
	 */
	public static String getContactsName(Context context, String address) {
		String result = null;

		if (!TextUtils.isEmpty(address)) {
			String[] arrayOfString1 = { "_id", "display_name" };
			Cursor cursor = context.getContentResolver().query(
					Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address)), arrayOfString1, null,
					null, null);

			if (cursor != null) {
				if ((cursor.getCount() > 0) && (cursor.moveToFirst())) {
					result = cursor.getString(1);
				}

				cursor.close();
			}
		}

		return result;
	}

	/**
	 * 获取上下文，一般用于访问其它apk中的函数（反射）或资源，慎用！
	 * 
	 * @param context     本地context
	 * @param packageName 需要获取上下文的apk的包名
	 * @return 对应packageName的上下文
	 */
	public static Context getContext(Context context, String packageName) {
		Context result = context;
		try {
			result = context.createPackageContext(packageName,
					Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "WL_DEBUG getContext error : " + e);
		}
		return result;
	}

	/**
	 * 获取子线程Handler
	 * 
	 * @return 子线程Handler
	 */
	public static Handler getSubThreadHandler() {
		if (mSubThreadHandler == null) {
			synchronized (Utils.class) {
				if (mSubThreadHandler == null) {
					mSubThread = new HandlerThread("SubThread", android.os.Process.THREAD_PRIORITY_BACKGROUND);
					mSubThread.start();
					mSubThreadHandler = new Handler(mSubThread.getLooper());
				}
			}
		}
		return mSubThreadHandler;
	}

	/**
	 * 必要时释放子线程资源
	 */
	public static void uninitSubThread() {
		if (mSubThread != null) {
			mSubThread.getLooper().quit();
			mSubThread = null;
		}

		if (mSubThreadHandler != null) {
			mSubThreadHandler = null;
		}
	}

	/**
	 * 执行命令并且输出结果
	 */
	public static final String execCmd(String cmd, boolean isRoot) {
		String result = "";
		DataOutputStream dos = null;
		DataInputStream dis = null;
		DataInputStream err = null;
		String errMsg = "";

		try {
			Process p = Runtime.getRuntime().exec(isRoot ? "su" : "sh");// 经过Root处理的android系统即有su命令
			dos = new DataOutputStream(p.getOutputStream());
			dis = new DataInputStream(p.getInputStream());
			err = new DataInputStream(p.getErrorStream());

			dos.writeBytes(cmd + "\n");
			dos.flush();
			dos.writeBytes("exit\n");
			dos.flush();
			String line = null;
			while ((line = dis.readLine()) != null) {
				result += line;
			}
			while ((line = err.readLine()) != null) {
				errMsg += line;
			}
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (err != null) {
				try {
					err.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Log.i(TAG, "WL_DEBUG execRootCmd cmd = " + cmd + ", result = " + result + ", errMsg = " + errMsg);
		return result;
	}

	/**
	 * 执行命令但不关注结果输出
	 */
	public static final int execCmdSilent(String cmd, boolean isRoot) {
		int result = -1;
		DataOutputStream dos = null;
		DataInputStream err = null;
		String errMsg = "";

		try {
			Process p = Runtime.getRuntime().exec(isRoot ? "su" : "sh");
			dos = new DataOutputStream(p.getOutputStream());
			err = new DataInputStream(p.getErrorStream());

			dos.writeBytes(cmd + "\n");
			dos.flush();
			dos.writeBytes("exit\n");
			dos.flush();
			String line = null;
			while ((line = err.readLine()) != null) {
				errMsg += line;
			}
			p.waitFor();
			result = p.exitValue();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (err != null) {
				try {
					err.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Log.i(TAG, "WL_DEBUG execRootCmdSilent cmd = " + cmd + ", result = " + result + ", errMsg = " + errMsg);
		return result;
	}
}