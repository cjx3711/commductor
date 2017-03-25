package nus.cs4347.commductor;


public class GesturesProcessor {

    public static final int REST = 0;
    public static final int ROLLING_LEFT = 1;
    public static final int ROLLING_RIGHT = 2;
    public static final int TILTING_DOWN = 3;
    public static final int TILTING_UP = 4;

    private static final double PITCH_ANGLE_THRESHOLD = 15.0;
    private static final double ROLL_ANGLE_THRESHOLD = 15.0;

    public static int detectGesture(double pitchAngle, double rollAngle) {
        if(Math.abs(rollAngle) > ROLL_ANGLE_THRESHOLD){
            if(rollAngle > 0){
                return ROLLING_LEFT;
            }
            else {
                return ROLLING_RIGHT;
            }
        }
        else if(Math.abs(pitchAngle) > PITCH_ANGLE_THRESHOLD){
            if(pitchAngle > 0){
                return TILTING_UP;
            }
            else{
                return TILTING_DOWN;
            }
        }
        // Random brownian motion
        else {
            return REST;
        }
    }

    public static String gestureTypeFromCode(int code){
        switch(code){
            case REST:
                return "Rest";
            case ROLLING_LEFT:
                return "Rolling left";
            case ROLLING_RIGHT:
                return "Rolling right";
            case TILTING_DOWN:
                return "Tilting down";
            case TILTING_UP:
                return "Tilting up";
            default:
                return "Rest";
        }
    }
}
