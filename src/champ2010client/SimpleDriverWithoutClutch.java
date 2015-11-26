package champ2010client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

public class SimpleDriverWithoutClutch extends Controller{

	/* Stuck constants*/
	final int  stuckTime = 25;
	final float  stuckAngle = (float) 0.523598775; //PI/6

	/* Accel and Brake Constants*/
	final float maxSpeedDist=110;
	final float maxSpeed=250;
	final float sin5 = (float) 0.08716;
	final float cos5 = (float) 0.99619;

	/* Steering constants*/
	final float steerLock=(float) 0.785398;
	final float steerSensitivityOffset=(float) 80.0;
	final float wheelSensitivityCoeff=1;

	/* ABS Filter Constants */
	final float wheelRadius[]={(float) 0.3179,(float) 0.3179,(float) 0.3276,(float) 0.3276};
	final float absSlip=(float) 2.0;
	final float absRange=(float) 3.0;
	final float absMinSpeed=(float) 3.0;

	private int stuck=0;

	private Writer output;
	private Writer input;

	public SimpleDriverWithoutClutch(){
		System.out.println("driver started");
		try {
			output = new BufferedWriter(new FileWriter("output.txt", true));
			input = new BufferedWriter(new FileWriter("input.txt", true));
		}catch ( IOException e){

		}

	}

	public void reset() {
		System.out.println("Restarting the race!");
		
	}

	public void shutdown() {
		try {
			output.close();
			input.close();
		}catch (IOException e){}
		System.out.println("Bye bye!");		
	}
	
	private float getSteer(SensorModel sensors){
		// steering angle is compute by correcting the actual car angle w.r.t. to track 
		// axis [sensors.getAngle()] and to adjust car position w.r.t to middle of track [sensors.getTrackPos()*0.5]
	    float targetAngle=(float) (sensors.getAngleToTrackAxis()-sensors.getTrackPosition()*0.5);
	    // at high speed reduce the steering command to avoid loosing the control
	    if (sensors.getSpeed() > steerSensitivityOffset)
	        return (float) (targetAngle/(steerLock*(sensors.getSpeed()-steerSensitivityOffset)*wheelSensitivityCoeff));
	    else
	        return (targetAngle)/steerLock;

	}
	
	private float getAccel(SensorModel sensors)
	{
	    // checks if car is out of track
	    if (sensors.getTrackPosition() < 1 && sensors.getTrackPosition() > -1)
	    {
	        // reading of sensor at +5 degree w.r.t. car axis
	        float rxSensor=(float) sensors.getTrackEdgeSensors()[10];
	        // reading of sensor parallel to car axis
	        float sensorsensor=(float) sensors.getTrackEdgeSensors()[9];
	        // reading of sensor at -5 degree w.r.t. car axis
	        float sxSensor=(float) sensors.getTrackEdgeSensors()[8];

	        float targetSpeed;

	        // track is straight and enough far from a turn so goes to max speed
	        if (sensorsensor>maxSpeedDist || (sensorsensor>=rxSensor && sensorsensor >= sxSensor))
	            targetSpeed = maxSpeed;
	        else
	        {
	            // approaching a turn on right
	            if(rxSensor>sxSensor)
	            {
	                // computing approximately the "angle" of turn
	                float h = sensorsensor*sin5;
	                float b = rxSensor - sensorsensor*cos5;
	                float sinAngle = b*b/(h*h+b*b);
	                // estimate the target speed depending on turn and on how close it is
	                targetSpeed = maxSpeed*(sensorsensor*sinAngle/maxSpeedDist);
	            }
	            // approaching a turn on left
	            else
	            {
	                // computing approximately the "angle" of turn
	                float h = sensorsensor*sin5;
	                float b = sxSensor - sensorsensor*cos5;
	                float sinAngle = b*b/(h*h+b*b);
	                // estimate the target speed depending on turn and on how close it is
	                targetSpeed = maxSpeed*(sensorsensor*sinAngle/maxSpeedDist);
	            }

	        }

	        // accel/brake command is exponentially scaled w.r.t. the difference between target speed and current one
	        return (float) (2/(1+Math.exp(sensors.getSpeed() - targetSpeed)) - 1);
	    }
	    else
	        return (float) 0.3; // when out of track returns a moderate acceleration command

	}

	public Action control(SensorModel sensors){
//		System.out.println(sensorsToString(sensors));
//		try {
//			input.append(sensorsToString(sensors));
//		}catch (IOException e){}
//		System.out.println("hi");
		// check if car is currently stuck
		if ( Math.abs(sensors.getAngleToTrackAxis()) > stuckAngle )
	    {
			// update stuck counter
	        stuck++;
	    }
	    else
	    {
	    	// if not stuck reset stuck counter
	        stuck = 0;
	    }

		// after car is stuck for a while apply recovering policy
	    if (stuck > stuckTime)
	    {
	    	/* set gear and sterring command assuming car is 
	    	 * pointing in a direction out of track */
	    	
	    	// to bring car parallel to track axis
	        float steer = (float) (- sensors.getAngleToTrackAxis() / steerLock); 
	        int gear=-1; // gear R

	        // if car is pointing in the correct direction revert gear and steer  
	        if (sensors.getAngleToTrackAxis()*sensors.getTrackPosition()>0)
	        {
	            gear = 1;
	            steer = -steer;
	        }
	        // build a CarControl variable and return it
	        Action action = new Action ();
//	        action.gear = gear;
	        action.steering = steer;
	        action.accelerate = -1;
	        action.brake = 1;
//			System.out.println(actionsToString(action));
			try {
				output.append(actionsToString(action));
				input.append(sensorsToString(sensors));
			}catch (IOException e){}
			return action;
	    }

	    else // car is not stuck
	    {
	    	// compute accel/brake command
	        float accel_and_brake = getAccel(sensors);
	        // compute gear 
//	        int gear = getGear(sensors);
	        // compute steering
	        float steer = getSteer(sensors);
	        

	        // normalize steering
	        if (steer < -1)
	            steer = -1;
	        if (steer > 1)
	            steer = 1;
	        
	        // set accel and brake from the joint accel/brake command 
	        float accel,brake;
	        if (accel_and_brake>0)
	        {
	            accel = accel_and_brake;
	            brake = 0;
	        }
	        else
	        {
	            accel = 0;
	            // apply ABS to brake
	            brake = accel_and_brake;
	        }
	        
//	        clutch = clutching(sensors, clutch);
	        
	        // build a CarControl variable and return it
	        Action action = new Action ();
//	        action.gear = gear;
	        action.steering = steer;
	        action.accelerate = accel;
	        action.brake = brake;
//	        action.clutch = clutch;
//			System.out.println(actionsToString(action));
			try {
				output.append(actionsToString(action));
				input.append(sensorsToString(sensors));
			}catch (IOException e){}
			return action;
	    }
	}

	private float filterABS(SensorModel sensors,float brake){
		// convert speed to m/s
		float speed = (float) (sensors.getSpeed() / 3.6);
		// when spedd lower than min speed for abs do nothing
	    if (speed < absMinSpeed)
	        return brake;
	    
	    // compute the speed of wheels in m/s
	    float slip = 0.0f;
	    for (int i = 0; i < 4; i++)
	    {
	        slip += sensors.getWheelSpinVelocity()[i] * wheelRadius[i];
	    }
	    // slip is the difference between actual speed of car and average speed of wheels
	    slip = speed - slip/4.0f;
	    // when slip too high applu ABS
	    if (slip > absSlip)
	    {
	        brake = brake - (slip - absSlip)/absRange;
	    }
	    
	    // check brake is not negative, otherwise set it to zero
	    if (brake<0)
	    	return 0;
	    else
	    	return brake;
	}
	

	public float[] initAngles()	{
		
		float[] angles = new float[19];

		/* set angles as {-90,-75,-60,-45,-30,-20,-15,-10,-5,0,5,10,15,20,30,45,60,75,90} */
		for (int i=0; i<5; i++)
		{
			angles[i]=-90+i*15;
			angles[18-i]=90-i*15;
		}

		for (int i=5; i<9; i++)
		{
				angles[i]=-20+(i-5)*5;
				angles[18-i]=20-(i-5)*5;
		}
		angles[9]=0;
		return angles;
	}

	private String sensorsToString(SensorModel sensors){
		String sensorString = "";
		sensorString += sensors.getSpeed();
		sensorString += ",";
		sensorString += sensors.getAngleToTrackAxis();
		sensorString += ",";
		sensorString += Arrays.toString(sensors.getTrackEdgeSensors());
		sensorString += ",";
		sensorString += Arrays.toString(sensors.getFocusSensors());
		sensorString += ",";
		sensorString += sensors.getTrackPosition();
		sensorString += ",";
		sensorString += sensors.getGear();
		sensorString += "\n";
		return sensorString.replace("[", "").replaceAll("]", "");
	}

	private String actionsToString(Action action){
		String actionString = "";
		actionString += action.accelerate;
		actionString += ",";
		actionString += action.brake;
		actionString += ",";
		actionString += action.steering;
		actionString += ",";
		actionString += action.clutch;
		actionString += ",";
		actionString += action.focus;
		actionString += ",";
		actionString += action.gear;
		actionString += "\n";
		return actionString;

	}

}
