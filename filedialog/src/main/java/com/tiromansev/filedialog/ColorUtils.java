package com.tiromansev.filedialog;

import android.content.Context;
import android.util.TypedValue;

public class ColorUtils {

    public static int getAttrColor(int colorId, Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(colorId, typedValue, true);

        return context.getTheme().getResources().getColor(typedValue.resourceId);
    }
}
