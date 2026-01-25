package PnCmod.by.Light;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FloatingWindowService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private Handler handler = new Handler();
    private Runnable clickRunnable;
    private boolean isRunning = false;
    
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    
    private List<ClickPreset> presets = new ArrayList<>();
    private int selectedPresetIndex = 0;
    private int clickDelay = 1000;

    private ArrayAdapter<String> adapter;
    private EditText nameInput;
    private EditText xInput;
    private EditText yInput;
    private EditText delayInput;
    private View contentContainer;
    private Button minimizeButton;
    private boolean isMinimized = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loadPresets();
        
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_window, null);

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 100;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingView, params);

        setupDragging();
        setupUI();
    }

    private java.io.File getPresetsFile() {
        return new java.io.File(Environment.getExternalStorageDirectory(), "PnCmod/click_presets.json");
    }

    private void loadPresets() {
        presets.clear();
        try {
            java.io.File presetsFile = getPresetsFile();
            InputStream is;

            if (presetsFile.exists()) {
                is = new java.io.FileInputStream(presetsFile);
            } else {
                // Fallback to assets if external file doesn't exist
                is = getAssets().open("click_presets.json");
            }

            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            JSONObject obj = new JSONObject(json);
            JSONArray presetsArray = obj.getJSONArray("presets");
            clickDelay = obj.optInt("clickDelay", 1000);

            for (int i = 0; i < presetsArray.length(); i++) {
                JSONObject preset = presetsArray.getJSONObject(i);
                presets.add(new ClickPreset(
                        preset.getInt("id"),
                        preset.getString("name"),
                        preset.getInt("x"),
                        preset.getInt("y")
                ));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading presets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePresets() {
        try {
            JSONArray arr = new JSONArray();
            for (ClickPreset preset : presets) {
                JSONObject obj = new JSONObject();
                obj.put("id", preset.id);
                obj.put("name", preset.name);
                obj.put("x", preset.x);
                obj.put("y", preset.y);
                arr.put(obj);
            }

            JSONObject root = new JSONObject();
            root.put("presets", arr);
            root.put("clickDelay", clickDelay);

            java.io.File presetsFile = getPresetsFile();
            if (!presetsFile.getParentFile().exists()) {
                presetsFile.getParentFile().mkdirs();
            }
            java.io.FileOutputStream fos = new java.io.FileOutputStream(presetsFile);
            fos.write(root.toString(2).getBytes("UTF-8"));
            fos.close();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving presets: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupDragging() {
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                        
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void setupUI() {
        Button startButton = floatingView.findViewById(R.id.startButton);
        Button stopButton = floatingView.findViewById(R.id.stopButton);
        Button closeButton = floatingView.findViewById(R.id.closeButton);
        Button addUpdateButton = floatingView.findViewById(R.id.addUpdateButton);
        Button deleteButton = floatingView.findViewById(R.id.deleteButton);
        Button setDelayButton = floatingView.findViewById(R.id.setDelayButton);
        Button resetDefaultsButton = floatingView.findViewById(R.id.resetDefaultsButton);
        Spinner presetSpinner = floatingView.findViewById(R.id.presetSpinner);
        TextView statusText = floatingView.findViewById(R.id.statusText);
        nameInput = floatingView.findViewById(R.id.nameInput);
        xInput = floatingView.findViewById(R.id.xInput);
        yInput = floatingView.findViewById(R.id.yInput);
        delayInput = floatingView.findViewById(R.id.delayInput);
        contentContainer = floatingView.findViewById(R.id.contentContainer);
        minimizeButton = floatingView.findViewById(R.id.minimizeButton);

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, buildPresetNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        presetSpinner.setAdapter(adapter);

        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPresetIndex = position;
                populateForm();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        startButton.setOnClickListener(v -> startClicking());
        stopButton.setOnClickListener(v -> stopClicking());
        closeButton.setOnClickListener(v -> stopSelf());
        addUpdateButton.setOnClickListener(v -> addOrUpdatePreset());
        deleteButton.setOnClickListener(v -> deletePreset());
        setDelayButton.setOnClickListener(v -> applyDelay());
        minimizeButton.setOnClickListener(v -> toggleMinimize());
        resetDefaultsButton.setOnClickListener(v -> resetToDefaults());

        populateForm();
        updateStatus(statusText);
    }

    private void startClicking() {
        if (isRunning) return;
        
        AutoClickerAccessibilityService service = AutoClickerAccessibilityService.getInstance();
        if (service == null) {
            Toast.makeText(this, "Accessibility service not enabled!", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (presets.isEmpty()) {
            Toast.makeText(this, "No presets loaded!", Toast.LENGTH_SHORT).show();
            return;
        }

        isRunning = true;
        TextView statusText = floatingView.findViewById(R.id.statusText);
        
        clickRunnable = new Runnable() {
            @Override
            public void run() {
                if (isRunning && selectedPresetIndex < presets.size()) {
                    ClickPreset preset = presets.get(selectedPresetIndex);
                    service.performClick(preset.x, preset.y);
                    updateStatus(statusText);
                    handler.postDelayed(this, clickDelay);
                }
            }
        };
        
        handler.post(clickRunnable);
        Toast.makeText(this, "Auto-clicker started", Toast.LENGTH_SHORT).show();
    }

    private void stopClicking() {
        isRunning = false;
        if (clickRunnable != null) {
            handler.removeCallbacks(clickRunnable);
        }
        TextView statusText = floatingView.findViewById(R.id.statusText);
        updateStatus(statusText);
        Toast.makeText(this, "Auto-clicker stopped", Toast.LENGTH_SHORT).show();
    }

    private void updateStatus(TextView statusText) {
        String status = isRunning ? "Running" : "Stopped";
        String preset = presets.isEmpty() ? "None" : 
            presets.get(selectedPresetIndex).name + " (" + 
            presets.get(selectedPresetIndex).x + "," + 
            presets.get(selectedPresetIndex).y + ")";
        statusText.setText("Status: " + status + "\nPreset: " + preset + "\nDelay: " + clickDelay + " ms");
    }

    private void toggleMinimize() {
        isMinimized = !isMinimized;
        contentContainer.setVisibility(isMinimized ? View.GONE : View.VISIBLE);
        minimizeButton.setText(isMinimized ? "+" : "-");
    }

    private List<String> buildPresetNames() {
        List<String> names = new ArrayList<>();
        for (ClickPreset preset : presets) {
            names.add(preset.name + " (" + preset.x + "," + preset.y + ")");
        }
        return names;
    }

    private void refreshAdapter() {
        adapter.clear();
        adapter.addAll(buildPresetNames());
        adapter.notifyDataSetChanged();
    }

    private void populateForm() {
        if (presets.isEmpty() || selectedPresetIndex >= presets.size()) {
            nameInput.setText("");
            xInput.setText("");
            yInput.setText("");
            delayInput.setHint("Delay ms (current " + clickDelay + ")");
            return;
        }
        ClickPreset preset = presets.get(selectedPresetIndex);
        nameInput.setText(preset.name);
        xInput.setText(String.valueOf(preset.x));
        yInput.setText(String.valueOf(preset.y));
        delayInput.setHint("Delay ms (current " + clickDelay + ")");
    }

    private void addOrUpdatePreset() {
        String name = nameInput.getText().toString().trim();
        String xStr = xInput.getText().toString().trim();
        String yStr = yInput.getText().toString().trim();

        if (name.isEmpty() || xStr.isEmpty() || yStr.isEmpty()) {
            Toast.makeText(this, "Enter name, X, Y", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int x = Integer.parseInt(xStr);
            int y = Integer.parseInt(yStr);

            // If a preset with the same name exists, update it; else add new
            boolean updated = false;
            for (int i = 0; i < presets.size(); i++) {
                if (presets.get(i).name.equalsIgnoreCase(name)) {
                    presets.set(i, new ClickPreset(presets.get(i).id, name, x, y));
                    selectedPresetIndex = i;
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                int nextId = presets.isEmpty() ? 1 : presets.get(presets.size() - 1).id + 1;
                presets.add(new ClickPreset(nextId, name, x, y));
                selectedPresetIndex = presets.size() - 1;
            }

            savePresets();
            refreshAdapter();
            populateForm();
            updateStatus((TextView) floatingView.findViewById(R.id.statusText));
            Toast.makeText(this, "Preset saved", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid X or Y", Toast.LENGTH_SHORT).show();
        }
    }

    private void deletePreset() {
        if (presets.isEmpty() || selectedPresetIndex >= presets.size()) {
            Toast.makeText(this, "No preset selected", Toast.LENGTH_SHORT).show();
            return;
        }
        presets.remove(selectedPresetIndex);
        if (selectedPresetIndex > 0) {
            selectedPresetIndex--;
        }
        savePresets();
        refreshAdapter();
        populateForm();
        updateStatus((TextView) floatingView.findViewById(R.id.statusText));
        Toast.makeText(this, "Preset deleted", Toast.LENGTH_SHORT).show();
    }

    private void applyDelay() {
        String delayStr = delayInput.getText().toString().trim();
        if (delayStr.isEmpty()) {
            Toast.makeText(this, "Enter delay in ms", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            int newDelay = Integer.parseInt(delayStr);
            if (newDelay < 50) newDelay = 50;
            clickDelay = newDelay;
            savePresets();
            updateStatus((TextView) floatingView.findViewById(R.id.statusText));
            Toast.makeText(this, "Delay set to " + clickDelay + " ms", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid delay", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetToDefaults() {
        try {
            InputStream is = getAssets().open("click_presets.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            JSONObject obj = new JSONObject(json);

            // Write defaults to external file and reload
            java.io.File presetsFile = getPresetsFile();
            if (!presetsFile.getParentFile().exists()) {
                presetsFile.getParentFile().mkdirs();
            }
            java.io.FileOutputStream fos = new java.io.FileOutputStream(presetsFile);
            fos.write(obj.toString(2).getBytes("UTF-8"));
            fos.close();

            loadPresets();
            refreshAdapter();
            populateForm();
            updateStatus((TextView) floatingView.findViewById(R.id.statusText));
            Toast.makeText(this, "Defaults restored", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error resetting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopClicking();
        if (floatingView != null) {
            windowManager.removeView(floatingView);
        }
    }

    private static class ClickPreset {
        int id;
        String name;
        int x;
        int y;

        ClickPreset(int id, String name, int x, int y) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
        }
    }
}
