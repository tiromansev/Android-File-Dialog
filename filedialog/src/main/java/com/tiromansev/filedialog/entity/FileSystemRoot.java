package com.tiromansev.filedialog.entity;

public class FileSystemRoot extends FileSystemEntry {
  final String rootPath;

  protected FileSystemRoot(String name, String rootPath) {
    super(null, name);
    this.rootPath = rootPath;
  }

  @Override
  public FileSystemEntry create() {
    return new FileSystemRoot(this.name, this.rootPath);
  }

  @Override
  public FileSystemEntry filter(CharSequence pattern, int blockSize) {
    // don't match name
    return filterChildren(pattern, blockSize);
  }
  
  public static String withSlash(String path) {
    if (path.length() > 0 && path.charAt(path.length() - 1) != '/')
      path += '/';
    return path;
  }
}
