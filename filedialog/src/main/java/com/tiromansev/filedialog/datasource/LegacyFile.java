package com.tiromansev.filedialog.datasource;

public interface LegacyFile {
  String getName();

  boolean isLink();
  boolean isFile();
  long length();

  LegacyFile[] listFiles();
  String[] list();

  LegacyFile getChild(String string);
}
