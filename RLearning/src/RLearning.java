// File created by Andrew Lai klai054 22/10/2017

import java.util.Random;
import java.util.Scanner;

import javax.swing.JFrame;

public class RLearning {

	public static void main(String[] args) {
		
		Random rand = new Random();
		
		// Learn rate and discount factor max number of episodes
		final double alpha = 0.1;
		final double gamma = 0.9;
		final int maxEpisodes = 300;
		
		// for your viewing pleasure change to true.
		final boolean watchLearning = false;
				
		// Declare and render the world
		RLWorld world = new RLWorld();
		JFrame window = new JFrame("RLearning GUI");
		window.setContentPane(world);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.pack();
		window.setVisible(true);
		
		// Declare RL bot
		RLBrain brain = new RLBrain(world.stateCount);
		
		Scanner s = new Scanner(System.in);
		System.out.println("Press enter to start learning process...");
		s.nextLine();
		
		// QLearning or SARSA	
		final boolean qLearning = true;
		
		// run each episode of RL
		for (int episodes = 0; episodes < maxEpisodes; episodes++) {
			// initialise random location for robot
			world.randRobotPos();
			
			int state = world.currentState();
			if (qLearning) { // QLEARNING
				// terminate episode at goal or unsafe location
				while (state != world.goalState && world.checkPlausible(world.getXY(state)[0], world.getXY(state)[1])) {
					
					// random action selection strategy
					world.doRandomAction();
					int action = world.currentState();
					
					// next state
					int rate = rand.nextInt(10);
					int nextState;
					if (rate < 8) { // 80% success rate of action
						nextState = world.currentState();
					} else { // failed action
						world.undoAction();
						nextState = world.currentState();
					}			
					
					// compute Q value
					double q = brain.Q(state, action);
					double maxQ = brain.maxQ(nextState);
					int r = world.R(state, action);
					double value = q + alpha * (r + gamma * maxQ - q);
					brain.setQ(state, action, value);
					
					// set robot state
					state = nextState;
	
					if (watchLearning) {
						try{
							Thread.sleep(1);
						}catch(Exception e){}
					}
				}
			} else { // SARSA
				world.doRandomAction();
				int action = world.currentState();
				int prevAction = 0;
				world.undoAction();
				// terminate episode at goal or unsafe location
				while (state != world.goalState && world.checkPlausible(world.getXY(state)[0], world.getXY(state)[1])) {
					
					// random action selection strategy
					int newState;
					int rate = rand.nextInt(10);
					if (rate < 8) { // 80% success rate of action
						newState = action;
					} else { // failed action
						world.undoAction();
						newState = world.currentState();
					}			
					
					// rewards
					int r = world.RState(newState);
					
					// random action selection strategy
					world.doRandomAction();	
					int newAction = world.currentState();
					world.undoAction();
									
					
					// compute Q value
					double q = brain.Q(state, prevAction);
					double nextQ = brain.Q(newState, newAction);
					
					double value = q + alpha * (r + gamma * nextQ - q);
					brain.setQ(state, action, value);
					
					// set robot state
					state = newState;
					action = newAction;
					prevAction = action;
					if (watchLearning) {
						try{
							Thread.sleep(1);
						}catch(Exception e){}
					}
				}
			}
		}
		
		System.out.println("Episodes over. Showing policies.");
		System.out.println("Press ESC key on GUI to exit...");
		// display and animate policy obtained from RL
		world.showPolicy(brain);		

	}

}
