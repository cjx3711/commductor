package nus.cs4347.commductor.bluetooth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Data packet for bluetooth networking
 * Note: Each packet should not exceed 1kB
 */

public class BTDataPacket implements Serializable {
    private int header;
    public String stringData;
    public int intData;
    public float floatData;

    public BTDataPacket(int type) {
        this.header = type;
        stringData = "";
        intData = 0;
        floatData = 0;
    }

    public int getHeader() {
        return header;
    }


    /**
     * Converts this object to a byte array
     * @return Serialised byte array representing the object
     */
    public byte[] convertToBytes() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(this);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * Creates a data packet from a byte array
     * @param bytes byte array to create data packet from
     * @return A new instance of this object. Null if byte array is wrong.
     */
    public static BTDataPacket convertFromBytes(byte[] bytes){
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInput in = new ObjectInputStream(bis);
            return (BTDataPacket) in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
