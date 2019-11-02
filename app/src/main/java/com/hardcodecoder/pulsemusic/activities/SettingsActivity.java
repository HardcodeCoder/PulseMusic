package com.hardcodecoder.pulsemusic.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.hardcodecoder.pulsemusic.themes.ThemeManager;
import com.hardcodecoder.pulsemusic.themes.ThemeStore;
import com.hardcodecoder.pulsemusic.utils.AppSettings;

public class SettingsActivity extends PMBActivity {

    /*private final short ID_LIGHT = 10;
    private final short ID_DARK = 20;*/
    private boolean autoModeEnable;
    private boolean darkModeEnable;
    private boolean optionChanged = false;
    private boolean isAccentChanged = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setUpDefaultStates();
        findViewById(R.id.settings_close_btn).setOnClickListener(v -> finish());
    }

    private void setUpDefaultStates() {
        TextView darkModeTextView = findViewById(R.id.setting_switch_1_title);
        Switch darkThemeToggle = findViewById(R.id.settings_toggle_dark_theme);
        TextView autoThemeTextView = findViewById(R.id.settings_switch_2_title);
        Switch autoThemeToggle = findViewById(R.id.settings_toggle_auto_theme);

        darkModeEnable = AppSettings.isDarkModeEnabled(this);
        darkModeTextView.setText(darkModeEnable ? R.string.dark_on : R.string.light_on);
        darkThemeToggle.setChecked(darkModeEnable);

        autoModeEnable = ThemeManager.isAutoThemeEnabled();
        autoThemeTextView.setText(autoModeEnable ? R.string.auto_theme_enabled : R.string.auto_theme_disabled);
        autoThemeToggle.setChecked(autoModeEnable);
        darkThemeToggle.setEnabled(!autoModeEnable);
        findViewById(R.id.setting_switch_1_title).setEnabled(!autoModeEnable);


        darkThemeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.enableDarkMode(this, isChecked);

            if (!ThemeManager.isDarkModeEnabled() && isChecked) {
                // User wants Dark theme but Light theme is previously
                // applied so restart the activity to apply Dark theme
                restart();
            } else if (ThemeManager.isDarkModeEnabled() && !isChecked) {
                // User wants Light theme but dark theme is previously
                // applied so restart the activity to apply Light theme
                restart();
            }
        });

        autoThemeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.enableAutoTheme(this, isChecked);
            darkThemeToggle.setEnabled(!isChecked);
            findViewById(R.id.setting_switch_1_title).setEnabled(!isChecked);

            if (isChecked && needToToggleTheme()) {
                // User want auto theme based on time of day
                // If its night/day and current theme is light/dark respectively
                // them restart to apply new theme
                restart();
            } else {
                // User does not want auto theme based on time of day
                // Revert to theme selected via darkThemeToggle
                if (darkThemeToggle.isChecked() && !ThemeManager.isDarkModeEnabled()) {
                    // User previously select dark theme so when auto theme is
                    // disabled apply dark theme if not already applied
                    restart();
                } else if (!darkThemeToggle.isChecked() && ThemeManager.isDarkModeEnabled()) {
                    // User previously select light theme so when auto theme is
                    // disabled apply light theme if not already applied
                    restart();
                }
            }
        });

        findViewById(R.id.accents_options).setOnClickListener(this::openAccentPicker);
        //findViewById(R.id.light_theme_options).setOnClickListener(v -> openThemeSelector(v, ID_LIGHT));
        findViewById(R.id.dark_theme_options).setOnClickListener(this::openDarkThemeSelector);
    }

    private void openAccentPicker (View view) {
        View windowView = View.inflate(this, R.layout.accent_color_picker , null);
        PopupWindow window = new PopupWindow(windowView, RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT, true);

        RadioGroup radioGroup = windowView.findViewById(R.id.radio_group);

        int currentAccent = AppSettings.getSelectedAccentColor(this);
        switch (currentAccent) {
            case ThemeStore.CINNAMON:
                ((RadioButton) radioGroup.findViewById(R.id.rd_btn_1)).setChecked(true);
                break;
            case ThemeStore.GREEN:
                ((RadioButton) radioGroup.findViewById(R.id.rd_btn_2)).setChecked(true);
                break;
            case ThemeStore.OCEAN:
                ((RadioButton) radioGroup.findViewById(R.id.rd_btn_3)).setChecked(true);
                break;
            case ThemeStore.ORCHID:
                ((RadioButton) radioGroup.findViewById(R.id.rd_btn_4)).setChecked(true);
                break;
            case ThemeStore.PURPLE:
                ((RadioButton) radioGroup.findViewById(R.id.rd_btn_5)).setChecked(true);
                break;
        }

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Log.e("Settings", "Checked changed");
            isAccentChanged = true;
        });

        windowView.findViewById(R.id.btn_set).setOnClickListener(v -> {
            if(isAccentChanged) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.rd_btn_1:
                        ThemeManager.setSelectedAccentColor(this, ThemeStore.CINNAMON);
                        break;
                    case R.id.rd_btn_2:
                        ThemeManager.setSelectedAccentColor(this, ThemeStore.GREEN);
                        break;
                    case R.id.rd_btn_3:
                        ThemeManager.setSelectedAccentColor(this, ThemeStore.OCEAN);
                        break;
                    case R.id.rd_btn_4:
                        ThemeManager.setSelectedAccentColor(this, ThemeStore.ORCHID);
                        break;
                    case R.id.rd_btn_5:
                    default:
                        ThemeManager.setSelectedAccentColor(this, ThemeStore.PURPLE);
                }
                isAccentChanged = false;
                restart();
            }
        });

        window.setBackgroundDrawable(getDrawable(R.drawable.popup_menu_background));
        window.showAtLocation(view, Gravity.CENTER, 0, 0);
        dimBackgroundOnPopupWindow(window);
    }

    private void openDarkThemeSelector(View view){
        View windowView = View.inflate(this, R.layout.dark_theme_picker, null);
        PopupWindow window = new PopupWindow(windowView, RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT, true);

        RadioGroup radioGroup = windowView.findViewById(R.id.radio_group);

        int currentTheme = AppSettings.getSelectedDarkTheme(this);
        //if(id == ID_LIGHT) currentTheme = AppSettings.getSelectedLightTheme(this);
        //else currentTheme = AppSettings.getSelectedDarkTheme(this);

        switch (currentTheme) {
            //case ThemeStore.LIGHT_THEME_1:
            case ThemeStore.DARK_THEME_1:
                ((RadioButton) radioGroup.findViewById(R.id.rd_btn_1)).setChecked(true);
                break;
            //case ThemeStore.LIGHT_THEME_2:
            case ThemeStore.DARK_THEME_2:
                ((RadioButton) radioGroup.findViewById(R.id.rd_btn_2)).setChecked(true);
                break;
            //case ThemeStore.LIGHT_THEME_3:
            case ThemeStore.DARK_THEME_3:
                ((RadioButton) radioGroup.findViewById(R.id.rd_btn_3)).setChecked(true);
                break;
        }

        /*RadioButton tempBtn = radioGroup.findViewById(R.id.rd_btn_1);
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
            tempBtn.setText(R.string.pitch_dark);
        }*/

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> optionChanged = true);

        windowView.findViewById(R.id.btn_set).setOnClickListener(v1 -> {
            if (window.isShowing())
                window.dismiss();
            if (optionChanged) {
                /*switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.rd_btn_1:
                        if (id == ID_LIGHT) ThemeManager.setSelectedLightTheme(this, ThemeStore.LIGHT_THEME_1);
                        else ThemeManager.setSelectedDarkTheme(this, ThemeStore.DARK_THEME_1);
                        break;
                    case R.id.rd_btn_2:
                        if (id == ID_LIGHT) ThemeManager.setSelectedLightTheme(this, ThemeStore.LIGHT_THEME_2);
                        else ThemeManager.setSelectedDarkTheme(this, ThemeStore.DARK_THEME_2);
                        break;
                    case R.id.rd_btn_3:
                        if (id == ID_LIGHT) ThemeManager.setSelectedLightTheme(this, ThemeStore.LIGHT_THEME_3);
                        else ThemeManager.setSelectedDarkTheme(this, ThemeStore.DARK_THEME_3);
                        break;
                }*/

                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.rd_btn_1:
                        ThemeManager.setSelectedDarkTheme(this, ThemeStore.DARK_THEME_1);
                        break;
                    case R.id.rd_btn_2:
                        ThemeManager.setSelectedDarkTheme(this, ThemeStore.DARK_THEME_2);
                        break;
                    case R.id.rd_btn_3:
                        ThemeManager.setSelectedDarkTheme(this, ThemeStore.DARK_THEME_3);
                        break;
                }

                if (autoModeEnable) {
                    /*if (id == ID_LIGHT && !isNight()) restart();
                    else */if (/*id == ID_DARK && */isNight()) restart();
                } else {
                    /*if (id == ID_LIGHT && !darkModeEnable) restart();
                    else*/ if (/*id == ID_DARK && */darkModeEnable) restart();
                }
                optionChanged = false;
            }
        });

        window.setBackgroundDrawable(getDrawable(R.drawable.popup_menu_background));
        window.showAtLocation(view, Gravity.CENTER, 0, 0);
        dimBackgroundOnPopupWindow(window);
    }

    private void dimBackgroundOnPopupWindow(PopupWindow window) {
        View container;
        if (null == window.getBackground()) container = (View) window.getContentView().getParent();
        else container = (View) window.getContentView().getParent().getParent();
        WindowManager wm = (WindowManager) window.getContentView().getContext().getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.70f;
        if(null != wm)
            wm.updateViewLayout(container, p);
    }

    private boolean needToToggleTheme() {
        return ((isNight() && !ThemeManager.isDarkModeEnabled()) || (!isNight() && ThemeManager.isDarkModeEnabled()));
    }

    private boolean isNight() {
        Day d = HomeWalliProvider.getTimeOfDay();
        return (d == Day.EVENING || d == Day.NIGHT);
    }

    private void restart() {
        ThemeManager.init(getApplicationContext());
        startActivity(new Intent(this, SettingsActivity.class));
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        finish();
    }
}
