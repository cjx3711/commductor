package nus.cs4347.commductor.client;

import nus.cs4347.commductor.enums.InstrumentType;

/**
 * This object represents the state of a given instrument
 */

public class Instrumentalist {
    protected InstrumentType type;

    // Modifiers range from 0 to 1. These numbers can be used for anything.
    protected float modifier1;
    protected float modifier2;

    public Instrumentalist() {
        modifier1 = 1;
        modifier2 = 1;
        type = null;
    }

    public InstrumentType getType() {
        return type;
    }

    public void setType(InstrumentType type) {
        this.type = type;
    }

    public void changeModifier1(float by) {
        modifier1 += by;
        if ( modifier1 > 1 ) {
            modifier1 = 1;
        }
        if ( modifier1 < 0 ) {
            modifier1 = 0;
        }
    }

    public void changeModifier2(float by) {
        modifier2 += by;
        if ( modifier2 > 1 ) {
            modifier2 = 1;
        }
        if ( modifier2 < 0 ) {
            modifier2 = 0;
        }
    }

    public void setModifier1(float modifier1) {
        this.modifier1 = modifier1;
        if ( this.modifier1 > 1 ) {
            this.modifier1 = 1;
        }
        if ( this.modifier1 < 0 ) {
            this.modifier1 = 0;
        }
    }

    public void setModifier2(float modifier2) {
        this.modifier2 = modifier2;
        if ( this.modifier2 > 1 ) {
            this.modifier2 = 1;
        }
        if ( this.modifier2 < 0 ) {
            this.modifier2 = 0;
        }
    }

    public float getModifier1() {
        return modifier1;
    }

    public float getModifier2() {
        return modifier2;
    }
}

