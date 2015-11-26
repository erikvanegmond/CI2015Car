package champ2010client;

import com.sun.deploy.util.ArrayUtil;
import org.neuroph.core.NeuralNetwork;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class NeuronDriver extends Controller{

	double[] inputMin = null;
	double[] inputMax = null;
	double[] outputMin = null;
	double[] outputMax = null;

	NeuralNetwork loadedMlPerceptron;

	public NeuronDriver(){
		//TODO: read file once in the beginning instead of for every row.
		File normalizedFile = new File("C:\\Users\\Erik\\IdeaProjects\\Neuralnetworks\\normalization.txt");
		BufferedReader reader = null;
		String inputMinText = null;
		String inputMaxText = null;
		String outputMinText = null;
		String outputMaxText = null;



		try {
			reader = new BufferedReader(new FileReader(normalizedFile));
			inputMinText  = reader.readLine();
			inputMaxText  = reader.readLine();
			outputMinText = reader.readLine();
			outputMaxText = reader.readLine();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
			}
		}

		String[] dataFeatures = inputMinText.replace("[", "").replace("]", "").split(",");
		inputMin = new double[dataFeatures.length];
		for(int j = 0; j<dataFeatures.length; j++){
			inputMin[j] = Double.parseDouble(dataFeatures[j]);
		}

		dataFeatures = inputMaxText.replace("[","").replace("]","").split(",");
		inputMax = new double[dataFeatures.length];
		for(int j = 0; j<dataFeatures.length; j++){
			inputMax[j] = Double.parseDouble(dataFeatures[j]);
		}

		dataFeatures = outputMinText.replace("[", "").replace("]", "").split(",");
		outputMin = new double[dataFeatures.length];
		for(int j = 0; j<dataFeatures.length; j++){
			outputMin[j] = Double.parseDouble(dataFeatures[j]);
		}

		dataFeatures = outputMaxText.replace("[","").replace("]","").split(",");
		outputMax = new double[dataFeatures.length];
		for(int j = 0; j<dataFeatures.length; j++){
			outputMax[j] = Double.parseDouble(dataFeatures[j]);
		}

		loadedMlPerceptron = NeuralNetwork.createFromFile("C:\\Users\\Erik\\IdeaProjects\\Neuralnetworks\\fullRace.nnet");

		System.out.println("driver started");
	}

	public void reset() {
		System.out.println("Restarting the race!");
		
	}

	public void shutdown() {
		System.out.println("Bye bye!");		
	}
	



	public Action control(SensorModel sensors){
		double[] sensorList = sensorsList(sensors);
		sensorList = normalizeInput(sensorList);
//		System.out.print(Arrays.toString(sensorList));
		loadedMlPerceptron.setInput(sensorList);
		loadedMlPerceptron.calculate();
		double[ ] networkOutput = loadedMlPerceptron.getOutput();
		networkOutput = denormalizeOutput(networkOutput);
		System.out.println(Arrays.toString(networkOutput));
		return listToAction(networkOutput);

	}

	private double[] normalizeInput(double[] input){
		double[] normalizeInput = new double[input.length];

		for(int i = 0; i<input.length; i++){
			normalizeInput[i] = (input[i]-inputMin[i])/(inputMax[i] - inputMin[i]);
			if(Double.isNaN(normalizeInput[i])){
				normalizeInput[i] = 0;
			}
		}
		return normalizeInput;
	}
	private double[] denormalizeOutput(double[] output){
		double[] denormalizeOutput = new double[output.length];

		for(int i = 0; i<output.length; i++){
			denormalizeOutput[i] = (output[i]*(outputMax[i] - outputMin[i])) + outputMin[i];
			if(Double.isNaN(denormalizeOutput[i])){
				denormalizeOutput[i] = 0;
			}
		}
		return denormalizeOutput;
	}

	private double[] sensorsList(SensorModel sensors){
		List<Double> list = new ArrayList<Double>();
		list.add(sensors.getSpeed());
		list.add(sensors.getAngleToTrackAxis());
		list.add(sensors.getTrackEdgeSensors()[10]);
		list.add(sensors.getTrackEdgeSensors()[9]);
		list.add(sensors.getTrackEdgeSensors()[8]);
		list.add(sensors.getTrackPosition());
		list.add((Double.parseDouble((""+sensors.getGear()))));//WTF???

		double[] returnList = new double[list.size()];

		for(int i = 0; i<list.size(); i++){
			returnList[i] = list.get(i).doubleValue();
		}

		return returnList;
	}

	private Action listToAction(double[] list){
		Action action = new Action();
		action.accelerate = list[0];
		action.brake = list[1];
		action.steering = list[2];
		action.clutch = list[3];
		action.gear = (int)Math.round(list[4]);
		return action;
	}


}
