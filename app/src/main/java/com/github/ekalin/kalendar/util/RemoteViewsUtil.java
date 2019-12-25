package com.github.ekalin.kalendar.util;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.IdRes;

import com.github.ekalin.kalendar.prefs.InstanceSettings;

public class RemoteViewsUtil {
    private static final String METHOD_SET_TEXT_SIZE = "setTextSize";
    private static final String METHOD_SET_BACKGROUND_COLOR = "setBackgroundColor";
    private static final String METHOD_SET_SINGLE_LINE = "setSingleLine";
    private static final String METHOD_SET_WIDTH = "setWidth";

    private RemoteViewsUtil() {
        // prohibit instantiation
    }

    public static void setPadding(InstanceSettings settings, RemoteViews rv, @IdRes int viewId,
                                  @DimenRes int leftDimenId, @DimenRes int topDimenId, @DimenRes int rightDimenId,
                                  @DimenRes int bottomDimenId) {
        int leftPadding = getScaledValueInPixels(settings, leftDimenId);
        int topPadding = getScaledValueInPixels(settings, topDimenId);
        int rightPadding = getScaledValueInPixels(settings, rightDimenId);
        int bottomPadding = getScaledValueInPixels(settings, bottomDimenId);
        rv.setViewPadding(viewId, leftPadding, topPadding, rightPadding, bottomPadding);
    }

    public static void setViewWidth(InstanceSettings settings, RemoteViews rv, int viewId, int dimenId) {
        rv.setInt(viewId, METHOD_SET_WIDTH, getScaledValueInPixels(settings, dimenId));
    }

    public static void setTextSize(InstanceSettings settings, RemoteViews rv, int viewId, int dimenId) {
        rv.setFloat(viewId, METHOD_SET_TEXT_SIZE, getScaledValueInScaledPixels(settings, dimenId));
    }

    public static void setBackgroundColor(RemoteViews rv, int viewId, int color) {
        rv.setInt(viewId, METHOD_SET_BACKGROUND_COLOR, color);
    }

    private static int getScaledValueInPixels(InstanceSettings settings, int dimenId) {
        float resValue = getDimension(settings.getContext(), dimenId);
        return Math.round(resValue * settings.getTextSizeScale().scaleValue);
    }

    private static float getScaledValueInScaledPixels(InstanceSettings settings, int dimenId) {
        float resValue = getDimension(settings.getContext(), dimenId);
        float density = settings.getContext().getResources().getDisplayMetrics().density;
        return resValue * settings.getTextSizeScale().scaleValue / density;
    }

    private static float getDimension(Context context, int dimensionResourceId) {
        try {
            return context.getResources().getDimension(dimensionResourceId);
        } catch (NotFoundException e) {
            Log.w(RemoteViewsUtil.class.getSimpleName(),
                    "getDimension failed for dimension resource Id:" + dimensionResourceId);
            return 0f;
        }
    }

    public static void setMultiline(RemoteViews rv, int viewId, boolean multiLine) {
        rv.setBoolean(viewId, METHOD_SET_SINGLE_LINE, !multiLine);
    }

    public static void setDrawableColor(RemoteViews rv, int viewId, @ColorInt int color) {
        rv.setInt(viewId, "setColorFilter", color);
    }
}
