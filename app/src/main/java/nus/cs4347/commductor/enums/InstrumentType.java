package nus.cs4347.commductor.enums;

import java.util.HashMap;
import java.util.Map;

public enum InstrumentType {
    TRIANGLE(0),
    COCONUT(1),
    PIANO(2),
    DRUMS(3);

    private final String [] nameMap = { "Triangle", "Coconut", "Piano", "Drums" };

    private int mValue;

    private static Map<Integer, InstrumentType> map = new HashMap<>();

    static {
        for (InstrumentType type : InstrumentType.values()) {
            map.put(type.mValue, type);
        }
    }

    public static InstrumentType valueOf(int number) {
        return map.get(number);
    }

    InstrumentType(int value) {
        mValue = value;
    }

    public int getInt()
    {
        return mValue;
    }

    public String toString() {
        return nameMap[getInt()];
    }
}