package org.andstatus.todoagenda.prefs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.fragment.app.DialogFragment;

import com.larswerkman.holocolorpicker.ColorPicker;

import org.andstatus.todoagenda.R;

@SuppressLint("ValidFragment")
public class BackgroundTransparencyDialog extends DialogFragment {
    private ColorPicker picker;
    @ColorInt private int initialColor;
    private Consumer<Integer> setter;

    public BackgroundTransparencyDialog(@ColorInt int initialColor, Consumer<Integer> setter) {
        this.initialColor = initialColor;
        this.setter = setter;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View layout = inflater.inflate(R.layout.background_color, null);
        picker = layout.findViewById(R.id.background_color_picker);
        picker.addSVBar(layout.findViewById(R.id.background_color_svbar));
        picker.addOpacityBar(layout.findViewById(R.id.background_color_opacitybar));
        picker.setColor(initialColor);
        picker.setOldCenterColor(initialColor);
        return createDialog(layout);
    }

    private Dialog createDialog(View layout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getTag().equals(InstanceSettings.PREF_PAST_EVENTS_BACKGROUND_COLOR)
                ? R.string.appearance_past_events_background_color_title
                : R.string.appearance_background_color_title);
        builder.setView(layout);
        builder.setPositiveButton(android.R.string.ok,
                (dialog, which) -> setter.accept(picker.getColor()));
        return builder.create();
    }
}
