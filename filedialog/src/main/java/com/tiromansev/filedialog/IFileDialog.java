package com.tiromansev.filedialog;

/**
 * интерфейс диалога выбора файлов и папок
 * при отображении содержимого файловой системы необходимо папки всегда отображать первыми
 * а также применять сортировку по имени по алфавиту (a-z) без учета регистра в названиях папок/файлов
 */
public interface IFileDialog {

    public static final int FILE_OPEN = 0;
    public static final int FOLDER_CHOOSE = 1;

    /**
     * устанавливает тип диалога
     * @param selectType может иметь два значения
     *                   FILE_OPEN - открываем диалог выбора файла
     *                   FOLDER_CHOOSE - открываем диалог выбора папки
     */
    void setSelectType(int selectType);

    /**
     * устанавливает фильтр по mime типу для файлов в окне диалога
     * @param filterFileExt массив mime типов
     */
    void setFilterFileExt(String[] filterFileExt);

    /**
     * определяет нужно ли в окне диалога показывать дату модификации файлов
     * @param add
     */
    void setAddModifiedDate(boolean add);

    /**
     * устанавливает слушатель для выбора файла/папки, который возвращает строковое значение абсолютного пути к выбранной папки/файлу
     * @param listener
     */
    void setFileDialogListener(FileDialogListener listener);

    /**
     * показывает диалог
     * @param directory строковое значение абсолютного пути к папке которую нужно открывать при показе окна диалога
     */
    void show(String directory);

}
