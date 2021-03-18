package com.tiromansev.filedialog;

import com.tiromansev.prefswrapper.typedprefs.BooleanPreference;
import com.tiromansev.prefswrapper.typedprefs.StringPreference;

public class AppPrefs {

    public static StringPreference basePath() {
        return StringPreference.
                builder("preferences_file_dialog_initial_path")
                .setDefaultValue("")
                .build();
    }

    public static BooleanPreference showUseSafRationaleDialog() {
        return BooleanPreference.
                builder("preferences_show_use_saf_rationale")
                .setDefaultValue(true)
                .build();
    }

}
