package nus.cs4347.commductor.synthesizer;

/**
 * Created by glutSolidSphere on 30/3/2017.
 */

public class LookUpTable {
    private static final double TWOPI = 8.*Math.atan(1.);
    private static final int SIZE = 16384;
    private static final double INVERSE_SIZE = 1.f / SIZE;

    private int samplingRate = 8000;

    private double lookUpTable[];

    public LookUpTable (int sr) {
        samplingRate = sr;
        generateLookUpTable();
    }

    private void generateLookUpTable () {
        lookUpTable = new double[SIZE];
        for (int i = 0; i < SIZE; i++) {
            lookUpTable[i] = Math.sin (i * (INVERSE_SIZE) * (TWOPI));
        }
    }

    public double getValAt (int i, double frequency) {
        double index = (i * frequency * SIZE / samplingRate) % SIZE;
        int floorIndex = (int) (Math.floor(index)) % SIZE;
        int ceilIndex = (int) (Math.ceil(index)) % SIZE;

        return (lookUpTable[floorIndex] + (lookUpTable[ceilIndex] - lookUpTable[floorIndex]) * (index - floorIndex));
    }
}