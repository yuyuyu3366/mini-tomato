package com.kun.statusbarfont;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "StatusBarFont";
    private static final String FONT_PATH = "/data/local/tmp/statusbar_5g.ttf";
    private static final boolean DEBUG = true;

    private static final String[] KEYWORDS = {
            "SignalDrawable", "Telephony", "MobileIcon", "NetworkType",
            "SignalCluster", "StatusBarMobile", "MobileSignalController", "SignalTileView"
    };

    private Typeface customTypeface;
    private static WeakReference<TextView> clockViewRef;
    private static int currentWeight = 600;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        if (!"com.android.systemui".equals(lpparam.packageName)) {
            return;
        }

        loadCustomFont();
        loadSavedWeight();
        hookFivegFont();
        hookClockFontWeight(lpparam);
        hookApplicationForReceiver();
    }

    private void loadSavedWeight() {
        XSharedPreferences prefs = new XSharedPreferences("com.kun.statusbarfont", MainActivity.PREFS_NAME);
        prefs.makeWorldReadable();
        prefs.reload();
        currentWeight = prefs.getInt(MainActivity.KEY_WEIGHT, 600);
    }

    private void loadCustomFont() {
        if (DEBUG) return;
        File fontFile = new File(FONT_PATH);
        if (!fontFile.exists()) {
            Log.e(TAG, "字体文件不存在: " + FONT_PATH);
            return;
        }
        try {
            customTypeface = Typeface.createFromFile(fontFile);
        } catch (Throwable t) {
            Log.e(TAG, "字体加载失败: " + FONT_PATH, t);
        }
    }

    private void hookFivegFont() {
        XposedHelpers.findAndHookMethod(Paint.class, "setTypeface", Typeface.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String hitClass = findMatchingCaller();
                        if (DEBUG) {
                            if (hitClass != null) Log.i(TAG, "命中关键词类: " + hitClass);
                            return;
                        }
                        if (hitClass != null && customTypeface != null) {
                            param.args[0] = customTypeface;
                        }
                    }
                });
    }

    private void hookClockFontWeight(LoadPackageParam lpparam) {
        try {
            Class<?> clockClass = XposedHelpers.findClass(
                    "com.android.systemui.statusbar.policy.Clock", lpparam.classLoader);

            XposedHelpers.findAndHookMethod(clockClass, "onAttachedToWindow", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    TextView clockView = (TextView) param.thisObject;
                    clockViewRef = new WeakReference<>(clockView);
                    applyWeight(clockView, currentWeight);
                }
            });
        } catch (Throwable t) {
            Log.e(TAG, "Clock class not found", t);
        }
    }

    private void hookApplicationForReceiver() {
        XposedHelpers.findAndHookMethod(Application.class, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Application app = (Application) param.thisObject;
                Context context = app.getApplicationContext();

                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context ctx, Intent intent) {
                        int weight = intent.getIntExtra(MainActivity.EXTRA_WEIGHT, currentWeight);
                        currentWeight = weight;
                        if (clockViewRef != null && clockViewRef.get() != null) {
                            applyWeight(clockViewRef.get(), weight);
                        }
                    }
                };

                context.registerReceiver(receiver,
                        new IntentFilter(MainActivity.ACTION_UPDATE_WEIGHT),
                        Context.RECEIVER_EXPORTED);
            }
        });
    }

    private static void applyWeight(TextView clockView, int weight) {
        try {
            Typeface base = clockView.getTypeface();
            Typeface weighted = Typeface.create(base, weight, false);
            clockView.setTypeface(weighted);
        } catch (Throwable t) {
            Log.e(TAG, "font weight set failed", t);
        }
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
