package com.hardcodecoder.pulsemusic.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hardcodecoder.pulsemusic.R;
import com.hardcodecoder.pulsemusic.helper.HomeWalliProvider;
import com.hardcodecoder.pulsemusic.helper.HomeWalliProvider.Day;
import com.hardcodecoder.pulsemusic.utils.UserInfo;

public class SettingsActivity extends Activity {

    private final short ID_PORTRAIT = 1;
    private final short ID_LANDSCAPE = 2;
    private final short ID_LIGHT = 10;
    private final short ID_DARK = 20;
    private boolean autoModeEnable;
    private boolean darkModeEnable;
    private boolean optionChanged = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(UserInfo.getThemeToApply());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setUpDefaultStates();
        findViewById(R.id.settings_close_btn).setOnClickListener(v -> finish());
    }

    private void setUpDefaultStates() {
        TextView tv1 = findViewById(R.id.setting_switch_1_title);
        Switch switch1 = findViewById(R.id.settings_switch_1);
        TextView tv2 = findViewById(R.id.settings_switch_2_title);
        Switch autoTheme = findViewById(R.id.settings_switch_2);

        darkModeEnable = UserInfo.isDarkModeSettingEnabled();
        tv1.setText(darkModeEnable ? R.string.dark_on : R.string.light_on);
        switch1.setChecked(darkModeEnable);

        autoModeEnable = UserInfo.isAutoThemeEnabled();
        tv2.setText(autoModeEnable ? R.string.auto_theme_enabled : R.string.auto_theme_disabled);
        autoTheme.setChecked(autoModeEnable);
        switch1.setEnabled(!autoModeEnable);
        findViewById(R.id.setting_switch_1_title).setEnabled(!autoModeEnable);


        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            switch1.setChecked(isChecked);
            UserInfo.enableDarkMode(isChecked);

            if (!UserInfo.isDarkModeEnabled() && isChecked/*needToToggleTheme()*/) {
                // User wants Dark theme but Light theme is previously
                // applied so restart the activity to apply Dark theme
                restart();
            } else if (UserInfo.isDarkModeEnabled() && !isChecked /*&& !autoTheme.isChecked()*/) {
                // User wants Light theme but dark theme is previously
                // applied so restart the activity to apply Light theme
                restart();
            }
        });

        autoTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            autoTheme.setChecked(isChecked);
            UserInfo.enableAutoTheme(isChecked);
            switch1.setEnabled(!isChecked);
            findViewById(R.id.setting_switch_1_title).setEnabled(!isChecked);

            if (isChecked && needToToggleTheme()) {
                //User want auto theme based on time of day
                //If its night/day and current theme is light/dark respectively
                // them restart to apply new theme
                restart();
            } else {
                //User does not want auto theme based on time of day
                //Revert to theme selected via switch1
                if (switch1.isChecked() && !UserInfo.isDarkModeEnabled()) {
                    //User previously select dark theme so when auto theme is
                    // disabled apply dark theme if not already applied
                    restart();
                } else if (!switch1.isChecked() && UserInfo.isDarkModeEnabled()) {
                    //User previously select light theme so when auto theme is
                    // disabled apply light theme if not already applied
                    restart();
                }
            }
        });


        findViewById(R.id.columns_portrait).setOnClickListener(v -> openRowSelector(v, ID_PORTRAIT));
        findViewById(R.id.columns_landscape).setOnClickListener(v -> openRowSelector(v, ID_LANDSCAPE));

        findViewById(R.id.light_theme_options).setOnClickListener(v -> openThemeSelector(v, ID_LIGHT));
        findViewById(R.id.dark_theme_options).setOnClickListener(v -> openThemeSelector(v, ID_DARK));
    }

    private void openRowSelector(View v, short id) {
        View windowView = View.inflate(this, R.layout.settings_drop_down_menu, null);
        PopupWindow window = new PopupWindow(windowView, RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT, true);
        RadioGroup radioGroup = windowView.findViewById(R.id.radio_group);
        ((TextView) windowView.findViewById(R.id.radio_group_title)).setText(R.string.select_columns);

        int currentCount;
        if (id == ID_PORTRAIT) {
            currentCount = UserInfo.getPortraitGridSpanCount(this);
            switch (currentCount) {
                case 2:
                    ((RadioButton) radioGroup.findViewById(R.id.rd_btn_1)).setChecked(true);
                    break;
                case 3:
                    ((RadioButton) radioGroup.findViewById(R.id.rd_btn_2)).setChecked(true);
                    break;
                case 4:
                    ((RadioButton) radioGroup.findViewById(R.id.rd_btn_3)).setChecked(true);
                    break;
            }
            RadioButton tempBtn = radioGroup.findViewById(R.id.rd_btn_1);
            tempBtn.setText(R.string.two);

            tempBtn = radioGroup.findViewById(R.id.rd_btn_2);
            tempBtn.setText(R.string.three);

            tempBtn = radioGroup.findViewById(R.id.rd_btn_3);
            tempBtn.setText(R.string.four);
        } else {
            currentCount = UserInfo.getLandscapeGridSpanCount(this);
            switch (currentCount) {
                case 4:
                    ((RadioButton) radioGroup.findViewById(R.id.rd_btn_1)).setChecked(true);
                    break;
                case 5:
                    ((RadioButton) radioGroup.findViewById(R.id.rd_btn_2)).setChecked(true);
                    break;
                case 6:
                    ((RadioButton) radioGroup.findViewById(R.id.rd_btn_3)).setChecked(true);
                    break;
            }
            RadioButton tempBtn = radioGroup.findViewById(R.id.rd_btn_1);
            tempBtn.setText(R.string.four);

            tempBtn = radioGroup.findViewById(R.id.rd_btn_2);
            tempBtn.setText(R.string.five);

            tempBtn = radioGroup.findViewById(R.id.rd_btn_3);
            tempBtn.setText(R.string.six);
        }

        windowView.findViewById(R.id.radio_group_btn_set).setOnClickListener(v1 -> {
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.rd_btn_1:
                    if (id == ID_PORTRAIT) UserInfo.savePortraitGridSpanCount(2);
                    else UserInfo.saveLandscapeGridSpanCount(4);
                    break;
                case R.id.rd_btn_2:
                    if (id == ID_PORTRAIT) UserInfo.savePortraitGridSpanCount(3);
                    else UserInfo.saveLandscapeGridSpanCount(5);
                    break;
                case R.id.rd_btn_3:
                    if (id == ID_PORTRAIT) UserInfo.savePortraitGridSpanCount(4);
                    else UserInfo.saveLandscapeGridSpanCount(6);
                    break;
            }
            if (window.isShowing())
                window.dismiss();

        });
        window.setBackgroundDrawable(getDrawable(R.drawable.popup_menu_background));
        window.showAtLocation(v, Gravity.CENTER, 0, 0);
        dimBackgroundOnPopupWindow(window);
    }

    private void openThemeSelector(View v, short id) {
        View windowView = View.inflate(this, R.layout.settings_drop_down_menu, null);
        PopupWindow window = new PopupWindow(windowView, RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT, true);

        RadioGroup radioGroup = windowView.findViewById(R.id.radio_group);

        int currentTheme;
        if (id == ID_LIGHT) currentTheme = UserInfo.getSelectedLightTheme(this);
        else currentTheme = UserInfo.getSelectedDarkTheme(this);

        switch (currentTheme) {
            case UserInfo.LIGHT_THEME_1:
            case UserInfo.DARK_THEME_1:
                ((RadioButton) radioGroup.findViewById(R.id.rd_btn_1)).setChecked(true);
                break;
            case UserInfo.LIGHT_THEME_2:
            case UserInfo.DARK_THEME_2:
                ((RadioButton) radioGroup.findViewById(R.id.rd_btn_2)).setChecked(true);
                break;
            case UserInfo.LIGHT_THEME_3:
            case UserInfo.DARK_THEME_3:
                ((RadioButton) radioGroup.findViewById(R.id.rd_btn_3)).setChecked(true);
                break;
        }

        RadioButton tempBtn = radioGroup.findViewById(R.id.rd_btn_1);
        if (id == ID_LIGHT) {
            ((TextView) windowView.findViewById(R.id.radio_group_title)).setText(R.string.select_light_theme);
            tempBtn.setText(R.string.def_theme_light);

            tempBtn = radioGroup.findViewById(R.id.rd_btn_2);
            tempBtn.setText(R.string.black_and_white);

            tempBtn = radioGroup.findViewById(R.id.rd_btn_3);
            tempBtn.setText(R.string.sweet_morning_light);
        } else {
            ((TextView) windowView.findViewById(R.id.radio_group_title)).setText(R.string.select_dark_theme);
            tempBtn.setText(R.string.def_theme_dark);

            tempBtn = radioGroup.findViewById(R.id.rd_btn_2);
            tempBtn.setText(R.string.app_dark_theme);

            tempBtn = radioGroup.findViewById(R.id.rd_btn_3);
            tempBtn.setText(R.string.charcoal_dark);
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> optionChanged = true);

        windowView.findViewById(R.id.radio_group_btn_set).setOnClickListener(v1 -> {
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.rd_btn_1:
                    if (id == ID_LIGHT) UserInfo.saveSelectedLightTheme(UserInfo.LIGHT_THEME_1);
                    else UserInfo.saveSelectedDarkTheme(UserInfo.DARK_THEME_1);
                    break;
                case R.id.rd_btn_2:
                    if (id == ID_LIGHT) UserInfo.saveSelectedLightTheme(UserInfo.LIGHT_THEME_2);
                    else UserInfo.saveSelectedDarkTheme(UserInfo.DARK_THEME_2);
                    break;
                case R.id.rd_btn_3:
                    if (id == ID_LIGHT) UserInfo.saveSelectedLightTheme(UserInfo.LIGHT_THEME_3);
                    else UserInfo.saveSelectedDarkTheme(UserInfo.DARK_THEME_3);
                    break;
            }
            if (window.isShowing())
                window.dismiss();

            if (autoModeEnable && optionChanged) {
                if (id == ID_LIGHT && !isNight()) restart();
                else if (id == ID_DARK && isNight()) restart();
            } else if (optionChanged) {
                if (id == ID_LIGHT && !darkModeEnable) restart();
                else if (id == ID_DARK && darkModeEnable) restart();
            }
            optionChanged = false;
        });

        window.setBackgroundDrawable(getDrawable(R.drawable.popup_menu_background));
        window.showAtLocation(v, Gravity.CENTER, 0, 0);
        dimBackgroundOnPopupWindow(window);
    }

    private void dimBackgroundOnPopupWindow(PopupWindow window) {
        View container;
        if (null == window.getBackground()) container = (View) window.getContentView().getParent();
        else container = (View) window.getContentView().getParent().getParent();
        WindowManager wm = (WindowManager) window.getContentView().getContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.4f;
        wm.updateViewLayout(container, p);
    }

    private boolean needToToggleTheme() {
        return (isNight() && !UserInfo.isDarkModeEnabled() || !isNight() && UserInfo.isDarkModeEnabled());
    }

    private boolean isNight() {
        Day d = HomeWalliProvider.getTimeOfDay();
        return (d == Day.EVENING || d == Day.NIGHT);
    }

    private void restart() {
        UserInfo.initSharedPrefs(getApplicationContext());
        this.recreate();
    }
}
