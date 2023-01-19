package com.tiromansev.filedialog;

/**
 * интерфейс диалога выбора файлов и папок
 * при отображении содержимого файловой системы необходимо папки всегда отображать первыми
 * а также применять сортировку по имени по алфавиту (a-z) без учета регистра в названиях папок/файлов
 */
public interface IFileDialog {

    public static final int FILE_OPEN = 0;
    public static final int FILE_SAVE = 1;
    public static final int FOLDER_CHOOSE = 2;

    void setSelectType(int selectType);

    void setFilterFileExt(String[] filterFileExt);

    void setAddModifiedDate(boolean add);

    void setFileDialogListener(FileDialogListener listener);

    void show();

}
