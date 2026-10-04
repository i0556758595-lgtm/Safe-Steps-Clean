package com.example.safesteps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static final String PREFS = "safe_steps";
    public static final String ENABLED = "protection_enabled";
    public static final String PASSWORD = "admin_password";
    public static final String PASSWORD_TYPE = "password_type";

    private static final String TYPE_PIN = "pin";
    private static final String TYPE_PATTERN = "pattern";

    private LinearLayout root;
    private TextView statusText;
    private TextView passwordTypeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeDefaults();
        buildScreen();
    }

    private void initializeDefaults() {
        android.content.SharedPreferences prefs =
                getSharedPreferences(PREFS, MODE_PRIVATE);

        if (!prefs.contains(PASSWORD)) {
            prefs.edit()
                    .putString(PASSWORD, "1234")
                    .putString(PASSWORD_TYPE, TYPE_PIN)
                    .putBoolean(ENABLED, false)
                    .apply();
        } else if (!prefs.contains(PASSWORD_TYPE)) {
            prefs.edit()
                    .putString(PASSWORD_TYPE, TYPE_PIN)
                    .apply();
        }
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(245, 248, 252));

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(30));

        scroll.addView(root);
        setContentView(scroll);

        buildHeader();
        buildStatusCard();
        buildAccessibilityCard();
        buildButtons();
    }

    private void buildHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, dp(15), 0, dp(20));

        TextView logo = new TextView(this);
        logo.setText("🛡");
        logo.setTextSize(64);
        logo.setGravity(Gravity.CENTER);
        header.addView(logo, matchWrap());

        TextView title = new TextView(this);
        title.setText("S");
        title.setTextSize(48);
        title.setTextColor(Color.rgb(32, 111, 220));
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        header.addView(title, matchWrap());

        TextView name = new TextView(this);
        name.setText("Safe Steps");
        name.setTextSize(30);
        name.setTextColor(Color.rgb(25, 42, 72));
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setGravity(Gravity.CENTER);
        header.addView(name, matchWrap());

        TextView tagline = new TextView(this);
        tagline.setText("צעדים בטוחים בשבילך");
        tagline.setTextSize(17);
        tagline.setTextColor(Color.rgb(82, 101, 125));
        tagline.setGravity(Gravity.CENTER);
        header.addView(tagline, matchWrap());

        LinearLayout.LayoutParams hp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);

        root.addView(header, hp);
    }

    private void buildStatusCard() {
        LinearLayout card = createCard();
        card.setOrientation(LinearLayout.VERTICAL);

        TextView title = text("מצב ההגנה", 21, Color.rgb(25, 42, 72), true);
        card.addView(title, matchWrap());

        statusText = text("", 19, Color.DKGRAY, true);
        statusText.setPadding(0, dp(10), 0, dp(4));
        card.addView(statusText, matchWrap());

        passwordTypeText = text("", 15, Color.rgb(90, 105, 125), false);
        card.addView(passwordTypeText, matchWrap());

        Button toggle = new Button(this);
        toggle.setAllCaps(false);
        toggle.setTextSize(17);
        toggle.setText("שינוי מצב ההגנה");
        toggle.setOnClickListener(v -> toggleProtection());

        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54));
        bp.topMargin = dp(15);

        card.addView(toggle, bp);

        root.addView(card, cardParams());
        updateState();
    }

    private void buildAccessibilityCard() {
        LinearLayout card = createCard();
        card.setOrientation(LinearLayout.VERTICAL);

        TextView title = text(
                "שירות ההגנה",
                21,
                Color.rgb(25, 42, 72),
                true
        );
        card.addView(title, matchWrap());

        TextView info = text(
                "כדי ש־Safe Steps יוכל להגן על המכשיר, " +
                        "יש להפעיל את שירות הנגישות של האפליקציה.",
                16,
                Color.rgb(82, 101, 125),
                false
        );
        info.setPadding(0, dp(10), 0, dp(10));
        card.addView(info, matchWrap());

        Button settingsButton = new Button(this);
        settingsButton.setText("פתיחת הגדרות נגישות");
        settingsButton.setTextSize(17);
        settingsButton.setAllCaps(false);

        settingsButton.setOnClickListener(v -> {
            try {
                Intent intent =
                        new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(
                        this,
                        "לא ניתן לפתוח את הגדרות הנגישות",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        card.addView(settingsButton, matchWrap());

        root.addView(card, cardParams());
    }

    private void buildButtons() {
        Button menuButton = new Button(this);
        menuButton.setText("⋮");
        menuButton.setTextSize(30);
        menuButton.setAllCaps(false);

        menuButton.setOnClickListener(this::showMenu);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        dp(65),
                        dp(60));

        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = dp(5);

        root.addView(menuButton, params);
    }

    private void showMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);

        menu.getMenu().add("שינוי סיסמת מנהל");
        menu.getMenu().add("אודות");

        menu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if ("שינוי סיסמת מנהל".equals(title)) {
                changePassword();
                return true;
            }

            if ("אודות".equals(title)) {
                showAbout();
                return true;
            }

            return false;
        });

        menu.show();
    }

    private void toggleProtection() {
        showCredentialDialog(
                "שינוי מצב ההגנה",
                "הזינו את סיסמת המנהל כדי להמשיך.",
                true,
                () -> {
                    boolean current =
                            getSharedPreferences(PREFS, MODE_PRIVATE)
                                    .getBoolean(ENABLED, false);

                    getSharedPreferences(PREFS, MODE_PRIVATE)
                            .edit()
                            .putBoolean(ENABLED, !current)
                            .apply();

                    updateState();
                }
        );
    }

    private void changePassword() {
        showCredentialDialog(
                "אימות מנהל",
                "הזינו את סיסמת המנהל הנוכחית.",
                true,
                this::chooseNewPassword
        );
    }

    private void chooseNewPassword() {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);

        builder.setTitle("בחירת סוג סיסמה");
        builder.setItems(
                new String[]{
                        "סיסמה מספרית",
                        "סיסמת קווים"
                },
                (dialog, which) -> {
                    if (which == 0) {
                        setNewPin();
                    } else {
                        setNewPattern();
                    }
                }
        );

        builder.show();
    }

    private void setNewPin() {
        final EditText input = new EditText(this);

        input.setHint("לפחות 4 ספרות");
        input.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        input.setPadding(
                dp(20),
                dp(10),
                dp(20),
                dp(10)
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("סיסמה מספרית חדשה")
                        .setMessage("בחרו סיסמה מספרית חדשה.")
                        .setView(input)
                        .setNegativeButton("ביטול", null)
                        .setPositiveButton("שמירה", null)
                        .create();

        dialog.setOnShowListener(d -> {
            Button positive =
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positive.setOnClickListener(v -> {
                String value =
                        input.getText().toString().trim();

                if (value.length() < 4) {
                    input.setError("יש להזין לפחות 4 ספרות");
                    return;
                }

                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .edit()
                        .putString(PASSWORD, value)
                        .putString(PASSWORD_TYPE, TYPE_PIN)
                        .apply();

                dialog.dismiss();

                Toast.makeText(
                        this,
                        "הסיסמה עודכנה בהצלחה",
                        Toast.LENGTH_SHORT
                ).show();

                updateState();
            });
        });

        dialog.show();
    }

    private void setNewPattern() {
        PatternLockView patternView =
                new PatternLockView(this);

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(
                dp(20),
                dp(10),
                dp(20),
                dp(10)
        );

        TextView message = text(
                "ציירו תבנית של לפחות 4 נקודות.",
                16,
                Color.rgb(70, 85, 105),
                false
        );

        message.setGravity(Gravity.CENTER);

        container.addView(
                message,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(50))
        );

        container.addView(
                patternView,
                new LinearLayout.LayoutParams(
                        dp(300),
                        dp(300))
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("סיסמת קווים חדשה")
                        .setView(container)
                        .setNegativeButton("ביטול", null)
                        .setPositiveButton("שמירה", null)
                        .create();

        dialog.setOnShowListener(d -> {
            Button positive =
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positive.setOnClickListener(v -> {
                String pattern =
                        patternView.getPattern();

                if (pattern == null || pattern.length() < 4) {
                    Toast.makeText(
                            this,
                            "יש לבחור לפחות 4 נקודות",
                            Toast.LENGTH_SHORT
                    ).show();
                    return;
                }

                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .edit()
                        .putString(PASSWORD, pattern)
                        .putString(PASSWORD_TYPE, TYPE_PATTERN)
                        .apply();

                dialog.dismiss();

                Toast.makeText(
                        this,
                        "סיסמת הקווים נשמרה בהצלחה",
                        Toast.LENGTH_SHORT
                ).show();

                updateState();
            });
        });

        dialog.show();
    }

    private void showCredentialDialog(
            String title,
            String message,
            boolean allowPattern,
            CredentialCallback callback) {

        String type =
                getSharedPreferences(PREFS, MODE_PRIVATE)
                        .getString(PASSWORD_TYPE, TYPE_PIN);

        if (TYPE_PATTERN.equals(type) && allowPattern) {
            showPatternCredential(title, message, callback);
        } else {
            showPinCredential(title, message, callback);
        }
    }

    private void showPinCredential(
            String title,
            String message,
            CredentialCallback callback) {

        final EditText input = new EditText(this);

        input.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_VARIATION_PASSWORD
        );

        input.setHint("סיסמה");
        input.setSingleLine(true);

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setMessage(message)
                        .setView(input)
                        .setNegativeButton("ביטול", null)
                        .setPositiveButton("אישור", null)
                        .create();

        dialog.setOnShowListener(d -> {
            Button positive =
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positive.setOnClickListener(v -> {
                String entered =
                        input.getText().toString();

                String saved =
                        getSharedPreferences(PREFS, MODE_PRIVATE)
                                .getString(PASSWORD, "1234");

                if (saved.equals(entered)) {
                    dialog.dismiss();
                    callback.success();
                } else {
                    input.setError("סיסמה שגויה");
                }
            });
        });

        dialog.show();
    }

    private void showPatternCredential(
            String title,
            String message,
            CredentialCallback callback) {

        PatternLockView patternView =
                new PatternLockView(this);

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(Gravity.CENTER);
        container.setPadding(
                dp(15),
                dp(5),
                dp(15),
                dp(5)
        );

        TextView info = text(
                message,
                16,
                Color.rgb(70, 85, 105),
                false
        );

        info.setGravity(Gravity.CENTER);

        container.addView(
                info,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(55))
        );

        container.addView(
                patternView,
                new LinearLayout.LayoutParams(
                        dp(300),
                        dp(300))
        );

        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(title)
                        .setView(container)
                        .setNegativeButton("ביטול", null)
                        .setPositiveButton("אישור", null)
                        .create();

        dialog.setOnShowListener(d -> {
            Button positive =
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positive.setOnClickListener(v -> {
                String entered =
                        patternView.getPattern();

                String saved =
                        getSharedPreferences(PREFS, MODE_PRIVATE)
                                .getString(PASSWORD, "");

                if (entered != null &&
                        entered.equals(saved)) {

                    dialog.dismiss();
                    callback.success();

                } else {
                    Toast.makeText(
                            this,
                            "תבנית שגויה",
                            Toast.LENGTH_SHORT
                    ).show();

                    patternView.clearPattern();
                }
            });
        });

        dialog.show();
    }

    private void showAbout() {
        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("אודות Safe Steps")
                        .setMessage(
                                "Safe Steps\n\n" +
                                "צעדים בטוחים בשבילך\n\n" +
                                "© כל הזכויות שמורות לישראל מויאל.\n\n" +
                                "ליצירת קשר פנו במייל:\n" +
                                "inm758595@gmail.com"
                        )
                        .setPositiveButton("סגירה", null)
                        .create();

        dialog.show();
    }

    private void updateState() {
        if (statusText == null) {
            return;
        }

        android.content.SharedPreferences prefs =
                getSharedPreferences(PREFS, MODE_PRIVATE);

        boolean enabled =
                prefs.getBoolean(ENABLED, false);

        String type =
                prefs.getString(PASSWORD_TYPE, TYPE_PIN);

        if (enabled) {
            statusText.setText("● ההגנה פעילה");
            statusText.setTextColor(
                    Color.rgb(20, 150, 95)
            );
        } else {
            statusText.setText("● ההגנה כבויה");
            statusText.setTextColor(
                    Color.rgb(210, 80, 70)
            );
        }

        if (TYPE_PATTERN.equals(type)) {
            passwordTypeText.setText(
                    "סוג סיסמת מנהל: סיסמת קווים"
            );
        } else {
            passwordTypeText.setText(
                    "סוג סיסמת מנהל: סיסמה מספרית"
            );
        }
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(
                dp(20),
                dp(20),
                dp(20),
                dp(20)
        );

        android.graphics.drawable.GradientDrawable background =
                new android.graphics.drawable.GradientDrawable();

        background.setColor(Color.WHITE);
        background.setCornerRadius(dp(22));
        background.setStroke(
                dp(1),
                Color.rgb(225, 232, 240)
        );

        card.setBackground(background);

        return card;
    }

    private TextView text(
            String value,
            float size,
            int color,
            boolean bold) {

        TextView view = new TextView(this);

        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);

        if (bold) {
            view.setTypeface(
                    Typeface.DEFAULT,
                    Typeface.BOLD
            );
        }

        return view;
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(16);

        return params;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(float value) {
        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density +
                        0.5f
        );
    }

    private interface CredentialCallback {
        void success();
    }
}
