package com.example.safesteps;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class GuardAccessibilityService extends AccessibilityService {
    private final Handler handler = new Handler();
    private WindowManager windowManager;
    private LinearLayout protectionView;
    private long lastBlock;
    private boolean blocking;

    @Override public void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || !getSharedPreferences(MainActivity.PREFS, 0)
                .getBoolean(MainActivity.ENABLED, false)) return;

        CharSequence packageName = event.getPackageName();
        if (packageName == null || getPackageName().contentEquals(packageName)) return;

        String pkg = packageName.toString();
        if (isForbiddenPackage(pkg) || isAccessibilityArea(event) || isSafeStepsAppInfo(event)) {
            blockNow();
        }
    }

    private boolean isForbiddenPackage(String pkg) {
        return pkg.equals("com.android.packageinstaller")
                || pkg.equals("com.google.android.packageinstaller")
                || pkg.equals("com.android.permissioncontroller");
    }

    private boolean isAccessibilityArea(AccessibilityEvent event) {
        if (!"com.android.settings".equals(String.valueOf(event.getPackageName()))) return false;
        String cls = lower(event.getClassName());
        String text = lower(collectEventText(event));
        return cls.contains("accessibility")
                || cls.contains("installedaccessibility")
                || cls.contains("accessibilitysettings")
                || text.contains("נגישות")
                || text.contains("accessibility");
    }

    private boolean isSafeStepsAppInfo(AccessibilityEvent event) {
        if (!"com.android.settings".equals(String.valueOf(event.getPackageName()))) return false;
        String cls = lower(event.getClassName());
        String text = lower(collectEventText(event));
        boolean detailsScreen = cls.contains("appdetails")
                || cls.contains("installedappdetails")
                || cls.contains("manageapplications")
                || text.contains("פרטי האפליקציה")
                || text.contains("פרטי יישום")
                || text.contains("app info")
                || text.contains("app details");
        return detailsScreen && (text.contains("safe steps") || text.contains("safesteps"));
    }

    private String collectEventText(AccessibilityEvent event) {
        StringBuilder b = new StringBuilder();
        for (CharSequence c : event.getText()) if (c != null) b.append(' ').append(c);
        if (event.getContentDescription() != null) b.append(' ').append(event.getContentDescription());
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root != null) {
            appendNodeText(root, b, 0);
            root.recycle();
        }
        return b.toString();
    }

    private void appendNodeText(AccessibilityNodeInfo node, StringBuilder b, int depth) {
        if (node == null || depth > 30) return;
        CharSequence text = node.getText();
        CharSequence desc = node.getContentDescription();
        if (text != null) b.append(' ').append(text);
        if (desc != null) b.append(' ').append(desc);
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                appendNodeText(child, b, depth + 1);
                child.recycle();
            }
        }
    }

    private String lower(CharSequence value) {
        return String.valueOf(value).toLowerCase(Locale.ROOT);
    }

    private void blockNow() {
        long now = System.currentTimeMillis();
        if (blocking || now - lastBlock < 700) return;
        lastBlock = now;
        blocking = true;

        // First put the protection page above the forbidden window, so it cannot be touched.
        showProtectionOverlay();

        // Then close the forbidden window and return to the home screen.
        handler.postDelayed(() -> {
            performGlobalAction(GLOBAL_ACTION_BACK);
            handler.postDelayed(() -> performGlobalAction(GLOBAL_ACTION_HOME), 60);
        }, 10);

        handler.postDelayed(() -> {
            removeProtectionOverlay();
            blocking = false;
        }, 2700);
    }

    private void showProtectionOverlay() {
        if (windowManager == null || protectionView != null) return;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(34), dp(34), dp(34), dp(28));
        root.setClickable(true);
        root.setFocusable(true);
        root.setBackground(gradient(new int[]{Color.rgb(8, 27, 58), Color.rgb(0, 116, 142), Color.rgb(86, 42, 145)}, 0));

        ImageView icon = new ImageView(this);
        icon.setImageResource(R.drawable.ic_guard);
        root.addView(icon, new LinearLayout.LayoutParams(dp(128), dp(128)));

        TextView title = label("הפעולה חסומה.", 30, Color.WHITE, true);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(62)));

        TextView message = label("מכשיר זה מוגן על ידי מערכת Safe Steps.", 18, Color.WHITE, false);
        root.addView(message, new LinearLayout.LayoutParams(-1, dp(70)));

        TextView brand = label("Safe Steps", 24, Color.WHITE, true);
        root.addView(brand);
        TextView tagline = label("צעדים בטוחים בשבילך", 14, Color.argb(225, 235, 250, 255), false);
        root.addView(tagline);

        Button back = new Button(this);
        back.setText("חזור");
        back.setTextColor(Color.WHITE);
        back.setTextSize(17);
        back.setAllCaps(false);
        back.setBackground(gradient(new int[]{Color.rgb(26, 198, 183), Color.rgb(38, 112, 214)}, dp(18)));
        back.setOnClickListener(v -> {
            performGlobalAction(GLOBAL_ACTION_HOME);
            removeProtectionOverlay();
        });
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, dp(54));
        bp.setMargins(0, dp(34), 0, 0);
        root.addView(back, bp);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                -1, -1,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        try {
            windowManager.addView(root, lp);
            protectionView = root;
        } catch (RuntimeException ignored) { }
    }

    private TextView label(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private GradientDrawable gradient(int[] colors, float radius) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
        g.setCornerRadius(radius);
        return g;
    }

    private int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    private void removeProtectionOverlay() {
        if (protectionView == null || windowManager == null) return;
        try { windowManager.removeView(protectionView); } catch (RuntimeException ignored) { }
        protectionView = null;
    }

    @Override public void onInterrupt() { removeProtectionOverlay(); blocking = false; }
    @Override public boolean onUnbind(android.content.Intent intent) { removeProtectionOverlay(); return super.onUnbind(intent); }
}
