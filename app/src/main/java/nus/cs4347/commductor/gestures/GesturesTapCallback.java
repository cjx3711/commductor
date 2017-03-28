package nus.cs4347.commductor.gestures;

/**
 * Created by Jonathan on 3/27/17.
 */

public interface GesturesTapCallback {
    void tapDetected();
}

/* Example callback in your class

GesturesTapCallback tapCallback = new GesturesTapCallback(){
            public void tapDetected(){
                // Play sound upon tap...
                gestureText.setText("Tap detected!" + numTaps);
                numTaps += 1;
            }
        };

Then init:

GesturesProcessor.getInstance().init(tapCallback);

*/
