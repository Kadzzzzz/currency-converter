package com.example.currency_converter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "CurrencyConverterPrefs";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_DARK_MODE = "dark_mode";

    private SwitchMaterial switchDarkMode;
    private RadioGroup radioGroupLanguage;
    private RadioButton radioFrench, radioEnglish;
    private TextView tvDataSource;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        applySettings();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
        loadPreferences();
        setupListeners();
    }

    private void initViews() {
        switchDarkMode = findViewById(R.id.switchDarkMode);
        radioGroupLanguage = findViewById(R.id.radioGroupLanguage);
        radioFrench = findViewById(R.id.radioFrench);
        radioEnglish = findViewById(R.id.radioEnglish);
        tvDataSource = findViewById(R.id.tvDataSource);

        // Bouton retour - Setup du listener ICI directement
        TextView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadPreferences() {
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(isDarkMode);

        String language = prefs.getString(KEY_LANGUAGE, "fr");
        if (language.equals("fr")) {
            radioFrench.setChecked(true);
        } else {
            radioEnglish.setChecked(true);
        }
    }

    private void setupListeners() {
        // Mode sombre
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(KEY_DARK_MODE, isChecked).apply();
            applySettings();
            recreate();
        });

        // Changement de langue
        radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String newLanguage = checkedId == R.id.radioFrench ? "fr" : "en";
            String currentLanguage = prefs.getString(KEY_LANGUAGE, "fr");

            if (!newLanguage.equals(currentLanguage)) {
                prefs.edit().putString(KEY_LANGUAGE, newLanguage).apply();

                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Lien vers la source des données
        tvDataSource.setOnClickListener(v -> {
            try {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://open.er-api.com/"));
                startActivity(browserIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void applySettings() {
        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        String language = prefs.getString(KEY_LANGUAGE, "fr");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    public static void applySettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        boolean isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        String language = prefs.getString(KEY_LANGUAGE, "fr");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}