package com.example.safesteps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    public static final String PREFS = "safe_steps";
    public static final String PASSWORD = "password";
    public static final String PASSWORD_TYPE = "password_type";
    public static final String PATTERN = "pattern";
    public static final String ENABLED = "enabled";
    public static final String SHOW_BLOCKED_SCREEN = "show_blocked_screen";
    private static final String DEFAULT_PASSWORD = "1234";
    private static final String TYPE_PIN = "pin";
    private static final String TYPE_PATTERN = "pattern";

    private TextView state;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getBooleanExtra(SHOW_BLOCKED_SCREEN, false)) showBlockedPage();
        else buildMain();
    }

    private int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable gradient(int[] colors, float radius) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, colors);
        g.setCornerRadius(dp(radius));
        return g;
    }

    private GradientDrawable solid(int color, float radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius));
        return g;
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color); t.setGravity(Gravity.CENTER);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void buildMain() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(18), dp(20), dp(20));
        root.setBackground(gradient(new int[]{Color.rgb(7, 25, 54), Color.rgb(8, 83, 113), Color.rgb(75, 34, 123)}, 0));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon = new ImageView(this); icon.setImageResource(R.drawable.ic_guard);
        header.addView(icon, new LinearLayout.LayoutParams(dp(66), dp(66)));
        Space gap = new Space(this); header.addView(gap, new LinearLayout.LayoutParams(dp(12), 1));
        LinearLayout names = new LinearLayout(this); names.setOrientation(LinearLayout.VERTICAL); names.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = text("Safe Steps", 27, Color.WHITE, true); name.setGravity(Gravity.RIGHT);
        TextView tagline = text("צעדים בטוחים בשבילך", 14, Color.rgb(206,240,246), false); tagline.setGravity(Gravity.RIGHT);
        names.addView(name); names.addView(tagline);
        header.addView(names, new LinearLayout.LayoutParams(0, -2, 1));
        TextView menu = text("⋮", 32, Color.WHITE, false);
        menu.setOnClickListener(v -> showMenu(menu));
        header.addView(menu, new LinearLayout.LayoutParams(dp(48), dp(60)));
        root.addView(header);

        Space s1 = new Space(this); root.addView(s1, new LinearLayout.LayoutParams(1, dp(25)));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL); hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(dp(22), dp(22), dp(22), dp(22));
        hero.setBackground(gradient(new int[]{Color.argb(235, 255,255,255), Color.argb(220, 235,248,255)}, 28));
        TextView title = text("מרכז ההגנה", 17, Color.rgb(45,63,80), true); hero.addView(title);
        ImageView heroIcon = new ImageView(this); heroIcon.setImageResource(R.drawable.ic_guard);
        hero.addView(heroIcon, new LinearLayout.LayoutParams(dp(100), dp(100)));
        state = text("", 25, Color.rgb(10, 137, 105), true); hero.addView(state, new LinearLayout.LayoutParams(-1, dp(50)));
        TextView sub = text("המערכת פעילה ברקע ומנטרת את המסכים המוגנים", 14, Color.rgb(75,95,108), false); hero.addView(sub, new LinearLayout.LayoutParams(-1, dp(48)));
        Button toggle = new Button(this);
        toggle.setText("הפעל / כבה הגנה"); toggle.setTextSize(17); toggle.setTextColor(Color.WHITE); toggle.setAllCaps(false);
        toggle.setBackground(gradient(new int[]{Color.rgb(20, 190, 168), Color.rgb(40, 106, 215)}, 18));
        toggle.setOnClickListener(v -> toggleProtection());
        hero.addView(toggle, new LinearLayout.LayoutParams(-1, dp(56)));
        root.addView(hero);

        Space s2 = new Space(this); root.addView(s2, new LinearLayout.LayoutParams(1, dp(16)));

        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER); row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        row.addView(infoCard("נגישות", "חסומה במצב הגנה", R.drawable.ic_guard), weightParams());
        Space g1 = new Space(this); row.addView(g1, new LinearLayout.LayoutParams(dp(10),1));
        row.addView(infoCard("התקנות", "חסומות במצב הגנה", R.drawable.ic_guard), weightParams());
        root.addView(row);

        Space s3 = new Space(this); root.addView(s3, new LinearLayout.LayoutParams(1, dp(14)));
        Button acc = new Button(this); acc.setText("פתיחת הגדרות נגישות"); acc.setTextSize(16); acc.setAllCaps(false); acc.setTextColor(Color.rgb(20,54,75));
        acc.setBackground(solid(Color.WHITE, 18)); acc.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(acc, new LinearLayout.LayoutParams(-1, dp(52)));
        updateState(); setContentView(root);
    }

    private LinearLayout.LayoutParams weightParams() { return new LinearLayout.LayoutParams(0, dp(105), 1); }

    private View infoCard(String title, String sub, int iconRes) {
        LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setGravity(Gravity.CENTER); c.setPadding(dp(8),dp(8),dp(8),dp(8)); c.setBackground(solid(Color.argb(225,255,255,255),20));
        ImageView i = new ImageView(this); i.setImageResource(iconRes); c.addView(i,new LinearLayout.LayoutParams(dp(38),dp(38)));
        TextView t=text(title,15,Color.rgb(35,60,77),true); c.addView(t);
        TextView s=text(sub,11,Color.rgb(85,105,117),false); c.addView(s);
        return c;
    }

    private void showMenu(View anchor) {
        PopupMenu p = new PopupMenu(this, anchor, Gravity.END);
        p.getMenu().add("שינוי סיסמת מנהל"); p.getMenu().add("אודות");
        p.setOnMenuItemClickListener(item -> { if (item.getTitle().toString().startsWith("שינוי")) showChangePassword(); else showAbout(); return true; });
        p.show();
    }

    private void toggleProtection() {
        showCredentialDialog("שינוי מצב ההגנה", "הזינו את סיסמת המנהל כדי להמשיך.", true, ok -> {
            getSharedPreferences(PREFS,0).edit().putBoolean(ENABLED,!isEnabled()).apply(); updateState();
        });
    }

    private interface CredentialCallback { void success(); }

    private void showCredentialDialog(String title, String message, boolean allowCancel, CredentialCallback callback) {
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(22),dp(8),dp(22),0);
        box.setBackground(gradient(new int[]{Color.rgb(9,40,79),Color.rgb(11,101,128)},22));
        TextView hint=text("שיטת הסיסמה הנוכחית: "+(isPattern()?"סיסמת קווים":"קוד מספרי"),15,Color.rgb(220,245,250),true); hint.setGravity(Gravity.RIGHT); box.addView(hint,new LinearLayout.LayoutParams(-1,dp(38)));
        final EditText pin=field("הקלידו את הקוד"); pin.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        final PatternLockView pattern=new PatternLockView(this); pattern.setVisibility(isPattern()?View.VISIBLE:View.GONE); box.addView(pin,new LinearLayout.LayoutParams(-1,dp(54))); box.addView(pattern,new LinearLayout.LayoutParams(-1,dp(280)));
        final TextView patternText=text("ציירו את התבנית שלכם",15,Color.WHITE,false); patternText.setVisibility(isPattern()?View.VISIBLE:View.GONE); box.addView(patternText);
        pattern.setListener(v -> patternText.setText(v.length()>0?"התבנית נקלטה":"ציירו את התבנית שלכם"));
        if(isPattern()) pin.setVisibility(View.GONE);
        AlertDialog d=new AlertDialog.Builder(this).setTitle(title).setMessage(message).setView(box).setNegativeButton(allowCancel?"ביטול":null,null).setPositiveButton("אישור",null).create();
        d.setOnShowListener(x -> { styleDialogButtons(d); d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> { String entered=isPattern()?pattern.getPattern():pin.getText().toString(); if(!verifyCredential(entered)){ if(isPattern()) patternText.setText("סיסמה שגויה — נסו שוב"); else pin.setError("סיסמה שגויה"); return;} callback.success(); d.dismiss(); }); });
        d.show();
    }

    private void showChangePassword() {
        final LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(20),dp(8),dp(20),0); box.setBackground(gradient(new int[]{Color.rgb(9,40,79),Color.rgb(11,101,128)},22));
        TextView title=text("בחירת שיטת סיסמה",19,Color.WHITE,true); title.setGravity(Gravity.RIGHT); box.addView(title,new LinearLayout.LayoutParams(-1,dp(42)));
        Button pinChoice=new Button(this), patternChoice=new Button(this); pinChoice.setText("קוד מספרי"); patternChoice.setText("סיסמת קווים"); styleChoice(pinChoice); styleChoice(patternChoice); box.addView(pinChoice); box.addView(patternChoice);
        AlertDialog chooser=new AlertDialog.Builder(this).setTitle("שינוי סיסמת מנהל").setMessage("בחרו את הדרך שבה תרצו להגן על אזור הניהול.").setView(box).setNegativeButton("ביטול",null).create();
        pinChoice.setOnClickListener(v->{chooser.dismiss(); showSetPassword(false);}); patternChoice.setOnClickListener(v->{chooser.dismiss(); showSetPassword(true);});
        chooser.show(); styleDialogButtons(chooser);
    }

    private void showSetPassword(boolean patternMode) {
        final LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(20),dp(8),dp(20),0); box.setBackground(gradient(new int[]{Color.rgb(9,40,79),Color.rgb(11,101,128)},22));
        TextView info=text(patternMode?"קודם אמתו את הסיסמה הנוכחית, ואז ציירו תבנית חדשה.":"קודם אמתו את הסיסמה הנוכחית, ואז הגדירו קוד חדש.",15,Color.WHITE,false); info.setGravity(Gravity.RIGHT); box.addView(info,new LinearLayout.LayoutParams(-1,dp(54)));
        EditText current=field("הסיסמה הנוכחית"); current.setInputType(isPattern()?InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD:InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_VARIATION_PASSWORD); box.addView(current,new LinearLayout.LayoutParams(-1,dp(54)));
        PatternLockView pv=new PatternLockView(this); EditText next=field(patternMode?"לא בשימוש":"קוד חדש"), confirm=field("אימות הקוד החדש");
        if(patternMode){ current.setVisibility(View.GONE); TextView cur=text("אמתו את התבנית הנוכחית במסך הבא לאחר השמירה.",14,Color.rgb(220,245,250),false); box.addView(cur); }
        if(!patternMode){box.addView(next,new LinearLayout.LayoutParams(-1,dp(54)));box.addView(confirm,new LinearLayout.LayoutParams(-1,dp(54)));}
        else {box.addView(pv,new LinearLayout.LayoutParams(-1,dp(280)));}
        AlertDialog d=new AlertDialog.Builder(this).setTitle(patternMode?"הגדרת סיסמת קווים":"הגדרת קוד מספרי").setView(box).setNegativeButton("ביטול",null).setPositiveButton("שמירה",null).create();
        d.setOnShowListener(x->{styleDialogButtons(d);d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(patternMode){String pat=pv.getPattern();if(pat.length()<4){Toast.makeText(this,"יש לבחור לפחות 4 נקודות",Toast.LENGTH_SHORT).show();return;}getSharedPreferences(PREFS,0).edit().putString(PASSWORD_TYPE,TYPE_PATTERN).putString(PATTERN,pat).apply();Toast.makeText(this,"סיסמת הקווים נשמרה",Toast.LENGTH_SHORT).show();d.dismiss();}else{if(!verifyCredential(current.getText().toString())){current.setError("סיסמה שגויה");return;}String b=next.getText().toString(),c=confirm.getText().toString();if(b.length()<4){next.setError("הקוד חייב להכיל לפחות 4 ספרות");return;}if(!b.equals(c)){confirm.setError("הקודים אינם זהים");return;}getSharedPreferences(PREFS,0).edit().putString(PASSWORD_TYPE,TYPE_PIN).putString(PASSWORD,b).apply();Toast.makeText(this,"הסיסמה שונתה בהצלחה",Toast.LENGTH_SHORT).show();d.dismiss();}});}); d.show();
    }

    private void styleChoice(Button b){b.setTextColor(Color.WHITE);b.setTextSize(16);b.setAllCaps(false);b.setBackground(gradient(new int[]{Color.rgb(26,198,183),Color.rgb(38,112,214)},18));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.setMargins(0,dp(6),0,dp(6));b.setLayoutParams(lp);}
    private boolean isPattern(){return TYPE_PATTERN.equals(getSharedPreferences(PREFS,0).getString(PASSWORD_TYPE,TYPE_PIN));}
    private boolean verifyCredential(String entered){if(isPattern())return entered.equals(getSharedPreferences(PREFS,0).getString(PATTERN,""));return entered.equals(getPassword());}

    private AlertDialog dialog(String title, String message, View view, String positive) {
        AlertDialog d=new AlertDialog.Builder(this).setTitle(title).setMessage(message).setView(view).setNegativeButton("ביטול",null).setPositiveButton(positive,null).create();
        return d;
    }

    private void styleDialogButtons(AlertDialog d) {
        if(d.getWindow()!=null) d.getWindow().setBackgroundDrawable(gradient(new int[]{Color.rgb(10,38,72),Color.rgb(9,93,119)},24));
        if(d.getButton(AlertDialog.BUTTON_POSITIVE)!=null){d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.rgb(30,200,175));d.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);}
        if(d.getButton(AlertDialog.BUTTON_NEGATIVE)!=null){d.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.rgb(190,220,235));d.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);}
    }

    private EditText field(String hint) { EditText e=new EditText(this); e.setHint(hint); e.setHintTextColor(Color.rgb(190,220,235)); e.setTextColor(Color.WHITE); e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); e.setPadding(dp(8),dp(7),dp(8),dp(7)); return e; }
    private LinearLayout dialogBox(View... views){LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setPadding(dp(20),dp(4),dp(20),0);for(View v:views)b.addView(v);return b;}

    private void showAbout() {
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setPadding(dp(26),dp(18),dp(26),dp(18));box.setBackground(gradient(new int[]{Color.rgb(9,39,78),Color.rgb(12,117,136),Color.rgb(77,40,140)},24));
        ImageView icon=new ImageView(this);icon.setImageResource(R.drawable.ic_guard);box.addView(icon,new LinearLayout.LayoutParams(dp(86),dp(86)));
        TextView title=text("Safe Steps",25,Color.WHITE,true);box.addView(title);TextView tag=text("צעדים בטוחים בשבילך",14,Color.rgb(220,245,250),false);box.addView(tag);
        TextView copy=text("© כל הזכויות שמורות לישראל מויאל.\nליצירת קשר פנו במייל: inm758595@gmail.com",14,Color.WHITE,false);box.addView(copy,new LinearLayout.LayoutParams(-1,dp(70)));
        new AlertDialog.Builder(this).setTitle("אודות").setView(box).setPositiveButton("סגירה",null).show();
    }

    private void showBlockedPage() { // fallback only; normal blocking uses the accessibility overlay
        Intent i=new Intent(Intent.ACTION_MAIN); i.addCategory(Intent.CATEGORY_HOME); i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(i); finish();
    }

    private String getPassword(){ return getSharedPreferences(PREFS,0).getString(PASSWORD,DEFAULT_PASSWORD); }
    private boolean isEnabled(){ return getSharedPreferences(PREFS,0).getBoolean(ENABLED,false); }
    private void updateState(){ if(state!=null){state.setText(isEnabled()?"ההגנה פעילה":"ההגנה כבויה"); state.setTextColor(isEnabled()?Color.rgb(0,145,105):Color.rgb(170,75,55));} }
}
