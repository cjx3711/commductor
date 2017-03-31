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
    private static final int NUM_THREADS = 5;
    private static final String TAG = "SynthThreadManager";

    private SynthesizerThread synthThreads[];
    private Stack<Integer> unusedThreadIndices;

    //HashMap to store user input to thread mapping.
    private Map<Integer, Integer> keyToThreadMap = new HashMap<Integer, Integer>();

    public SynthThreadManager() {
        synthThreads = new SynthesizerThread[NUM_THREADS];
        unusedThreadIndices = new Stack<Integer> ();
        for (int i = 0; i < synthThreads.length; i++) {
            synthThreads[i] = new SynthesizerThread();
            unusedThreadIndices.push (i);
        }
    }

    public void init () {
        for (int i = 0; i < synthThreads.length; i++) {
            synthThreads[i].start();
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
            synthThreads[i].destroy();
            try {
                synthThreads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synthThreads[i] = null;
        }
    }
}
