package com.guardianai.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.Executors;

public class UpdateManager {

    public static final String UPDATE_JSON_URL = "https://raw.githubusercontent.com/harsha-amarthaluri/GuardianAI_Full/refs/heads/main/Apk/update.json";

    public static class UpdateInfo {
        private final int versionCode;
        private final String versionName;
        private final String downloadUrl;
        private final String sha256;
        private final String changelog;

        public UpdateInfo(int versionCode, String versionName, String downloadUrl, String sha256, String changelog) {
            this.versionCode = versionCode;
            this.versionName = versionName;
            this.downloadUrl = downloadUrl;
            this.sha256 = sha256;
            this.changelog = changelog;
        }

        public int getVersionCode() { return versionCode; }
        public String getVersionName() { return versionName; }
        public String getDownloadUrl() { return downloadUrl; }
        public String getSha256() { return sha256; }
        public String getChangelog() { return changelog; }
    }

    public interface UpdateCheckCallback {
        void onUpdateAvailable(UpdateInfo info, int currentVersionCode);
        void onNoUpdateAvailable(int currentVersionCode);
        void onError(String error);
    }

    public interface ProgressCallback {
        void onProgress(int progress, long downloadedBytes, long totalBytes);
        void onDownloadComplete(File apkFile);
        void onError(String error);
    }

    public static void checkForUpdates(Context context, UpdateCheckCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            try {
                int currentCode = getCurrentVersionCode(context);
                URL url = new URL(UPDATE_JSON_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    int remoteVersionCode = json.optInt("versionCode", 1);
                    String remoteVersionName = json.optString("versionName", "1.0.0");
                    String downloadUrl = json.optString("downloadUrl", "");
                    String sha256 = json.optString("sha256", "");
                    String changelog = json.optString("changelog", "Performance improvements & bug fixes.");

                    UpdateInfo info = new UpdateInfo(remoteVersionCode, remoteVersionName, downloadUrl, sha256, changelog);

                    mainHandler.post(() -> {
                        if (remoteVersionCode > currentCode) {
                            callback.onUpdateAvailable(info, currentCode);
                        } else {
                            callback.onNoUpdateAvailable(currentCode);
                        }
                    });
                } else {
                    mainHandler.post(() -> callback.onError("Failed to fetch update info (HTTP " + responseCode + ")"));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Update check failed: " + e.getLocalizedMessage()));
            }
        });
    }

    public static void downloadAndInstallApk(Activity activity, String downloadUrl, String expectedSha256, ProgressCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    mainHandler.post(() -> callback.onError("Server returned HTTP " + responseCode));
                    return;
                }

                long fileLength = conn.getContentLength();
                File targetFile = new File(activity.getExternalCacheDir(), "guardianai_update.apk");
                if (targetFile.exists()) {
                    targetFile.delete();
                }

                InputStream input = conn.getInputStream();
                FileOutputStream output = new FileOutputStream(targetFile);

                byte[] buffer = new byte[8192];
                long total = 0;
                int count;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    output.write(buffer, 0, count);

                    final long downloaded = total;
                    final int progress = fileLength > 0 ? (int) ((downloaded * 100) / fileLength) : -1;
                    mainHandler.post(() -> callback.onProgress(progress, downloaded, fileLength));
                }

                output.flush();
                output.close();
                input.close();

                // SHA-256 Checksum Validation
                if (expectedSha256 != null && !expectedSha256.trim().isEmpty()) {
                    String actualSha256 = calculateSha256(targetFile);
                    if (actualSha256 == null || !actualSha256.equalsIgnoreCase(expectedSha256.trim())) {
                        targetFile.delete();
                        mainHandler.post(() -> callback.onError("APK Integrity Check Failed! SHA-256 hash mismatch. Update installation aborted for security."));
                        return;
                    }
                }

                mainHandler.post(() -> {
                    callback.onDownloadComplete(targetFile);
                    installApk(activity, targetFile);
                });

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Download failed: " + e.getLocalizedMessage()));
            }
        });
    }

    public static String calculateSha256(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int count;
            while ((count = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, count);
            }
            fis.close();
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static void installApk(Activity activity, File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!activity.getPackageManager().canRequestPackageInstalls()) {
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivity(permissionIntent);
            }
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri apkUri = FileProvider.getUriForFile(
                activity,
                activity.getPackageName() + ".fileprovider",
                apkFile
        );

        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
    }

    public static int getCurrentVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) pInfo.getLongVersionCode();
            } else {
                return pInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return 1;
        }
    }

    public static String getCurrentVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName != null ? pInfo.versionName : "1.0.0";
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }
}
