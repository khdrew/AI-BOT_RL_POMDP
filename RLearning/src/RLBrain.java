// File created by Andrew Lai klai054 22/10/2017

public class RLBrain {

	private double[][] Q;
	int stateCount;
	
	RLBrain(int stateCount) {
		this.stateCount = stateCount;
		Q = new double[stateCount][stateCount];
		for (int i = 0; i < stateCount; i++) {
			for (int j = 0; j < stateCount; j++) {
				Q[i][j] = 0.0;
			}
		}
	}
	
	// get value of max Q
	public double maxQ(int state){
		double maxValue = -999999999999.9;
		int temp = 0;
		for (int i = 0; i < stateCount; i++) {
			if (Q[state][i] > maxValue) {
				maxValue = Q[state][i];
				temp = i;
			}
		}
		return maxValue;
	}
	
	// get state with max Q
	public int maxQState(int state){
		double maxValue = -999999999999.9;
		int maxState = 0;
		for (int i = 0; i < stateCount; i++) {
			if (Q[state][i] > maxValue) {
				maxValue = Q[state][i];
				maxState = i;
			}
		}
		return maxState;
	}
	
	// Q function
	public double Q(int state, int action){
		return Q[state][action];
	}
	
	// set new Q value
	public void setQ(int state, int action, double value){
		Q[state][action] = value;
	}
}
