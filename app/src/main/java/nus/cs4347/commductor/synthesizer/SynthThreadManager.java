package nus.cs4347.commductor.synthesizer;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import nus.cs4347.commductor.synthesizer.SynthesizerThread;

/**
 * Created by glutSolidSphere on 29/3/2017.
 */

public class SynthThreadManager {
    private static final int NUM_THREADS = 2;
    private static final String TAG = "SynthThreadManager";

    private SynthesizerThread synthThreads[];
    private Stack<Integer> unusedThreadIndices;
    private boolean isInitialized = false;

    //HashMap to store user input to thread mapping.
    private Map<Integer, Integer> keyToThreadMap = new HashMap<Integer, Integer>();

    private static SynthThreadManager singleton = new SynthThreadManager();

    private SynthThreadManager() {
        synthThreads = new SynthesizerThread[NUM_THREADS];
        unusedThreadIndices = new Stack<Integer> ();
    }

    public static SynthThreadManager getInstance(){
        return singleton;
    }

    public void init () {
        if(isInitialized){
            return;
        }
        for (int i = 0; i < synthThreads.length; i++) {
            synthThreads[i] = new SynthesizerThread();
            synthThreads[i].start();
            unusedThreadIndices.push(i);
        }
    }

    public boolean playNote (int key) {
        //There is a free thread and the note can be played.
        if (unusedThreadIndices.size() > 0) {
            int threadToUseIndex = unusedThreadIndices.pop();
            synthThreads[threadToUseIndex].setFundamentalFrequency (60 + key);
            synthThreads[threadToUseIndex].startSynthesizing();
            keyToThreadMap.put (key, threadToUseIndex);
            return true;
        }

        return false;
    }

    public boolean stopNote (int key) {
        if (keyToThreadMap.containsKey (key)) {
            int threadToFreeIndex = keyToThreadMap.get (key);
            if (threadToFreeIndex >= 0) {
                synthThreads[threadToFreeIndex].stopSythnesizing();
                unusedThreadIndices.push (threadToFreeIndex);
                keyToThreadMap.put (key, -1);
                return true;
            }
        }
        return false;
    }

    public void destroy() {
        for (int i = 0; i < synthThreads.length; i++) {
            Log.d("Destroying Thread: ", Integer.toString(i));
            synthThreads[i].interrupt();
            synthThreads[i] = null;
        }
        isInitialized = false;
    }
}
