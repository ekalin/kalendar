package com.github.ekalin.kalendar;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.prefs.KalendarPreferenceFragment;
import com.github.ekalin.kalendar.prefs.PreferencesFragment;
import com.github.ekalin.kalendar.util.PermissionsUtil;

public class WidgetConfigurationActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private static final String TITLE_TAG = "com.github.ekalin.kalendar.PREFS_TITLE";

    private int widgetId = 0;
    private String prefsName;

    @NonNull
    public static Intent intentToStartMe(Context context, int widgetId) {
        Intent intent = new Intent(context, WidgetConfigurationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!openThisActivity(getIntent())) {
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.widget_configuration_actitivy);

        prefsName = InstanceSettings.nameForWidget(widgetId);

        if (savedInstanceState == null) {
            PreferencesFragment fragment = new PreferencesFragment();
            Bundle args = new Bundle();
            setFragmentArguments(args);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(R.id.preferences, fragment).commit();
            setTitleToWidgetName();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                setTitleToWidgetName();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setFragmentArguments(Bundle args) {
        args.putString(KalendarPreferenceFragment.PREFS_NAME_KEY, prefsName);
        args.putInt(KalendarPreferenceFragment.WIDGET_ID_KEY, widgetId);
    }

    private boolean openThisActivity(Intent newIntent) {
        int newWidgetId = newIntent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        Intent restartIntent = null;
        if (newWidgetId == 0 || !PermissionsUtil.arePermissionsGranted(this)) {
            restartIntent = MainActivity.intentToStartMe(this);
        } else if (widgetId != 0 && widgetId != newWidgetId) {
            restartIntent = MainActivity.intentToConfigure(this, newWidgetId);
        } else if (widgetId == 0) {
            widgetId = newWidgetId;
        }
        if (restartIntent != null) {
            widgetId = 0;
            startActivity(restartIntent);
            finish();
            return false;
        }

        return true;
    }

    private void setTitleToWidgetName() {
        InstanceSettings instanceSettings = AllSettings.instanceFromId(this, widgetId);
        setTitle(instanceSettings.getWidgetInstanceName());
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        Bundle args = pref.getExtras();
        setFragmentArguments(args);
        Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.preferences, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        EnvironmentChangedReceiver.updateWidget(this, widgetId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        restartIfNeeded();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    private void restartIfNeeded() {
        if (!PermissionsUtil.arePermissionsGranted(this)) {
            widgetId = 0;
            startActivity(MainActivity.intentToStartMe(this));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        } else {
            finish();
            return true;
        }
    }
}
