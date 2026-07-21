package com.kun.statusbarfont;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("在 LSPosed 里启用本模块并勾选 System UI 作用域，然后重启 System UI 或重启手机。\n\n"
                + "调试步骤：\n"
                + "1. 保持代码里 DEBUG = true，编译安装并启用\n"
                + "2. 重启后执行 adb logcat | grep StatusBarFont\n"
                + "3. 切换到 5G 网络，观察打印出的类名\n"
                + "4. 把命中的类名填进 KEYWORDS，FONT_PATH 改成你的字体路径\n"
                + "5. 把 DEBUG 改成 false，重新编译安装");
        tv.setPadding(32, 64, 32, 32);
        tv.setTextIsSelectable(true);
        setContentView(tv);
    }
}
