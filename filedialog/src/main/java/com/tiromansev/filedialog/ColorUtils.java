package com.tiromansev.filedialog;

import android.content.Context;
import android.util.TypedValue;

public class ColorUtils {

    public static int getAttrColor(int colorId, Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().applyStyle(R.style.BreadCrumbsTheme, true);
        context.getTheme().resolveAttribute(colorId, typedValue, true);

        return context.getResources().getColor(typedValue.resourceId);
    }
}
