package nus.cs4347.commductor.bluetooth;

import java.util.HashMap;
import java.util.Map;

/**
 * The headers for the bluetooth packets
 * Naming convention:
 *    First word is CLIENT or SERVER
 *    CLIENT means it's a packet from client to server
 *    SERVER means it's a packet from server to client
 *
 *    If it has none, it means it's a general command
 */

public enum BTPacketHeader {
    SEND_SUCCESS(0),
    SEND_FAILURE(1),
    STRING_DATA(2),
    CLIENT_INSTRUMENT_TYPE(3),
    SERVER_START_GAME(4);

    private int mValue;

    private static Map<Integer, BTPacketHeader> map = new HashMap<>();

    static {
        for (BTPacketHeader type : BTPacketHeader.values()) {
            map.put(type.mValue, type);
        }
    }

    public static BTPacketHeader valueOf(int number) {
        return map.get(number);
    }

    BTPacketHeader(int value) {
        mValue = value;
    }

    public int getInt()
    {
        return mValue;
    }
}
