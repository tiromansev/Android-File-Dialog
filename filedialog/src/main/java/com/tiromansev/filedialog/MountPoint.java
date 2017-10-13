package com.tiromansev.filedialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.tiromansev.filedialog.datasource.DataSource;
import com.tiromansev.filedialog.datasource.PortableFile;
import com.tiromansev.filedialog.datasource.StatFsSource;
import com.tiromansev.filedialog.entity.FileSystemEntry;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MountPoint {
  final FileSystemEntry.ExcludeFilter excludeFilter;
  public String title;
  final String root;
  final boolean hasApps2SD;
  final boolean rootRequired;
  final boolean forceHasApps;
  final String fsType;

  MountPoint(String title, String root, FileSystemEntry.ExcludeFilter excludeFilter,
             boolean hasApps2SD, boolean rootRequired, String fsType, boolean forceHasApps) {
    this.title = title;
    this.root = root;
    this.excludeFilter = excludeFilter;
    this.hasApps2SD = hasApps2SD;
    this.rootRequired = rootRequired;
    this.fsType = fsType;
    this.forceHasApps = forceHasApps;
  }

  private static MountPoint defaultStorage;
  private static Map<String, MountPoint> mountPoints = new TreeMap<String, MountPoint>();
  private static Map<String, MountPoint> rootedMountPoints = new TreeMap<String, MountPoint>();
  private static List<MountPoint> storageMountPoints = new ArrayList<MountPoint>();
  private static boolean init = false;
  private static MountPoint honeycombSdcard;
  static int checksum = 0;

  public static Map<String,MountPoint> getMountPoints(Context context) {
    initMountPoints(context);
    return mountPoints;
  }

  public static List<String> getPreLollipopMountPoints() {
    List<String> rootDirList = new ArrayList<>();
    String rootDir;
    rootDir = System.getenv("EXTERNAL_STORAGE");
    if (rootDir != null) {
      for (String dir: rootDir.split(":")) {
        rootDirList.add(new File(dir).getAbsolutePath());
      }
    }
    rootDir = System.getenv("SECONDARY_STORAGE");
    if (rootDir != null) {
      for (String dir: rootDir.split(":")) {
        rootDirList.add(new File(dir).getAbsolutePath());
      }
    }
    rootDir = System.getenv("EXTERNAL_SDCARD_STORAGE");
    if (rootDir != null) {
      for (String dir: rootDir.split(":")) {
        rootDirList.add(new File(dir).getAbsolutePath());
      }
    }
    rootDir = System.getenv("EXTERNAL_SD_STORAGE");
    if (rootDir != null) {
      for (String dir: rootDir.split(":")) {
        rootDirList.add(new File(dir).getAbsolutePath());
      }
    }
    rootDir = System.getenv("EXTERNAL_STORAGE_DOCOMO");
    if (rootDir != null) {
      for (String dir: rootDir.split(":")) {
        rootDirList.add(new File(dir).getAbsolutePath());
      }
    }
    return rootDirList;
  }

  public static boolean checkWriteMountPoint(String mountPath) {
    File appDir = new File(mountPath);
    //проверяем возможность использования папки
    if (appDir.exists() && appDir.isDirectory() && appDir.canWrite()) {
      return true;
    }
    //пытаемся создать папку
    else {
      if (appDir.mkdir()) {
        return true;
      }
    }

    return true;
  }

  public String getRoot() {
    return root;
  }

  public static String storageCardPath() {
    PortableFile externalStorageDirectory = DataSource.get().getExternalStorageDirectory();
    return externalStorageDirectory.getCanonicalPath();
  }

  private static boolean isEmulated(String fsType) {
    return fsType.equals("sdcardfs") || fsType.equals("fuse");
  }

  public static int calcHash(String path) {
    File dir = new File(path);
    StringBuilder tmpHash = new StringBuilder();

    tmpHash.append(dir.getTotalSpace());
    tmpHash.append(dir.getUsableSpace());

    File[] list = dir.listFiles();
    if (list != null) {
      for (File file : list) {
        tmpHash.append(file.getName());
        if (file.isFile()) {
          tmpHash.append(file.length());
        }
      }
    }

    return tmpHash.toString().hashCode();
  }

  private static void initMountPoints(Context context) {
    if (init) return;
    init = true;
    String storagePath = storageCardPath();
    Log.d("diskusage", "StoragePath: " + storagePath);

    ArrayList<MountPoint> mountPointsList = new ArrayList<MountPoint>();
    HashSet<String> excludePoints = new HashSet<String>();
    if (storagePath != null) {
      defaultStorage = new MountPoint(
              titleStorageCard(context), storagePath, null, false, false, "", false);
      mountPointsList.add(defaultStorage);
      mountPoints.put(storagePath, defaultStorage);
    }

    try {
      // FIXME: debug
      checksum = 0;
      BufferedReader reader = DataSource.get().getProcReader();
      String line;
      while ((line = reader.readLine()) != null) {
        checksum += line.length();
        Log.d("diskusage", "line: " + line);
        String[] parts = line.split(" +");
        if (parts.length < 3) continue;
        String mountPoint = parts[1];
        Log.d("diskusage", "Mount point: " + mountPoint);
        String fsType = parts[2];

        StatFsSource stat = null;
        try {
          stat = DataSource.get().statFs(mountPoint);
        } catch (Exception e) {
        }

        if (!(fsType.equals("vfat") || fsType.equals("tntfs") || fsType.equals("exfat")
            || fsType.equals("texfat") || isEmulated(fsType))
            || mountPoint.startsWith("/mnt/asec")
            || mountPoint.startsWith("/firmware")
            || mountPoint.startsWith("/mnt/secure")
            || mountPoint.startsWith("/data/mac")
            || stat == null
            || (mountPoint.endsWith("/legacy") && isEmulated(fsType))) {
          Log.d("diskusage", String.format("Excluded based on fsType=%s or black list", fsType));
          excludePoints.add(mountPoint);

          // Default storage is not vfat, removing it (real honeycomb)
          if (mountPoint.equals(storagePath)) {
            mountPointsList.remove(defaultStorage);
            mountPoints.remove(mountPoint);
          }
          if (/*rooted &&*/ !mountPoint.startsWith("/mnt/asec/")) {
            mountPointsList.add(new MountPoint(mountPoint, mountPoint, null, false, true, fsType, false));
          }
        } else {
          Log.d("diskusage", "Mount point is good");
          MountPoint mountPointObj = new MountPoint(mountPoint, mountPoint, null, false, false, fsType, false);
          mountPointsList.add(mountPointObj);
          if (mountPoint.startsWith("/storage/") && !mountPoint.startsWith("/storage/emulated")) {
            storageMountPoints.add(mountPointObj);
          }
        }
      }
      reader.close();

      for (MountPoint mountPoint: mountPointsList) {
        String prefix = mountPoint.root + "/";
        boolean has_apps2sd = false;
        ArrayList<String> excludes = new ArrayList<String>();
        String mountPointName = new File(mountPoint.root).getName();

        for (MountPoint otherMountPoint : mountPointsList) {
          if (otherMountPoint.root.startsWith(prefix)) {
            excludes.add(mountPointName + "/" + otherMountPoint.root.substring(prefix.length()));
          }
        }
        for (String otherMountPoint : excludePoints) {
          if (otherMountPoint.equals(prefix + ".android_secure")) {
            has_apps2sd = true;
          }
          if (otherMountPoint.startsWith(prefix)) {
            excludes.add(mountPointName + "/" + otherMountPoint.substring(prefix.length()));
          }
        }
        MountPoint newMountPoint = new MountPoint(
            mountPoint.root, mountPoint.root, new FileSystemEntry.ExcludeFilter(excludes),
            has_apps2sd, mountPoint.rootRequired, mountPoint.fsType, false);
        if (mountPoint.rootRequired) {
          rootedMountPoints.put(mountPoint.root, newMountPoint);
        } else {
          mountPoints.put(mountPoint.root, newMountPoint);
        }
      }
    } catch (Exception e) {
      Log.e("diskusage", "Failed to get mount points", e);
    }
    final int sdkVersion = DataSource.get().getAndroidVersion();

    try {
      addMediaPaths(context);
    } catch (Throwable t) {
      Log.e("diskusage", "Adding media paths", t);
    }

    MountPoint storageCard = mountPoints.get(storageCardPath());
    if(sdkVersion >= Build.VERSION_CODES.HONEYCOMB
        && (storageCard == null || isEmulated(storageCard.fsType))) {
      mountPoints.remove(storageCardPath());
      // No real /sdcard in honeycomb
      honeycombSdcard = defaultStorage;
      mountPoints.put("/data", new MountPoint(
              titleStorageCard(context), "/data", null, false, false, "", true));
    }

    if (!mountPoints.isEmpty()) {
      defaultStorage = mountPoints.values().iterator().next();
      defaultStorage.title = titleStorageCard(context);
    }

    if (sdkVersion >= Build.VERSION_CODES.LOLLIPOP) {
      initMountPointsLollipop(context);
    }
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static PortableFile getBaseDir(PortableFile dir) {
    if (dir == null) {
      return null;
    }

    long totalSpace = dir.getTotalSpace();
    while (true) {
      PortableFile base = DataSource.get().getParentFile(dir);
      try {
        base.isExternalStorageEmulated();
      } catch (Exception e) {
        return dir;
      }
      if (base == null || dir.equals(base) || base.getTotalSpace() != totalSpace) {
        return dir;
      }
      dir = base;
    }
  }

  // Lollipop have new API to get storage states, which can try to use it instead complicated
  // legacy stuff.
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static void initMountPointsLollipop(Context context) {
    Map<String, MountPoint> mountPoints = new TreeMap<String, MountPoint>();
    PortableFile defaultDir = getBaseDir(DataSource.get().getExternalFilesDir(context));
    PortableFile[] dirs = DataSource.get().getExternalFilesDirs(context);
    for (PortableFile path : dirs) {
      if (path == null) {
        continue;
      }
      PortableFile dir = getBaseDir(path);
      boolean isEmulated = false;
      boolean isRemovable = false;
      boolean hasApps = false;
      try {
        isEmulated = path.isExternalStorageEmulated();
        isRemovable = path.isExternalStorageRemovable();
        hasApps = isEmulated && !isRemovable;
      } catch (Exception e) {
        e.printStackTrace();
      }
      MountPoint mountPoint = new MountPoint(
          dir.equals(defaultDir) ? titleStorageCard(context) : dir.getAbsolutePath(),
          dir.getAbsolutePath(),
          new FileSystemEntry.ExcludeFilter(new ArrayList<String>()),
          false /* hasApps2SD */,
          false /* rootRequired */,
          "whoCares",
          hasApps /* forceHasApps */);
      mountPoints.put(mountPoint.root, mountPoint);

      if (!isRemovable) {
        defaultStorage = mountPoint;
        honeycombSdcard = mountPoint;
      }
    }
    for (MountPoint m : storageMountPoints) {
      if (!mountPoints.containsKey(m.root)) {
        mountPoints.put(m.root, m);
      }
    }

    MountPoint.mountPoints = mountPoints;
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  public static PortableFile[] getMediaStoragePaths(Context context) {
    try {
      return DataSource.get().getExternalFilesDirs(context);
    } catch (Throwable t) {
      return new PortableFile[0];
    }
  }

  private static void addMediaPaths(Context context) {
    PortableFile[] mediaStoragePaths = getMediaStoragePaths(context);
    for (PortableFile file : mediaStoragePaths) {
      while (file != null) {
        String canonical = file.getCanonicalPath();

        if (mountPoints.containsKey(canonical)) {
          break;
        }

        MountPoint rootedMountPoint = rootedMountPoints.get(canonical);
        if (rootedMountPoint != null) {
          mountPoints.put(canonical, new MountPoint(
              canonical,
              canonical,
              null,
              false,
              false,
              rootedMountPoint.fsType, false));
          break;
        }
        if (canonical.equals("/")) break;
        file = DataSource.get().getParentFile(file);
      }
    }
  }

  private static String titleStorageCard(Context context) {
    return context.getString(R.string.storage_card);
  }
}
