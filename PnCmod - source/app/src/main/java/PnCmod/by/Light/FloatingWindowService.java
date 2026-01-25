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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

    private void loadPresets() {
        try {
            java.io.File presetsFile = new java.io.File(Environment.getExternalStorageDirectory(), "PnCmod/click_presets.json");
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
        Spinner presetSpinner = floatingView.findViewById(R.id.presetSpinner);
        TextView statusText = floatingView.findViewById(R.id.statusText);

        // Setup preset spinner
        List<String> presetNames = new ArrayList<>();
        for (ClickPreset preset : presets) {
            presetNames.add(preset.name + " (" + preset.x + "," + preset.y + ")");
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, presetNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        presetSpinner.setAdapter(adapter);
        
        presetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPresetIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        startButton.setOnClickListener(v -> startClicking());
        stopButton.setOnClickListener(v -> stopClicking());
        closeButton.setOnClickListener(v -> stopSelf());

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
        statusText.setText("Status: " + status + "\nPreset: " + preset);
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
