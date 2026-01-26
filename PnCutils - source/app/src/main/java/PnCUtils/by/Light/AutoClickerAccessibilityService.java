package PnCUtils.by.Light;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

/**
 * Accessibility service to perform automated clicks at specified coordinates.
 */
public class AutoClickerAccessibilityService extends AccessibilityService {
    private static AutoClickerAccessibilityService instance;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Not needed for our use case
    }

    @Override
    public void onInterrupt() {
        // Called when service is interrupted
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
    }

    public static AutoClickerAccessibilityService getInstance() {
        return instance;
    }

    /**
     * Perform a click at the specified coordinates.
     * @param x X coordinate
     * @param y Y coordinate
     * @return true if click was dispatched successfully
     */
    public boolean performClick(int x, int y) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            return false;
        }

        Path clickPath = new Path();
        clickPath.moveTo(x, y);
        
        GestureDescription.StrokeDescription strokeDescription = 
            new GestureDescription.StrokeDescription(clickPath, 0, 50);
        
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(strokeDescription);
        
        return dispatchGesture(gestureBuilder.build(), null, null);
    }
}
