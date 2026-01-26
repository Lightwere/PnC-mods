package PnCUtils.by.Light;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.animation.ValueAnimator;
import android.graphics.Color;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Simple utility app that replaces a specific file with a bundled version,
 * keeping a local backup for restore.
 */
public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "pncutils_prefs";
    private static final String PREF_TARGET_URI = "target_uri";
    private static final String PREF_INITIALIZED = "initialized";

    private static final String TARGET_PATH = "/storage/emulated/0/Android/data/com.global.tmslg/files/ABAsset/1b98f4343ed035646b53cccdb7bd3811.assetbundles";
    private static final String PNCUTILS_FOLDER = "PnCUtils";
    private static final String BACKUP_NAME = "original_backup.bin";
    private static final String ASSET_NAME = "1b98f4343ed035646b53cccdb7bd3811.assetbundles";
    private static final String PRESETS_FILE = "click_presets.json";

    private TextView statusView;
    @Nullable
    private Uri targetUri;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean readGranted = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
                Boolean writeGranted = result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (Boolean.TRUE.equals(readGranted) && Boolean.TRUE.equals(writeGranted)) {
                    updateStatus("Permissions granted");
                    checkManageStoragePermission();
                } else {
                    updateStatus("Required permissions missing");
                    toast("App requires storage access");
                }
            });

    private final ActivityResultLauncher<Intent> pickFileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        // Persist permissions so we can write later without re-prompting.
                        final int flags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                        try {
                            getContentResolver().takePersistableUriPermission(uri, flags);
                        } catch (SecurityException ignored) {
                            // Best effort; continue even if persist fails.
                        }
                        targetUri = uri;
                        saveTargetUri(uri);
                        updateStatus("Selected URI: " + uri);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusView = findViewById(R.id.status);
        Button pickButton = findViewById(R.id.pickButton);
        Button replaceButton = findViewById(R.id.replaceButton);
        Button restoreButton = findViewById(R.id.restoreButton);
        Button autoClickerButton = findViewById(R.id.autoClickerButton);

        targetUri = loadSavedUri();
        if (targetUri != null) {
            updateStatus("Stored URI: " + targetUri);
        } else {
            updateStatus("Default path: " + TARGET_PATH);
        }

        pickButton.setOnClickListener(v -> launchPicker());
        replaceButton.setOnClickListener(v -> doReplace());
        restoreButton.setOnClickListener(v -> doRestore());
        autoClickerButton.setOnClickListener(v -> launchAutoClicker());

        checkAndRequestPermissions();
        initializeExternalFiles();
        setupGradientAnimation();
    }

    private void setupGradientAnimation() {
        TextView creditText = findViewById(R.id.credit);
        
        // Gradient colors: Cyan, Dark Blue, Purple
        int[] colors = {0xFF0099CC, 0xFF1A3A8E, 0xFF7B3FF2};
        
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(4000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new android.view.animation.LinearInterpolator());
        
        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            
            // Smooth cycling through 3 colors
            int color;
            if (progress < 0.33f) {
                // Cyan to Dark Blue
                float fraction = progress / 0.33f;
                color = interpolateColor(colors[0], colors[1], fraction);
            } else if (progress < 0.66f) {
                // Dark Blue to Purple
                float fraction = (progress - 0.33f) / 0.33f;
                color = interpolateColor(colors[1], colors[2], fraction);
            } else {
                // Purple back to Cyan
                float fraction = (progress - 0.66f) / 0.34f;
                color = interpolateColor(colors[2], colors[0], fraction);
            }
            
            creditText.setTextColor(color);
        });
        
        animator.start();
    }

    private int interpolateColor(int color1, int color2, float fraction) {
        int a1 = Color.alpha(color1);
        int r1 = Color.red(color1);
        int g1 = Color.green(color1);
        int b1 = Color.blue(color1);
        
        int a2 = Color.alpha(color2);
        int r2 = Color.red(color2);
        int g2 = Color.green(color2);
        int b2 = Color.blue(color2);
        
        return Color.argb(
                (int) (a1 + (a2 - a1) * fraction),
                (int) (r1 + (r2 - r1) * fraction),
                (int) (g1 + (g2 - g1) * fraction),
                (int) (b1 + (b2 - b1) * fraction)
        );
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+: Check MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                updateStatus("Android 11+: Special permissions required");
                checkManageStoragePermission();
            } else {
                updateStatus("Permissions OK (Android 11+)");
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6-10: Check READ/WRITE permissions
            boolean hasRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            boolean hasWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (!hasRead || !hasWrite) {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                });
            } else {
                updateStatus("Permissions OK");
            }
        } else {
            updateStatus("Permissions OK (Android <6)");
        }
    }

    private void checkManageStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                toast("Go to settings and allow full file access");
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        }
    }

    private void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/octet-stream");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        pickFileLauncher.launch(intent);
    }

    private void doReplace() {
        try {
            Uri target = getResolvedTargetUri();
            if (target == null) {
                toast("Target file not found");
                return;
            }
            backupOriginal(target);
            writeAssetToTarget(target);
            updateStatus("File replaced");
            toast("File replaced");
        } catch (IOException | SecurityException e) {
            updateStatus("Error: " + e.getMessage());
            toast("Error: " + e.getMessage());
        }
    }

    private void doRestore() {
        try {
            Uri target = getResolvedTargetUri();
            if (target == null) {
                toast("Target file not found for restore");
                return;
            }
            File pncutilsDir = new File(Environment.getExternalStorageDirectory(), PNCUTILS_FOLDER);
            File backup = new File(pncutilsDir, BACKUP_NAME);
            if (!backup.exists()) {
                toast("No backup found");
                return;
            }
            copyStream(new FileInputStream(backup), openTargetOutput(target));
            updateStatus("Original restored");
            toast("Original restored");
        } catch (IOException | SecurityException e) {
            updateStatus("Error: " + e.getMessage());
            toast("Error: " + e.getMessage());
        }
    }

    @Nullable
    private Uri getResolvedTargetUri() {
        if (targetUri != null) {
            return targetUri;
        }
        File file = new File(TARGET_PATH);
        if (file.exists()) {
            return Uri.fromFile(file);
        }
        return null;
    }

    private void backupOriginal(Uri target) throws IOException {
        File pncutilsDir = new File(Environment.getExternalStorageDirectory(), PNCUTILS_FOLDER);
        if (!pncutilsDir.exists()) {
            pncutilsDir.mkdirs();
        }
        File backup = new File(pncutilsDir, BACKUP_NAME);
        try (InputStream in = openTargetInput(target); OutputStream out = new FileOutputStream(backup)) {
            copyStream(in, out);
        }
    }

    private void writeAssetToTarget(Uri target) throws IOException {
        try (InputStream in = getAssets().open(ASSET_NAME); OutputStream out = openTargetOutput(target)) {
            copyStream(in, out);
        }
    }

    private InputStream openTargetInput(Uri target) throws IOException {
        if ("content".equals(target.getScheme())) {
            InputStream in = getContentResolver().openInputStream(target);
            if (in == null) throw new IOException("Nie można otworzyć input stream");
            return in;
        }
        return new FileInputStream(new File(target.getPath()));
    }

    private OutputStream openTargetOutput(Uri target) throws IOException {
        if ("content".equals(target.getScheme())) {
            OutputStream out = getContentResolver().openOutputStream(target, "rwt");
            if (out == null) throw new IOException("Nie można otworzyć output stream");
            return out;
        }
        return new FileOutputStream(new File(target.getPath()));
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8 * 1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }

    private void saveTargetUri(Uri uri) {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().putString(PREF_TARGET_URI, uri.toString()).apply();
    }

    @Nullable
    private Uri loadSavedUri() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String stored = prefs.getString(PREF_TARGET_URI, null);
        if (stored != null) {
            return Uri.parse(stored);
        }
        return null;
    }

    private void updateStatus(String message) {
        statusView.setText(message);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void initializeExternalFiles() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getBoolean(PREF_INITIALIZED, false)) {
            return; // Already initialized
        }

        try {
            // Create PnCUtils directory
            File pncutilsDir = new File(Environment.getExternalStorageDirectory(), PNCUTILS_FOLDER);
            if (!pncutilsDir.exists()) {
                pncutilsDir.mkdirs();
            }

            // Copy presets file from assets to external storage
            File presetsFile = new File(pncutilsDir, PRESETS_FILE);
            if (!presetsFile.exists()) {
                try (InputStream in = getAssets().open(PRESETS_FILE);
                     OutputStream out = new FileOutputStream(presetsFile)) {
                    copyStream(in, out);
                }
            }

            // Copy asset file to PnCUtils folder for easy access
            File assetFile = new File(pncutilsDir, ASSET_NAME);
            if (!assetFile.exists()) {
                try (InputStream in = getAssets().open(ASSET_NAME);
                     OutputStream out = new FileOutputStream(assetFile)) {
                    copyStream(in, out);
                }
            }

            prefs.edit().putBoolean(PREF_INITIALIZED, true).apply();
            
            String message = "PnCUtils files created at:\n/sdcard/" + PNCUTILS_FOLDER + "/";
            updateStatus(message);
            toast("Setup complete! Check /sdcard/PnCUtils/");
        } catch (IOException e) {
            toast("Error initializing files: " + e.getMessage());
        }
    }

    private void launchAutoClicker() {
        // Check overlay permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                toast("Please enable overlay permission");
                return;
            }
        }

        // Check accessibility service
        if (AutoClickerAccessibilityService.getInstance() == null) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            toast("Please enable PnCUtils Accessibility Service");
            return;
        }

        // Start floating window service
        Intent serviceIntent = new Intent(this, FloatingWindowService.class);
        startService(serviceIntent);
        toast("Auto-clicker launched");
    }
}
