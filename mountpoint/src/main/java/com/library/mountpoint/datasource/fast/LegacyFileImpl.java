package com.library.mountpoint.datasource.fast;

import com.library.mountpoint.datasource.LegacyFile;

import java.io.File;

public class LegacyFileImpl implements LegacyFile {

  private final File file;

  private LegacyFileImpl(File file) {
    this.file = file;
  }

  static LegacyFile createRoot(String root) {
    return new LegacyFileImpl(new File(root));
  }

  @Override
  public String getName() {
    return file.getName();
  }

  @Override
  public boolean isLink() {
    try {
      if (file.getCanonicalPath().equals(file.getPath())) return false;
    } catch(Throwable t) {}
    return true;
  }

  @Override
  public boolean isFile() {
    return file.isFile();
  }

  @Override
  public long length() {
    return file.length();
  }

  @Override
  public LegacyFile[] listFiles() {
    File[] children = file.listFiles();
    LegacyFile[] res = new LegacyFile[children.length];
    for (int i = 0; i < children.length; i++) {
      res[i] = new LegacyFileImpl(children[i]);
    }
    return res;
  }

  @Override
  public String[] list() {
    return file.list();
  }

  @Override
  public LegacyFile getChild(String childName) {
    return new LegacyFileImpl(new File(file, childName));
  }
}
