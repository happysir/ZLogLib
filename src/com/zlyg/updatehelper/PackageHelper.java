package com.zlyg.updatehelper;

import java.io.File;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Environment;

public class PackageHelper {
	private PackageInfo info = null;
	private PackageManager pm;

	public PackageHelper(Context context) {
		pm = context.getPackageManager();
		try {
			info = pm.getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	public int getLocalVersionCode() {
		return info != null ? info.versionCode : Integer.MAX_VALUE;
	}

	public String getLocalVersionName() {
		return info != null ? info.versionName : "";
	}

	public String getAppName() {
		return info != null ? (String) info.applicationInfo.loadLabel(pm) : "";
	}

	public String getPackageName() {
		return info != null ? info.packageName : "";
	}

	public int getAppIcon() {
		return info != null ? info.applicationInfo.icon
				: android.R.drawable.ic_dialog_info;
	}

	public static void install_apk(Context context, String apkPath) {

		Intent installintent = new Intent();
		installintent.setComponent(new ComponentName(
				"com.android.packageinstaller",
				"com.android.packageinstaller.PackageInstallerActivity"));
		installintent.setAction(Intent.ACTION_VIEW);

		installintent.setData(Uri.fromFile(new File(apkPath)));
		context.startActivity(installintent);
	}

	public static void update(Context context, File file){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file)
                , "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
	
}
