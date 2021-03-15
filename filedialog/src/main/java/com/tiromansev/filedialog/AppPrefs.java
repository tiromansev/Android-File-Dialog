package com.tiromansev.filedialog;

import com.tiromansev.prefswrapper.typedprefs.StringPreference;

public class AppPrefs {

    public static StringPreference initialPath() {
        return StringPreference.
                builder("preferences_file_dialog_initial_path")
                .setDefaultValue("")
                .build();
    }

}
