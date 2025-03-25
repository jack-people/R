package com.agenew.widget;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.StringJoiner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.provider.Telephony;
import android.telephony.ServiceState;
import android.net.ConnectivityManager;

/**
 * 
 * @author 赵俊杰) 工具类，目前包括：APN; MobileNetwork
 */
public class UtilsZ {
	
	private static final String TAG = "UtilsZ";
	
	public static class APNUtli {
		
		
		private static final String PROTOCOL_IPV4 = "IP";
		private static final String PROTOCOL_IPV6 = "IPV6";
		private static final String PROTOCOL_IPV4V6 = "IPV4V6";
		public static Uri APN_URI = Uri.parse("content://telephony/carriers");
		//获取或设置默认 APN 的 ID。
		public static Uri DEFAULT_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
		//查询当前正在使用的 APN 的详细信息。
		public static Uri CURRENT_APN_URI = Uri.parse("content://telephony/carriers/current");
		//访问SIM卡上的APN（Access Point Name）列表
		public static final Uri SIM_APN_URI = Uri.parse(
                "content://telephony/carriers/sim_apn_list");
	 
		public static int addAPN(Context context, int iptype, String profilename
		, String apn, int authmode, String username, String password, int mcc, int mnc) {
			int id = -1;
			String protocol = null;
			String NUMERIC = getSIMInfo(context);
			Log.d(TAG, "ZJJ_DEBUG APNUtli addAPN NUMERIC: "+ NUMERIC);
			if (NUMERIC == null) {
				return -1;
			}
			//Log.d(TAG, "ZJJ_DEBUG APNUtli addAPN numericNow: "+ numericNow);
			switch (iptype) {
				case 0:
					protocol = PROTOCOL_IPV4;
					break;
				case 1:
					protocol = PROTOCOL_IPV6;
					break;
				case 2:
					protocol = PROTOCOL_IPV4V6;
					break;
				default:
					Log.e(TAG, "ZJJ_DEBUG APNUtli addAPN Invalid iptype: " + iptype);
					return -1;
			}
	 
			ContentResolver resolver = context.getContentResolver();
			ContentValues[] mContentValues = new ContentValues[6];
			ContentValues values = new ContentValues();
			values.put("protocol", protocol); 
			values.put("roaming_protocol", protocol); 			
			values.put("name", profilename);                                     
			values.put("apn", apn);                            
			values.put("authtype", authmode);
			values.put("user", username);
			values.put("password", password);
			values.put("mcc", mcc); 
			values.put("mnc", mnc);  
			values.put("numeric", NUMERIC); 
			values.put("bearer", "0"); 
			values.put("edited", "1"); 
			values.put("bearer_bitmask", "0"); 
			values.put("sourcetype", "1"); 
			//values.put("mcc", "460"); // 中国 MCC
			//values.put("mnc", "01");  // 中国移动 MNC
			//values.put("numeric", "46001"); // 运营商代码
			values.put("type", "default,supl");
			values.put("carrier_enabled", "1"); // 是否启用
			values.put("current", "1");
			Uri newRow = resolver.insert(APN_URI, values);
			Log.d(TAG, "ZJJ_DEBUG APNUtli addAPN newRow: "+ newRow);
			if (newRow != null) {
				int result = Integer.parseInt(newRow.getLastPathSegment());
				Log.d(TAG, "ZJJ_DEBUG APNUtli New APN added: " + newRow.toString() + ", result = " + result);
				return result; // 返回新 APN 的 ID
			} else {
				Log.e(TAG, "ZJJ_DEBUG APNUtli Failed to add APN");
				return -1;
			}
			
		}
	 
		public static String getSIMInfo(Context context) {
			TelephonyManager iPhoneManager = (TelephonyManager)context
					.getSystemService(Context.TELEPHONY_SERVICE);
					return iPhoneManager.getSimOperator();
		}
	 
		// 设置接入点
		public static void setAPN(Context context,int apnmode,int index, int preferred, int onlymodify,
		int iptype, String profilename, String apn, int authmode, String username, String password) {
			Log.d(TAG, "ZJJ_DEBUG APNUtli setAPN : ");
			String protocol = null;
			switch (iptype) {
				case 0:
					protocol = PROTOCOL_IPV4;
					break;
				case 1:
					protocol = PROTOCOL_IPV6;
					break;
				case 2:
					protocol = PROTOCOL_IPV4V6;
					break;
				default:
					Log.e(TAG, "Invalid iptype: " + iptype);
			}
			ContentResolver resolver = context.getContentResolver();
			ContentValues values = new ContentValues();
			//values.put("apnmode", apnmode);
			//values.put("index", index);
			//values.put("preferred", preferred);
			//values.put("onlymodify", onlymodify);
			values.put("protocol", protocol);
			values.put("roaming_protocol", protocol); 
			values.put("name", profilename);
			values.put("apn", apn);
			values.put("authtype", authmode);
			values.put("user", username);
			values.put("password", password);
			String whereClause = "_id=?";
			String[] whereArgs = new String[]{String.valueOf(index)};
			int rowsUpdated = resolver.update(APN_URI, values, whereClause, whereArgs);
			Log.d(TAG, "ZJJ_DEBUG APNUtli setAPN rowsUpdated: " + rowsUpdated);
			if (rowsUpdated > 0) {
				Log.d(TAG, "ZJJ_DEBUG APNUtli setAPN APN updated: " + index);
			} else {
				Log.e(TAG, "ZJJ_DEBUG APNUtli setAPN Failed to update APN");
			}
			
		}
		
		public static void deleteAPN(Context context, int index){
			Log.d(TAG, "ZJJ_DEBUG APNUtli deleteAPN : ");
			// 通过ID删除
			ContentResolver resolver = context.getContentResolver();
			String whereClause = "_id=?";
			String[] whereArgs = new String[]{String.valueOf(index)};
			int deletedRows = resolver.delete(APN_URI, whereClause, whereArgs);

			// 通过名称删除（需先查询匹配的ID）
			/*Cursor cursor = resolver.query(
				APN_URI,
				new String[]{"_id"},
				"name=?",
				new String[]{String.valueOf(index)},
				null
			);
			if (cursor != null && cursor.moveToFirst()) {
				String id = cursor.getString(cursor.getColumnIndex("_id"));
				resolver.delete(APN_URI, "_id=?", new String[]{id});
				cursor.close();
			}*/
		}
		
		/**
		 * 获取当前正在使用的 APN 的 id 和 editable。
		 *
		 * @param resolver ContentResolver 对象。
		 * @return 返回当前 APN 的 id 和 editable 的 JSON 对象。如果查询失败或数据为空，返回空的 JSON 对象。
		 */
		public static JSONObject getCurrentApn(ContentResolver resolver) {
			JSONObject currentApnJson = new JSONObject();
			Cursor cursor = null;
			

			try {
				// 查询当前 APN
				cursor = resolver.query(DEFAULT_APN_URI, null, null, null, null);
				
				// 检查 Cursor 是否为空
				if (cursor == null) {
					Log.e(TAG, "ZJJ_DEBUG APNUtli getCurrentApn: Cursor is null, query failed.");
					return currentApnJson;
				}
				
				// 打印所有列名
				String[] columnNames = cursor.getColumnNames();
				for (String columnName : columnNames) {
					Log.d(TAG, "ZJJ_DEBUG APNUtli getCurrentApn Column: " + columnName);
				}
				
				// 打印 Cursor 的所有内容
				logCursorData(cursor);
				
				// 检查是否有数据
				if (cursor.getCount() > 0) {
					// 获取列索引
					int idIndex = cursor.getInt(cursor.getColumnIndex("_id"));
					int editableIndex = cursor.getInt(cursor.getColumnIndex("edited"));
					
					Log.d(TAG, "ZJJ_DEBUG APNUtli getCurrentApn idIndex: " + idIndex + ", editableIndex = " + editableIndex);

					// 检查列索引是否有效
					if (idIndex != -1 && editableIndex != -1) {
						currentApnJson.put("curid", idIndex);
						currentApnJson.put("readonly", editableIndex);
					} else {
						Log.e(TAG, "ZJJ_DEBUG APNUtli getCurrentApn: Column '_id' or 'edited' not found.");
					}
				} else {
					Log.e(TAG, "ZJJ_DEBUG APNUtli getCurrentApn: No data found in Cursor. cursor.getCount() = " 
					+ cursor.getCount() + ", cursor.moveToFirst() = " + cursor.moveToFirst());
				}
			} catch (JSONException e) {
				Log.e(TAG, "ZJJ_DEBUG APNUtli getCurrentApn: Error creating JSON: " + e.getMessage());
			} finally {
				cursor.close();
			}
			return currentApnJson;
		}
		
		/**
		 * 查询设备上所有配置的 APN，并返回当前正在使用的 APN 的 id 和 editable。
		 *
		 * @param context Android 上下文对象。
		 * @return 返回一个 Bundle，包含查询结果和 JSON 数据：
		 *         - "status"：整型值（0 表示成功，-1 表示失败）。
		 *         - "apns"：所有 APN 的配置信息（JSON 字符串）。
		 *         - "current_apn"：当前正在使用的 APN 的 id 和 editable（JSON 字符串）。
		 */
		public static Bundle getAllApns(Context context) {
			ContentResolver resolver = context.getContentResolver();
			JSONArray apnArray = new JSONArray();
			JSONObject currentApnJson = new JSONObject();
			JSONObject resultJson = new JSONObject(); // 用于存储合并后的结果
			Bundle resultBundle = new Bundle();

			// 查询 carriers 表
			Cursor cursor = resolver.query(SIM_APN_URI, null, null, null, null);
			if (cursor == null) {
				Log.e(TAG, "ZJJ_DEBUG APNUtli Failed to query APNs: cursor is null");
				resultBundle.putInt("retcode", -1); // 失败状态
				resultBundle.putString("profileList", ""); // 空 JSON 数据
				return resultBundle;
			}

			try {
				// 遍历查询结果，将每个 APN 的配置添加到 JSON 数组
				while (cursor.moveToNext()) {
					JSONObject apnJson = new JSONObject();
					apnJson.put("index", cursor.getInt(cursor.getColumnIndex("_id")));
					apnJson.put("iptype", cursor.getString(cursor.getColumnIndex("protocol")));
					apnJson.put("profilename", cursor.getString(cursor.getColumnIndex("name")));
					apnJson.put("apn", cursor.getString(cursor.getColumnIndex("apn")));
					apnJson.put("authmode", cursor.getInt(cursor.getColumnIndex("authtype")));
					apnJson.put("user", cursor.getString(cursor.getColumnIndex("user")));
					apnJson.put("password", cursor.getString(cursor.getColumnIndex("password")));
					//apnJson.put("type", cursor.getString(cursor.getColumnIndex("type")));
					//apnJson.put("mcc", cursor.getString(cursor.getColumnIndex("mcc")));
					//apnJson.put("mnc", cursor.getString(cursor.getColumnIndex("mnc")));
					//apnJson.put("numeric", cursor.getString(cursor.getColumnIndex("numeric")));
					//apnJson.put("roaming_protocol", cursor.getString(cursor.getColumnIndex("roaming_protocol")));
					//apnJson.put("carrier_enabled", cursor.getInt(cursor.getColumnIndex("carrier_enabled")));
					//apnJson.put("bearer", cursor.getInt(cursor.getColumnIndex("bearer")));
					//apnJson.put("editable", cursor.getInt(cursor.getColumnIndex("editable")));
					apnArray.put(apnJson); // 将 APN 配置添加到 JSON 数组
				}

				// 查询当前正在使用的 APN
				currentApnJson = getCurrentApn(resolver);

				// 将结果合并到一个 JSON 对象中
				resultJson.put("retcode", 0); // 成功状态
				resultJson.put("apns", apnArray); // 所有 APN 的配置
				resultJson.put("current_apn", currentApnJson); // 当前 APN 的 id 和 editable

				// 将合并后的 JSON 对象存储到 Bundle
				resultBundle.putInt("retcode", 0); // 成功状态
				resultBundle.putString("profileList", resultJson.toString()); // 合并后的 JSON 数据
			} catch (JSONException e) {
				Log.e(TAG, "ZJJ_DEBUG APNUtli Error creating JSON: " + e.getMessage());
				resultBundle.putInt("retcode", -1); // 失败状态
				resultBundle.putString("profileList", ""); // 空 JSON 数据
			} finally {
				cursor.close(); // 关闭 Cursor
			}

			// 打印合并后的 JSON 数据
			Log.d(TAG, "ZJJ_DEBUG APNUtli getAllApns resultJson: " + resultJson.toString());

			return resultBundle;
		}
		
		public static String array2JsonString(String[] value) {
			String jsonString = null;
			if (value != null) {
				JSONArray array = new JSONArray();
				for (String item : value) {
					array.put(item);
				}
				jsonString = array.toString();
			}
			return jsonString;
		}
		
		/**
		 * 打印 Cursor 中的所有数据。
		 *
		 * @param cursor 需要打印的 Cursor 对象。
		 */
		public static void logCursorData(Cursor cursor) {
			if (cursor == null) {
				Log.e(TAG, "ZJJ_DEBUG APNUtli logCursorData: Cursor is null.");
				return;
			}

			// 获取列名
			String[] columnNames = cursor.getColumnNames();
			if (columnNames == null || columnNames.length == 0) {
				Log.e(TAG, "ZJJ_DEBUG APNUtli logCursorData: No columns found in Cursor.");
				return;
			}

			// 遍历 Cursor 的所有行
			int rowCount = cursor.getCount();
			Log.d(TAG, "ZJJ_DEBUG APNUtli logCursorData: Total rows = " + rowCount);

			for (int i = 0; i < rowCount; i++) {
				if (cursor.moveToPosition(i)) {
					StringBuilder rowData = new StringBuilder("Row " + i + ": ");
					for (String columnName : columnNames) {
						int columnIndex = cursor.getColumnIndex(columnName);
						if (columnIndex != -1) {
							String columnValue = cursor.getString(columnIndex);
							rowData.append(columnName).append("=").append(columnValue).append(", ");
						}
					}
					Log.d(TAG,"ZJJ_DEBUG APNUtli:" + rowData.toString());
				}
			}
		}
		
		/**
		 * 获取当前 SIM 卡的 MCC。
		 *
		 * @param 当前 SIM 卡的 MCC。
		 */
		public static int getSimMcc(Context context) {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				String networkOperator = telephonyManager.getNetworkOperator();
				if (networkOperator != null && networkOperator.length() >= 5) {
					int mcc = Integer.parseInt(networkOperator.substring(0, 3));
					Log.d(TAG, "ZJJ_DEBUG APNUtli MCC: " + mcc );
					return mcc;
				}
			return -1;
		}
		
		/**
		 * 获取当前 SIM 卡的MNC。
		 *
		 * @param 当前 SIM 卡的MNC。
		 */
		public static int getSimMnc(Context context) {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
				String networkOperator = telephonyManager.getNetworkOperator();
				if (networkOperator != null && networkOperator.length() >= 5) {
					int mnc = Integer.parseInt(networkOperator.substring(3));
					Log.d(TAG, "ZJJ_DEBUG APNUtli MNC: " + mnc);
					return mnc;
				}
			return -1;
		}
		
	}
	
	public static class MobileNetworkUtil {
		
		public static final int NETWORK_MODE_AUTOMATIC = 0; // 自动模式
		public static final int NETWORK_MODE_2G = 1;       // 2G only
		public static final int NETWORK_MODE_3G = 2;       // 3G only
		public static final int NETWORK_MODE_4G = 3;       // 4G only
		public static final int NETWORK_MODE_5G = 4;       // 5G only
		
		// 2G
		public static final int RAF_UNKNOWN = (int) TelephonyManager.NETWORK_TYPE_BITMASK_UNKNOWN;
		public static final int RAF_GSM = (int) TelephonyManager.NETWORK_TYPE_BITMASK_GSM;
		public static final int RAF_GPRS = (int) TelephonyManager.NETWORK_TYPE_BITMASK_GPRS;
		public static final int RAF_EDGE = (int) TelephonyManager.NETWORK_TYPE_BITMASK_EDGE;
		public static final int RAF_IS95A = (int) TelephonyManager.NETWORK_TYPE_BITMASK_CDMA;
		public static final int RAF_IS95B = (int) TelephonyManager.NETWORK_TYPE_BITMASK_CDMA;
		public static final int RAF_1xRTT = (int) TelephonyManager.NETWORK_TYPE_BITMASK_1xRTT;
		// 3G
		public static final int RAF_EVDO_0 = (int) TelephonyManager.NETWORK_TYPE_BITMASK_EVDO_0;
		public static final int RAF_EVDO_A = (int) TelephonyManager.NETWORK_TYPE_BITMASK_EVDO_A;
		public static final int RAF_EVDO_B = (int) TelephonyManager.NETWORK_TYPE_BITMASK_EVDO_B;
		public static final int RAF_EHRPD = (int) TelephonyManager.NETWORK_TYPE_BITMASK_EHRPD;
		public static final int RAF_HSUPA = (int) TelephonyManager.NETWORK_TYPE_BITMASK_HSUPA;
		public static final int RAF_HSDPA = (int) TelephonyManager.NETWORK_TYPE_BITMASK_HSDPA;
		public static final int RAF_HSPA = (int) TelephonyManager.NETWORK_TYPE_BITMASK_HSPA;
		public static final int RAF_HSPAP = (int) TelephonyManager.NETWORK_TYPE_BITMASK_HSPAP;
		public static final int RAF_UMTS = (int) TelephonyManager.NETWORK_TYPE_BITMASK_UMTS;
		public static final int RAF_TD_SCDMA = (int) TelephonyManager.NETWORK_TYPE_BITMASK_TD_SCDMA;
		// 4G
		public static final int RAF_LTE = (int) TelephonyManager.NETWORK_TYPE_BITMASK_LTE;
		public static final int RAF_LTE_CA = (int) TelephonyManager.NETWORK_TYPE_BITMASK_LTE_CA;

		// 5G
		public static final int RAF_NR = (int) TelephonyManager.NETWORK_TYPE_BITMASK_NR;

		// Grouping of RAFs
		// 2G
		private static final int GSM = RAF_GSM | RAF_GPRS | RAF_EDGE;
		private static final int CDMA = RAF_IS95A | RAF_IS95B | RAF_1xRTT;
		// 3G
		private static final int EVDO = RAF_EVDO_0 | RAF_EVDO_A | RAF_EVDO_B | RAF_EHRPD;
		private static final int HS = RAF_HSUPA | RAF_HSDPA | RAF_HSPA | RAF_HSPAP;
		private static final int WCDMA = HS | RAF_UMTS;
		// 4G
		private static final int LTE = RAF_LTE | RAF_LTE_CA;

		// 5G
		private static final int NR = RAF_NR;
		
		/**
		 * Copied from {@link android.telephony.TelephonyManager}
		 */
        // Network modes are in turn copied from RILConstants
        // with one difference: NETWORK_MODE_CDMA is named NETWORK_MODE_CDMA_EVDO

        public static final int NETWORK_MODE_UNKNOWN = -1;

        /**
         * GSM, WCDMA (WCDMA preferred)
         */
        public static final int NETWORK_MODE_WCDMA_PREF = 0;

        /**
         * GSM only
         */
        public static final int NETWORK_MODE_GSM_ONLY = 1;

        /**
         * WCDMA only
         */
        public static final int NETWORK_MODE_WCDMA_ONLY = 2;

        /**
         * GSM, WCDMA (auto mode, according to PRL)
         */
        public static final int NETWORK_MODE_GSM_UMTS = 3;

        /**
         * CDMA and EvDo (auto mode, according to PRL)
         * this is NETWORK_MODE_CDMA in RILConstants.java
         */
        public static final int NETWORK_MODE_CDMA_EVDO = 4;

        /**
         * CDMA only
         */
        public static final int NETWORK_MODE_CDMA_NO_EVDO = 5;

        /**
         * EvDo only
         */
        public static final int NETWORK_MODE_EVDO_NO_CDMA = 6;

        /**
         * GSM, WCDMA, CDMA, and EvDo (auto mode, according to PRL)
         */
        public static final int NETWORK_MODE_GLOBAL = 7;

        /**
         * LTE, CDMA and EvDo
         */
        public static final int NETWORK_MODE_LTE_CDMA_EVDO = 8;

        /**
         * LTE, GSM and WCDMA
         */
        public static final int NETWORK_MODE_LTE_GSM_WCDMA = 9;

        /**
         * LTE, CDMA, EvDo, GSM, and WCDMA
         */
        public static final int NETWORK_MODE_LTE_CDMA_EVDO_GSM_WCDMA = 10;

        /**
         * LTE only mode.
         */
        public static final int NETWORK_MODE_LTE_ONLY = 11;

        /**
         * LTE and WCDMA
         */
        public static final int NETWORK_MODE_LTE_WCDMA = 12;

        /**
         * TD-SCDMA only
         */
        public static final int NETWORK_MODE_TDSCDMA_ONLY = 13;

        /**
         * TD-SCDMA and WCDMA
         */
        public static final int NETWORK_MODE_TDSCDMA_WCDMA = 14;

        /**
         * LTE and TD-SCDMA
         */
        public static final int NETWORK_MODE_LTE_TDSCDMA = 15;

        /**
         * TD-SCDMA and GSM
         */
        public static final int NETWORK_MODE_TDSCDMA_GSM = 16;

        /**
         * TD-SCDMA, GSM and LTE
         */
        public static final int NETWORK_MODE_LTE_TDSCDMA_GSM = 17;

        /**
         * TD-SCDMA, GSM and WCDMA
         */
        public static final int NETWORK_MODE_TDSCDMA_GSM_WCDMA = 18;

        /**
         * LTE, TD-SCDMA and WCDMA
         */
        public static final int NETWORK_MODE_LTE_TDSCDMA_WCDMA = 19;

        /**
         * LTE, TD-SCDMA, GSM, and WCDMA
         */
        public static final int NETWORK_MODE_LTE_TDSCDMA_GSM_WCDMA = 20;

        /**
         * TD-SCDMA, CDMA, EVDO, GSM and WCDMA
         */
        public static final int NETWORK_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA = 21;

        /**
         * LTE, TDCSDMA, CDMA, EVDO, GSM and WCDMA
         */
        public static final int NETWORK_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA = 22;

        /**
         * NR 5G only mode
         */
        public static final int NETWORK_MODE_NR_ONLY = 23;

        /**
         * NR 5G, LTE
         */
        public static final int NETWORK_MODE_NR_LTE = 24;

        /**
         * NR 5G, LTE, CDMA and EvDo
         */
        public static final int NETWORK_MODE_NR_LTE_CDMA_EVDO = 25;

        /**
         * NR 5G, LTE, GSM and WCDMA
         */
        public static final int NETWORK_MODE_NR_LTE_GSM_WCDMA = 26;

        /**
         * NR 5G, LTE, CDMA, EvDo, GSM and WCDMA
         */
        public static final int NETWORK_MODE_NR_LTE_CDMA_EVDO_GSM_WCDMA = 27;

        /**
         * NR 5G, LTE and WCDMA
         */
        public static final int NETWORK_MODE_NR_LTE_WCDMA = 28;

        /**
         * NR 5G, LTE and TDSCDMA
         */
        public static final int NETWORK_MODE_NR_LTE_TDSCDMA = 29;

        /**
         * NR 5G, LTE, TD-SCDMA and GSM
         */
        public static final int NETWORK_MODE_NR_LTE_TDSCDMA_GSM = 30;

        /**
         * NR 5G, LTE, TD-SCDMA, WCDMA
         */
        public static final int NETWORK_MODE_NR_LTE_TDSCDMA_WCDMA = 31;

        /**
         * NR 5G, LTE, TD-SCDMA, GSM and WCDMA
         */
        public static final int NETWORK_MODE_NR_LTE_TDSCDMA_GSM_WCDMA = 32;

        /**
         * NR 5G, LTE, TD-SCDMA, CDMA, EVDO, GSM and WCDMA
         */
        public static final int NETWORK_MODE_NR_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA = 33;
        
		/* CDMA,GSM(2G Global) */
		public static final int NETWORK_MODE_CDMA_GSM = 103; 
		
		/**
		 *  Imported from {@link android.telephony.RadioAccessFamily}
		 */
		public static int getNetworkTypeFromRaf(int raf) {
			raf = getAdjustedRaf(raf);

			switch (raf) {
				case (GSM | WCDMA):
					return NETWORK_MODE_WCDMA_PREF;
				case GSM:
					return NETWORK_MODE_GSM_ONLY;
				case WCDMA:
					return NETWORK_MODE_WCDMA_ONLY;
				case (CDMA | EVDO):
					return NETWORK_MODE_CDMA_EVDO;
				case (LTE | CDMA | EVDO):
					return NETWORK_MODE_LTE_CDMA_EVDO;
				case (LTE | GSM | WCDMA):
					return NETWORK_MODE_LTE_GSM_WCDMA;
				case (LTE | CDMA | EVDO | GSM | WCDMA):
					return NETWORK_MODE_LTE_CDMA_EVDO_GSM_WCDMA;
				case LTE:
					return NETWORK_MODE_LTE_ONLY;
				case (LTE | WCDMA):
					return NETWORK_MODE_LTE_WCDMA;
				case CDMA:
					return NETWORK_MODE_CDMA_NO_EVDO;
				case EVDO:
					return NETWORK_MODE_EVDO_NO_CDMA;
				case (GSM | WCDMA | CDMA | EVDO):
					return NETWORK_MODE_GLOBAL;
				case RAF_TD_SCDMA:
					return NETWORK_MODE_TDSCDMA_ONLY;
				case (RAF_TD_SCDMA | WCDMA):
					return NETWORK_MODE_TDSCDMA_WCDMA;
				case (LTE | RAF_TD_SCDMA):
					return NETWORK_MODE_LTE_TDSCDMA;
				case (RAF_TD_SCDMA | GSM):
					return NETWORK_MODE_TDSCDMA_GSM;
				case (LTE | RAF_TD_SCDMA | GSM):
					return NETWORK_MODE_LTE_TDSCDMA_GSM;
				case (RAF_TD_SCDMA | GSM | WCDMA):
					return NETWORK_MODE_TDSCDMA_GSM_WCDMA;
				case (LTE | RAF_TD_SCDMA | WCDMA):
					return NETWORK_MODE_LTE_TDSCDMA_WCDMA;
				case (LTE | RAF_TD_SCDMA | GSM | WCDMA):
					return NETWORK_MODE_LTE_TDSCDMA_GSM_WCDMA;
				case (RAF_TD_SCDMA | CDMA | EVDO | GSM | WCDMA):
					return NETWORK_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA;
				case (LTE | RAF_TD_SCDMA | CDMA | EVDO | GSM | WCDMA):
					return NETWORK_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA;
				case (NR):
					return NETWORK_MODE_NR_ONLY;
				case (NR | LTE):
					return NETWORK_MODE_NR_LTE;
				case (NR | LTE | CDMA | EVDO):
					return NETWORK_MODE_NR_LTE_CDMA_EVDO;
				case (NR | LTE | GSM | WCDMA):
					return NETWORK_MODE_NR_LTE_GSM_WCDMA;
				case (NR | LTE | CDMA | EVDO | GSM | WCDMA):
					return NETWORK_MODE_NR_LTE_CDMA_EVDO_GSM_WCDMA;
				case (NR | LTE | WCDMA):
					return NETWORK_MODE_NR_LTE_WCDMA;
				case (NR | LTE | RAF_TD_SCDMA):
					return NETWORK_MODE_NR_LTE_TDSCDMA;
				case (NR | LTE | RAF_TD_SCDMA | GSM):
					return NETWORK_MODE_NR_LTE_TDSCDMA_GSM;
				case (NR | LTE | RAF_TD_SCDMA | WCDMA):
					return NETWORK_MODE_NR_LTE_TDSCDMA_WCDMA;
				case (NR | LTE | RAF_TD_SCDMA | GSM | WCDMA):
					return NETWORK_MODE_NR_LTE_TDSCDMA_GSM_WCDMA;
				case (NR | LTE | RAF_TD_SCDMA | CDMA | EVDO | GSM | WCDMA):
					return NETWORK_MODE_NR_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA;
				/// M: Add for revising the entry values. @{
				case (CDMA | GSM):
					return NETWORK_MODE_CDMA_GSM;
				/// @}
				default:
					return NETWORK_MODE_UNKNOWN;
			}
		}
		
		
		/**
		 *  Imported from {@link android.telephony.RadioAccessFamily}
		 */
		public static long getRafFromNetworkType(int type) {
			switch (type) {
				case NETWORK_MODE_WCDMA_PREF:
					return GSM | WCDMA;
				case NETWORK_MODE_GSM_ONLY:
					return GSM;
				case NETWORK_MODE_WCDMA_ONLY:
					return WCDMA;
				case NETWORK_MODE_GSM_UMTS:
					return GSM | WCDMA;
				case NETWORK_MODE_CDMA_EVDO:
					return CDMA | EVDO;
				case NETWORK_MODE_LTE_CDMA_EVDO:
					return LTE | CDMA | EVDO;
				case NETWORK_MODE_LTE_GSM_WCDMA:
					return LTE | GSM | WCDMA;
				case NETWORK_MODE_LTE_CDMA_EVDO_GSM_WCDMA:
					return LTE | CDMA | EVDO | GSM | WCDMA;
				case NETWORK_MODE_LTE_ONLY:
					return LTE;
				case NETWORK_MODE_LTE_WCDMA:
					return LTE | WCDMA;
				case NETWORK_MODE_CDMA_NO_EVDO:
					return CDMA;
				case NETWORK_MODE_EVDO_NO_CDMA:
					return EVDO;
				case NETWORK_MODE_GLOBAL:
					return GSM | WCDMA | CDMA | EVDO;
				case NETWORK_MODE_TDSCDMA_ONLY:
					return RAF_TD_SCDMA;
				case NETWORK_MODE_TDSCDMA_WCDMA:
					return RAF_TD_SCDMA | WCDMA;
				case NETWORK_MODE_LTE_TDSCDMA:
					return LTE | RAF_TD_SCDMA;
				case NETWORK_MODE_TDSCDMA_GSM:
					return RAF_TD_SCDMA | GSM;
				case NETWORK_MODE_LTE_TDSCDMA_GSM:
					return LTE | RAF_TD_SCDMA | GSM;
				case NETWORK_MODE_TDSCDMA_GSM_WCDMA:
					return RAF_TD_SCDMA | GSM | WCDMA;
				case NETWORK_MODE_LTE_TDSCDMA_WCDMA:
					return LTE | RAF_TD_SCDMA | WCDMA;
				case NETWORK_MODE_LTE_TDSCDMA_GSM_WCDMA:
					return LTE | RAF_TD_SCDMA | GSM | WCDMA;
				case NETWORK_MODE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
					return RAF_TD_SCDMA | CDMA | EVDO | GSM | WCDMA;
				case NETWORK_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
					return LTE | RAF_TD_SCDMA | CDMA | EVDO | GSM | WCDMA;
				case NETWORK_MODE_NR_ONLY:
					return NR;
				case NETWORK_MODE_NR_LTE:
					return NR | LTE;
				case NETWORK_MODE_NR_LTE_CDMA_EVDO:
					return NR | LTE | CDMA | EVDO;
				case NETWORK_MODE_NR_LTE_GSM_WCDMA:
					return NR | LTE | GSM | WCDMA;
				case NETWORK_MODE_NR_LTE_CDMA_EVDO_GSM_WCDMA:
					return NR | LTE | CDMA | EVDO | GSM | WCDMA;
				case NETWORK_MODE_NR_LTE_WCDMA:
					return NR | LTE | WCDMA;
				case NETWORK_MODE_NR_LTE_TDSCDMA:
					return NR | LTE | RAF_TD_SCDMA;
				case NETWORK_MODE_NR_LTE_TDSCDMA_GSM:
					return NR | LTE | RAF_TD_SCDMA | GSM;
				case NETWORK_MODE_NR_LTE_TDSCDMA_WCDMA:
					return NR | LTE | RAF_TD_SCDMA | WCDMA;
				case NETWORK_MODE_NR_LTE_TDSCDMA_GSM_WCDMA:
					return NR | LTE | RAF_TD_SCDMA | GSM | WCDMA;
				case NETWORK_MODE_NR_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA:
					return NR | LTE | RAF_TD_SCDMA | CDMA | EVDO | GSM | WCDMA;
				/// M: Add for revising the entry values. @{
				case NETWORK_MODE_CDMA_GSM:
					return CDMA | GSM;
				/// @}
				default:
					return RAF_UNKNOWN;
			}
		}
		
		/**
		 *  Imported from {@link android.telephony.RadioAccessFamily}
		 */
		private static int getAdjustedRaf(int raf) {
			raf = ((GSM & raf) > 0) ? (GSM | raf) : raf;
			raf = ((WCDMA & raf) > 0) ? (WCDMA | raf) : raf;
			raf = ((CDMA & raf) > 0) ? (CDMA | raf) : raf;
			raf = ((EVDO & raf) > 0) ? (EVDO | raf) : raf;
			raf = ((LTE & raf) > 0) ? (LTE | raf) : raf;
			raf = ((NR & raf) > 0) ? (NR | raf) : raf;
			return raf;
		}
		
		/**
		 *  设置网络模式
		 */
		public static void setNetworkMode(Context context,int mode) {
			TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if(mode == 2){
				mTelephonyManager.setAllowedNetworkTypesForReason(
					TelephonyManager.ALLOWED_NETWORK_TYPES_REASON_USER,
					getRafFromNetworkType(NETWORK_MODE_NR_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA));  // 5G
			}else if(mode == 3){
				mTelephonyManager.setAllowedNetworkTypesForReason(
					TelephonyManager.ALLOWED_NETWORK_TYPES_REASON_USER,
					getRafFromNetworkType(NETWORK_MODE_LTE_CDMA_EVDO_GSM_WCDMA));        //4G
			}else if(mode == 4){
				mTelephonyManager.setAllowedNetworkTypesForReason(
					TelephonyManager.ALLOWED_NETWORK_TYPES_REASON_USER,      //3G
					getRafFromNetworkType(NETWORK_MODE_GLOBAL));
			}else{
				Log.d(TAG, "ZJJ_DEBUG MobileNetworkUtil setNetworkMode failed" );
			}
		}
		
		/**
		 *  获取网络模式
		 */
		public static int getNetworkMode(Context context) {
			TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			int networkMode = getNetworkTypeFromRaf(
                    (int) mTelephonyManager.getAllowedNetworkTypesForReason(
                            TelephonyManager.ALLOWED_NETWORK_TYPES_REASON_USER));
			Log.d(TAG, "ZJJ_DEBUG MobileNetworkUtil getPreferredNetworkMode: " + networkMode);
			if (networkMode == NETWORK_MODE_NR_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA){
				return 2;   //5G
			}else if (networkMode == NETWORK_MODE_LTE_CDMA_EVDO_GSM_WCDMA){
				return 3;      //4G
			}else if (networkMode == NETWORK_MODE_GLOBAL){
				return 4;       //3G
			}
			return -1;
		}
		
		/**
		 * 检查设备是否有 SIM 卡
		 *
		 * @param context 上下文
		 * @return true 表示有 SIM 卡，false 表示没有 SIM 卡
		 */
		public static boolean hasSimCard(Context context) {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager == null) {
				return false; // 无法获取 TelephonyManager
			}
			int simState = telephonyManager.getSimState();
			return simState != TelephonyManager.SIM_STATE_ABSENT;
		}
		
		/**
		 * 设置当前网络搜索模式（0-自动，1-手动）
		 *
		 */
		public static void setNetworkSearch(Context context, int mode) {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if(mode == 0){
				telephonyManager.setNetworkSelectionModeAutomatic();
			}else if(mode == 1){
				Log.d(TAG, "ZJJ_DEBUG MobileNetworkUtil setNetworkSearch 11111");
			}else{
				Log.e(TAG, "ZJJ_DEBUG MobileNetworkUtil setNetworkSearch failed");
			}			
		}
		 
		 
		/**
		 * 获取当前网络搜索模式（0-自动，1-手动）
		 *
		 */
		 
		public static int getNetworkSearch(Context context) {
			
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephonyManager == null) {
				Log.e(TAG, "ZJJ_DEBUG MobileNetworkUtil getNetworkSearch TelephonyManager is null");
				return -1;
			}
			ServiceState serviceState = telephonyManager.getServiceState();
			if (serviceState == null) {
				Log.e(TAG, "ZJJ_DEBUG MobileNetworkUtil getNetworkSearch ServiceState is null");
				return -1;
			}
			
			boolean isManualSelection = serviceState.getIsManualSelection();
			Log.d(TAG, "ZJJ_DEBUG MobileNetworkUtil getNetworkSearch Network search mode: " + (isManualSelection ? "Manual" : "Auto"));
			return isManualSelection ? 1 : 0;
		}
		
		/**
		 * 获取网络运营商名称
		 * <p>中国移动、如中国联通、中国电信</p>
		 *
		 * @return 运营商名称
		 */
		public static String getNetworkOperatorName(Context context) {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String opeType = "unknown";
			String noSim = "noSim";
			// No sim
			if (!hasSim(context)) {
				return noSim;
			}
			int simState = telephonyManager.getSimState();
			String operator = telephonyManager.getSimOperator();
			String simOperatorName = telephonyManager.getSimOperatorName();
			Log.d(TAG, "ZJJ_DEBUG MobileNetworkUtil getNetworkOperatorName simState: " + simState 
			+ ", operator = " + operator + ", simOperatorName = " + simOperatorName);
			if ("46001".equals(operator) || "46006".equals(operator) || "46009".equals(operator)) {
				opeType = "中国联通";
			} else if ("46000".equals(operator) || "46002".equals(operator) || "46004".equals(operator) || "46007".equals(operator)) {
				opeType = "中国移动";
	 
			} else if ("46003".equals(operator) || "46005".equals(operator) || "46011".equals(operator)) {
				opeType = "中国电信";
			} else {
				opeType = "unknown";
			}
			return opeType;
		}
		
		 /**
		 * 检查手机是否有sim卡
		 */
		private static boolean hasSim(Context context) {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			String operator = telephonyManager.getSimOperator();
			if (TextUtils.isEmpty(operator)) {
				return false;
			}
			return true;
		}
		
		/**
		 * 获取漫游状态
		 */
		public static int getRoamStatus(Context context) {
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			ServiceState serviceState = telephonyManager.getServiceState();
			boolean result1 = telephonyManager.isNetworkRoaming();
			boolean result2 = serviceState.getRoaming();
			if (telephonyManager == null || serviceState == null) {
				return 0;
			}
			Log.d(TAG, "ZJJ_DEBUG MobileNetworkUtil getRoamStatus result1: " + result1 + ", result2 = " + result2);
			if(result1 && result2){
				return 2;
			}else if(!result1 || !result2){
				return 1;
			}
			return 0;
		}
		
		/**
		 * 获取网络状态（是否可用）
		 */
		public static int getNetworkStatus(Context context) {

			boolean isMobileConn = false;
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			isMobileConn = false;
			if(networkInfo == null || connMgr == null){
				return 0;
			}
			isMobileConn = networkInfo.isConnected();
			Log.d(TAG, "ZJJ_DEBUG MobileNetworkUtil getNetworkStatus Mobile connected: " + isMobileConn);
			return isMobileConn ? 2 : 1;
		}
		
		/**
		 * 设置5G的NSA和SA模式
		 *
		 */
		private static final String AT_CMD_5G = "+E5GOPT";
		private static final int MSG_SET_5G_CMD = 102;
		private static int current_selected_phone = 0;
		
		private static void set5G(int mode) {
			Log.d(TAG, "ZJJ_DEBUG MobileNetworkUtil set5G mode: " + mode);
			if(mode == 0){
				
			}else if(mode == 1){
				sendAtCommand("AT" + AT_CMD_5G + "=5", "", MSG_SET_5G_CMD);
			}else if(mode == 2){
				sendAtCommand("AT" + AT_CMD_5G + "=3", "", MSG_SET_5G_CMD);
			}else{
				Log.d(TAG, "ZJJ_DEBUG MobileNetworkUtil set5G mode failed ");
			}	
			
			// sa and nsa on
			/*if (sa_sw && nsa_sw) {
				sendAtCommand("AT" + AT_CMD_5G + "=7", "", MSG_SET_5G_CMD);
				showToast("enabling SA and NSA");
			// only sa on
			} else if (sa_sw) {
				sendAtCommand("AT" + AT_CMD_5G + "=3", "", MSG_SET_5G_CMD);
				showToast("enabling SA and disabling NSA");
			// only nsa on
			} else if (nsa_sw) {
				sendAtCommand("AT" + AT_CMD_5G + "=5", "", MSG_SET_5G_CMD);
				showToast("disabling SA and enabling NSA");
			// sa and nsa off
			} else {
				sendAtCommand("AT" + AT_CMD_5G + "=1", "", MSG_SET_5G_CMD);
				showToast("disabling SA and disabling NSA");
			}*/
		}
		
		private static void sendAtCommand(String cmd, String resp, int msg) {
			sendCommand(new String[]{cmd, resp}, msg);
		}

		private static void sendCommand(String[] command, int msg) {
			Log.d(TAG, "ZJJ_DEBUG MobileNetworkUtil sendCommand: " + command[0]);
			//EmUtils.invokeOemRilRequestStringsEm(current_selected_phone, command, mCommandHander.obtainMessage(msg));
		}
		
		public static void invokeOemRilRequestStringsEm(int phoneid, String[] command, Message response) {
			//if (OEM_RIL_REQUEST_MODE == OEM_RIL_REQUEST_HIDL) {
			//	invokeOemRilRequestStringsEmHidl(phoneid, command, response);
			//} else {
			//	invokeOemRilRequestStringsEmPhone(phoneid, command, response);
			//}
		}
		
		
		
	
	
	}
}
