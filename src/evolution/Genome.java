package evolution;

import java.util.Random;


/**
 * Created by Erik on 11/30/2015.
 */
public class Genome {

//    public int[]  gearUp={5000,6000,6000,6500,7000,0};
//    public int[]  gearDown={0,2500,3000,3000,3500,3500};

    public float almost0 = 0.000001f;

    /* Stuck constants*/
    public int stuckTime = 25;
    public float stuckAngle = (float) 0.523598775; //PI/6

    /* Accel and Brake Constants*/
    public float maxSpeedDist = 10;
    public float maxSpeed = 40;

    /* Steering constants*/
    public float steerLock = (float) 0.785398;
    public float steerSensitivityOffset = (float) 80.0;
    public float wheelSensitivityCoeff = 1;

    /* ABS Filter Constants */
    public float wheelRadius[] = {(float) 0.3179, (float) 0.3179, (float) 0.3276, (float) 0.3276};
    public float absSlip = (float) 2.0;
    public float absRange = (float) 3.0;
    public float absMinSpeed = (float) 3.0;

    /* Clutching Constants */
    public float clutchMax = (float) 0.5;
    public float clutchDelta = (float) 0.05;
    public float clutchRange = (float) 0.82;
    public float clutchDeltaTime = (float) 0.02;
    public float clutchDeltaRaced = 10;
    public float clutchDec = (float) 0.01;
    public float clutchMaxModifier = (float) 1.3;
    public float clutchMaxTime = (float) 1.5;

    public Genome() {
        makeRandomGenome();
    }

    private void makeRandomGenome() {
        Random rand = new Random();
        stuckTime = rand.nextInt(100);
        stuckAngle = rand.nextFloat() * 3.14F * 2;
        maxSpeedDist = rand.nextInt(200) + almost0;
        maxSpeed = rand.nextInt(200);
        steerLock = rand.nextFloat() * 3.14F + almost0;
        steerSensitivityOffset = rand.nextFloat() * 100F + almost0;
        wheelSensitivityCoeff = rand.nextFloat() * 5;
        absSlip = rand.nextFloat() * 10;
        absRange = rand.nextFloat() * 10 + almost0;
        absMinSpeed = rand.nextFloat() * 10;
        clutchMax = rand.nextFloat() * 2;
        clutchDelta = rand.nextFloat() * 0.5f;
        clutchRange = rand.nextFloat() * 2;
        clutchDeltaTime = rand.nextFloat() * 0.5f;
        clutchDeltaRaced = rand.nextFloat() * 20;
        clutchDec = rand.nextFloat() * 0.5f;
        clutchMaxModifier = rand.nextFloat() * 3;
        clutchMaxTime = rand.nextFloat() * 3;
    }

    public String toString() {
        String str = "";
        str += stuckTime + ",";
        str += stuckAngle + ",";
        str += maxSpeedDist + ",";
        str += maxSpeed + ",";
        str += steerLock + ",";
        str += steerSensitivityOffset + ",";
        str += wheelSensitivityCoeff + ",";
        str += absSlip + ",";
        str += absRange + ",";
        str += absMinSpeed + ",";
        str += clutchMax + ",";
        str += clutchDelta + ",";
        str += clutchRange + ",";
        str += clutchDeltaTime + ",";
        str += clutchDeltaRaced + ",";
        str += clutchDec + ",";
        str += clutchMaxModifier + ",";
        str += clutchMaxTime;

        return str;
    }
}