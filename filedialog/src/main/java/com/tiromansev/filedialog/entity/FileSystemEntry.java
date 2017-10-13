package com.tiromansev.filedialog.entity;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class FileSystemEntry {
  private static final Paint bg = new Paint();
  private static final Paint bg_emptySpace = new Paint();
  private static final Paint cursor_fg = new Paint();
  private static final Paint fg_rect = new Paint();
//  private static final Paint fg_rect = new Paint();
  public static final Paint fg2 = new Paint();
  private static final Paint fill_bg = new Paint();
  private static final Paint textPaintFolder = new Paint();
  private static final Paint textPaintFile = new Paint();

  /**
   * Font size. Also accessed from FileSystemView.
   */
  public static float fontSize;

  static {
    bg.setColor(Color.parseColor("#060118"));
    bg_emptySpace.setColor(Color.parseColor("#063A43"));
    bg.setStyle(Paint.Style.FILL);
//    bg.setAlpha(255);
    fg_rect.setColor(Color.WHITE);
    fg_rect.setStyle(Paint.Style.STROKE);
    fg_rect.setFlags(fg_rect.getFlags() | Paint.ANTI_ALIAS_FLAG);
//    fg_rect.setColor(Color.WHITE);
//    fg_rect.setStyle(Paint.Style.STROKE);
    fg2.setColor(Color.parseColor("#18C5E7"));
    fg2.setStyle(Paint.Style.STROKE);
    fg2.setFlags(fg2.getFlags() | Paint.ANTI_ALIAS_FLAG);
    fill_bg.setColor(Color.WHITE);
    fill_bg.setStyle(Paint.Style.FILL);
    cursor_fg.setColor(Color.YELLOW);
    cursor_fg.setStyle(Paint.Style.STROKE);

    textPaintFolder.setColor(Color.WHITE);
    textPaintFolder.setStyle(Paint.Style.FILL_AND_STROKE);
    textPaintFolder.setFlags(textPaintFolder.getFlags() | Paint.ANTI_ALIAS_FLAG);

    textPaintFile.setColor(Color.parseColor("#18C5E7"));
    textPaintFile.setStyle(Paint.Style.FILL_AND_STROKE);
    textPaintFile.setFlags(textPaintFile.getFlags() | Paint.ANTI_ALIAS_FLAG);
  }

  // FIXME: remove outdate info:
  // reminder can be 0..blockSize (inclusive)
  // size in bytes = (size in blocks * blockSize) + reminder - blockSize;
  // FIXME: make private and update code which uses it
  public long encodedSize;
  public FileSystemEntry parent;
  public FileSystemEntry[] children;
  public String name;

  private static final int MULTIPLIER_SHIFT=18;

  private static final int MULTIPLIER_KBYTES = 1 << MULTIPLIER_SHIFT;
  private static final int MULTIPLIER_MBYTES = 2 << MULTIPLIER_SHIFT;
  private static final int MULTIPLIER_MBYTES10 = 3 << MULTIPLIER_SHIFT;
  private static final int MULTIPLIER_MBYTES100 = 4 << MULTIPLIER_SHIFT;
  private static final int MULTIPLIER_GBYTES = 5 << MULTIPLIER_SHIFT;
  private static final int MULTIPLIER_GBYTES10 = 6 << MULTIPLIER_SHIFT;
  private static final int MULTIPLIER_GBYTES100 = 7 << MULTIPLIER_SHIFT;

//  static int blockSize;
  // will take for a while to make this break
  // 16Mb block size on mobile device... probably in year 2020.
  // probably 32 bits for maximum number of block will break before ~2016
  public static final int blockOffset = 24;

  public long getSizeInBlocks() {
    return encodedSize >> blockOffset;
  }

  private long makeBytesPart(long size) {
    if (size < 1024) return size;
    if (size < 1024 * 1024) return MULTIPLIER_KBYTES | (size >> 10);
    if (size < 1024 * 1024 * 10 ) return MULTIPLIER_MBYTES | (size >> 10);
    if (size < 1024 * 1024 * 200) return MULTIPLIER_MBYTES10 | (size >> 10);
    if (size < (long) 1024 * 1024 * 1024) return MULTIPLIER_MBYTES100 | (size >> 20);
    if (size < (long) 1024 * 1024 * 1024 * 10) return MULTIPLIER_GBYTES | (size >> 20);
    if (size < (long) 1024 * 1024 * 1024 * 200) return MULTIPLIER_GBYTES10 | (size >> 20);
    return MULTIPLIER_GBYTES100 | (size >> 30);
  }

  public void setSizeInBlocks(long blocks, int blockSize) {
    long bytes = blocks * blockSize;
    encodedSize = (blocks << blockOffset) | makeBytesPart(bytes);
  }

  public static class ExcludeFilter {
    public final Map<String, ExcludeFilter> childFilter;

    private static void addEntry(
            TreeMap<String, ArrayList<String>> filter, String name, String value) {
      ArrayList<String> entry = filter.get(name);
      if (entry == null) {
        entry = new ArrayList<String>();
        filter.put(name, entry);
      }
      entry.add(value);
    }

    public ExcludeFilter(ArrayList<String> exclude_paths) {
      if (exclude_paths == null) {
        this.childFilter = null;
        return;
      }
      TreeMap<String, ArrayList<String>> filter =
        new TreeMap<String, ArrayList<String>>();
      for(String path : exclude_paths) {
        String[] parts = path.split("/", 2);
        if (parts.length < 2) {
          addEntry(filter, path, null);
        } else {
          addEntry(filter, parts[0], parts[1]);
        }
      }
      TreeMap<String, ExcludeFilter> excludeFilter = new TreeMap<String, ExcludeFilter>();
      for (Entry<String, ArrayList<String>> entry : filter.entrySet()) {
        boolean has_null = false;
        for (String part : entry.getValue()) {
          if (part == null) {
            has_null = true;
            break;
          }
        }
        if (has_null) {
          excludeFilter.put(entry.getKey(), new ExcludeFilter(null));
        } else {
          excludeFilter.put(entry.getKey(), new ExcludeFilter(entry.getValue()));
        }
      }
      this.childFilter = excludeFilter;
    }
  }

  protected FileSystemEntry(FileSystemEntry parent, String name) {
    this.name = name;
    this.parent = parent;
  }

  public static class Compare implements Comparator<FileSystemEntry> {
    @Override
    public final int compare(FileSystemEntry aa, FileSystemEntry bb) {
      if (aa.encodedSize == bb.encodedSize) {
        return 0;
      }
      return aa.encodedSize < bb.encodedSize ? 1 : -1;
    }
  }

  /**
   * For sorting according to size.
   */
  public static Compare COMPARE = new Compare();

  public FileSystemEntry create() {
    return new FileSystemEntry(null, this.name);
  }

  public class SearchInterruptedException extends RuntimeException {
    private static final long serialVersionUID = -3986013022885904101L;
  };

  public FileSystemEntry copy() {
    if (Thread.interrupted()) throw new SearchInterruptedException();
    FileSystemEntry copy = create();
    if (this.children != null) {
      FileSystemEntry[] children = new FileSystemEntry[this.children.length];
      for (int i = 0; i < this.children.length; i++) {
        FileSystemEntry childCopy = children[i] = this.children[i].copy();
        childCopy.parent = copy;
      }
      copy.children = children;
    }
    copy.encodedSize = this.encodedSize;
    return copy;
  }

  public FileSystemEntry filterChildren(CharSequence pattern, int blockSize) {
//    res = Pattern.compile(Pattern.quote(pattern.toString()), Pattern.CASE_INSENSITIVE).matcher(name).find();

    if (children == null) return null;
    ArrayList<FileSystemEntry> filtered_children = new ArrayList<FileSystemEntry>();

    for (FileSystemEntry child : this.children) {
      FileSystemEntry childCopy = child.filter(pattern, blockSize);
      if (childCopy != null) {
        filtered_children.add(childCopy);
      }
    }
    if (filtered_children.size() == 0) return null;
    FileSystemEntry[] children = new FileSystemEntry[filtered_children.size()];
    filtered_children.toArray(children);
    Arrays.sort(children, COMPARE);
    FileSystemEntry copy = create();
    copy.children = children;
    long size = 0;


    for (FileSystemEntry child : children) {
      size += child.getSizeInBlocks();
      child.parent = copy;
    }
    copy.setSizeInBlocks(size, blockSize);
    return copy;
  }

  public FileSystemEntry filter(CharSequence pattern, int blockSize) {
    if (name.toLowerCase().contains(pattern)) {
      return copy();
    }
    return filterChildren(pattern, blockSize);
  }

  /**
   * Find index of directChild in 'children' field of this entry.
   * @param directChild
   * @return index of the directChild in 'children' field.
   */
  public final int getIndexOf(FileSystemEntry directChild) {
    FileSystemEntry[] children0 = children;
    int len = children0.length;
    int i;

    for (i = 0; i < len; i++) {
      if (children0[i] == directChild) return i;
    }

    throw new RuntimeException("something broken");
  }

  /**
   * Find entry which follows this entry in its the parent.
   * @return next entry in the same parent or this entry if there is no more entries
   */
  public final FileSystemEntry getNext() {
    int index = parent.getIndexOf(this);
    if (index + 1 == parent.children.length) return this;
    return parent.children[index + 1];
  }

  public final String path2() {
    ArrayList<String> pathElements = new ArrayList<String>();
    FileSystemEntry current = this;
    while (current != null) {
      pathElements.add(current.name);
      current = current.parent;
    }
    pathElements.remove(pathElements.size() - 1);
    pathElements.remove(pathElements.size() - 1);
    StringBuilder path = new StringBuilder();
    String sep = "";
    for (int i = pathElements.size() - 1; i >= 0; i--) {
      path.append(sep);
      path.append(pathElements.get(i));
      sep = "/";
    }
    return path.toString();
  }

  /**
   * Walks through the path and finds the specified entry, null otherwise.
   * @param exactMatch TODO
   */
  public FileSystemEntry getEntryByName(String path, boolean exactMatch) {
    Log.d("diskusage", "getEntryForName = " + path);
    String[] pathElements = path.split("/");
    FileSystemEntry entry = this;

    outer:
      for (int i = 0; i < pathElements.length; i++) {
        String name = pathElements[i];
        FileSystemEntry[] children = entry.children;
        if (children == null) {
          return null;
        }
        for (int j = 0; j < children.length; j++) {
          entry = children[j];
          if (name.equals(entry.name)) {
            continue outer;
          }
        }
        return null;
      }
    return entry;
  }

  public static final int padding = 4;

}
