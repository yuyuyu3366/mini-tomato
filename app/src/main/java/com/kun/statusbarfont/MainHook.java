package com.kun.statusbarfont;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "StatusBarFont";

    private static final String FONT_PATH = "/data/local/tmp/statusbar_5g.ttf";

    private static final boolean DEBUG = true;

    private static final String[] KEYWORDS = {
            "SignalDrawable",
            "Telephony",
            "MobileIcon",
            "NetworkType",
            "SignalCluster",
            "StatusBarMobile",
            "MobileSignalController",
            "SignalTileView"
    };

    private Typeface customTypeface;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!"com.android.systemui".equals(lpparam.packageName)) {
            return;
        }

        if (!DEBUG) {
            File fontFile = new File(FONT_PATH);
            if (!fontFile.exists()) {
                Log.e(TAG, "字体文件不存在: " + FONT_PATH);
                return;
            }
            try {
                customTypeface = Typeface.createFromFile(fontFile);
            } catch (Throwable t) {
                Log.e(TAG, "字体加载失败: " + FONT_PATH, t);
                return;
            }
        }

        XposedHelpers.findAndHookMethod(Paint.class, "setTypeface", Typeface.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String hitClass = findMatchingCaller();

                        if (DEBUG) {
                            if (hitClass != null) {
                                Log.i(TAG, "命中关键词类: " + hitClass);
                            }
                            return;
                        }

                        if (hitClass != null) {
                            param.args[0] = customTypeface;
                        }
                    }
                });
    }

    private String findMatchingCaller() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            for (String keyword : KEYWORDS) {
                if (className.contains(keyword)) {
                    return className;
                }
            }
        }
        return null;
    }
}
