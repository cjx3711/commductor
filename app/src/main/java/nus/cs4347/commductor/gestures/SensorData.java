package nus.cs4347.commductor.gestures;

/**
 * Created by Jonathan on 3/26/17.
 */

public class SensorData {
    private float x;
    private float y;
    private float z;

    public SensorData(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }

    public float getZ(){
        return z;
    }
}
