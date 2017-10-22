// File created by Andrew Lai klai054 22/10/2017

package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JPanel;

public class RLWorld extends JPanel implements Runnable, KeyListener{
	
	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 320, HEIGHT = 320;
	public static final int FPS = 30;
	private long targetTime = 1000 / FPS;
	private BufferedImage image;
	private Graphics2D g;
	private Thread thread;
	boolean running;
	
	private Random rand;
	
	// world variables
	int[][] grid;
	int[][] rewards;
	int stateCount;
	int goalState;
	
	// robot variables
	int[] robotPos;
	int[] prevRobotPos;
	
	// print policy variables
	String[] optPolicy;
	double[] policyValues;
	boolean policy;
	int animCount;
	
	RLWorld() {
		super();  // set dimensions
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);
		requestFocus();
		robotPos = new int[]{0,0};
		init();
	}

	public void addNotify() {
		super.addNotify(); // insert thread
		if (thread == null) {
			thread = new Thread(this);
			addKeyListener(this);
			thread.start();
		}
	}	

	private void init(){
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		g = (Graphics2D) image.getGraphics();
		animCount = 0;
		policy = false;
		rand = new Random();
		
		// initiliase world grid
		grid = new int[][] {
			{0,1,0,0},
			{0,0,0,2},
			{0,1,0,0},
			{0,0,0,1}
		};
		
		goalState = 7;
		
		stateCount = grid[0].length * grid.length;
		
		// initiliase rewards for robot to observe
		rewards = new int[stateCount][stateCount];
		// GOAL	
		rewards[getState(2,1)][getState(3,1)] = 100;
		rewards[getState(3,0)][getState(3,1)] = 100;
		rewards[getState(3,2)][getState(3,1)] = 100;
		
		// BLACK HOLES Unsafe Zones
		rewards[getState(0,0)][getState(1,0)] = -100;
		rewards[getState(1,1)][getState(1,0)] = -100;
		rewards[getState(2,0)][getState(1,0)] = -100;
		
		rewards[getState(1,1)][getState(1,2)] = -100;
		rewards[getState(1,3)][getState(1,2)] = -100;
		rewards[getState(0,2)][getState(1,2)] = -100;
		rewards[getState(2,2)][getState(1,2)] = -100;
		
		rewards[getState(3,2)][getState(3,3)] = -100;
		rewards[getState(2,3)][getState(3,3)] = -100;
				
		running = true;
	}
	
	public void run() {
		long start, elapsed, wait;

		while (running) {

			start = System.nanoTime();
			
			update();
			draw();
			drawToScreen();
			
			elapsed = System.nanoTime() - start;

			wait = targetTime - elapsed / 1000000;
			
			
			if (wait < 0) wait = 5;
			
			try {
				Thread.sleep(wait);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void drawToScreen() {
		Graphics g2 = getGraphics();
		g2.drawImage(image, 0, 0, WIDTH, HEIGHT, null);
		g2.dispose();
	}

	private void draw() {
		// draw back ground
		g.setColor(Color.WHITE);
		g.fillRect(0,0,WIDTH,HEIGHT);
		
		String s = "";
		// grid
		for (int i = 0; i < grid.length; i++) { // y
			for (int j = 0; j < grid[i].length; j++) { // x
				if (grid[i][j] == 1) { // draw unsafe zone
					g.setColor(Color.BLACK);
					g.fillRect(j * WIDTH/grid[i].length, i * HEIGHT/grid.length, WIDTH/grid[i].length, HEIGHT/grid.length);
					g.setColor(Color.WHITE);
					s = "-100";
				}else if (grid[i][j] == 2) { // draw goal zone
					g.setColor(Color.GREEN);
					g.fillRect(j * WIDTH/grid[i].length, i * HEIGHT/grid.length, WIDTH/grid[i].length, HEIGHT/grid.length);
					g.setColor(Color.BLACK);
					s = "100";
				}else { // normal zone
					g.setColor(Color.BLACK);
					s = "0";
				}
				
				// Show policy and Q values
				if (policy){
					switch (optPolicy[getState(j,i)]){
						case "up":
							s = " ^";
							break;
						case "down":
							s = " v";
							break;
						case "left":
							s = " <";
							break;
						case "right":
							s = " >";
							break;
						default:
							s = " x";
							break;
					}
					s += " " + String.format("%.02f", policyValues[getState(j,i)]);
				}
				
				// draw gride lines
				g.drawString(s, j * WIDTH/grid[i].length, i * HEIGHT/grid.length + HEIGHT/grid.length);
				g.setColor(Color.BLACK);
				g.drawRect(j * WIDTH/grid[i].length, i * HEIGHT/grid.length, WIDTH/grid[i].length, HEIGHT/grid.length);
			}
		}
		
		// draw robot
		g.setColor(Color.YELLOW);
		g.fillOval((int)(robotPos[0] * WIDTH/grid[0].length + WIDTH/grid[0].length * 0.05),
				   (int)(robotPos[1] * HEIGHT/grid.length + HEIGHT/grid.length * 0.05),
				   (int)(WIDTH/grid[0].length * 0.9), (int)(HEIGHT/grid.length * 0.9));
		g.setColor(Color.BLACK);
		g.drawOval((int)(robotPos[0] * WIDTH/grid[0].length + WIDTH/grid[0].length * 0.05),
				   (int)(robotPos[1] * HEIGHT/grid.length + HEIGHT/grid.length * 0.05),
				   (int)(WIDTH/grid[0].length * 0.9), (int)(HEIGHT/grid.length * 0.9));
		
		
	}
	
	// convert single integer pos form into x y pos form 
	public int[] getXY(int state){
		int x = state % grid.length;
		int y = (state - x)/4;
		int[] out = new int[]{x,y};
		return out;
	}
	
	// convert x y pos form into single integer form
	public int getState(int x, int y) {
		return y * 4 + x;
	}
	
	// get current pos in single integer form
	public int currentState(){
		return robotPos[0] + robotPos[1] * 4;
	}
	
	// spawn robot in random location
	public void randRobotPos(){
		boolean plausible = false;
		rand = new Random();
		while (!plausible) {
			robotPos[0] = rand.nextInt(grid[0].length);
			robotPos[1] = rand.nextInt(grid.length);
			if (checkPlausible(robotPos[0],robotPos[1])) {
				plausible = true;
			}
		}
	}
	
	// check if spawned in unsafe zone
	public boolean checkPlausible(int x, int y){
		try{
			if (grid[y][x] == 1) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
	}
	
	// random action selection strategy for robot
	public int doRandomAction(){
		prevRobotPos = robotPos;
		while (true){
			int action = rand.nextInt(4);
			if (action == 0){ // up
				if (robotPos[1] != 0) {
					robotPos[1] -= 1;
					return getState(robotPos[0],robotPos[1]);
				}
			} else if (action == 1){ // down
				if (robotPos[1] != grid.length - 1) {
					robotPos[1] += 1;
					return getState(robotPos[0],robotPos[1]+1);
				}
			} else if (action == 2){ // left
				if (robotPos[0] != 0) {
					robotPos[0] -= 1;
					return getState(robotPos[0]-1,robotPos[1]);
				}
			} else { // right
				if (robotPos[0] != grid[0].length - 1) {
					robotPos[0] += 1;
					return getState(robotPos[0]+1,robotPos[1]);
				}
			}
		}
	}
	
	public void undoAction(){
		robotPos = prevRobotPos;
	}
	
	// reward function
	public int R(int state, int action){
		return rewards[state][action];
	}
	
	public void showPolicy(RLBrain brain) {
		// get policy from RL brain
		optPolicy = new String[stateCount];
		policyValues = new double[stateCount];
		for (int i = 0; i < stateCount; i++) {
			
			int[] thisStateXY = getXY(i);
			int targetState = brain.maxQState(i);
			int[] targetStateXY = getXY(targetState);
			optPolicy[i] = Integer.toString(targetState);
			
			// compute policy actions
			if (thisStateXY[1] > targetStateXY[1]) { // up
				optPolicy[i] = "up";
			} else if (thisStateXY[1] < targetStateXY[1]) { // down
				optPolicy[i] = "down";
			} else if (thisStateXY[0] > targetStateXY[0]) { // left 
				optPolicy[i] = "left";
			} else if (thisStateXY[0] < targetStateXY[0]) { // right
				optPolicy[i] = "right";
			}
			policyValues[i] = brain.maxQ(i);
		}
		
		policy = true;		
	}
	
	// animate policy update function
	public void update(){
		if (!policy) return;
		
		// run each 10 counts
		animCount++;
		if (animCount < 10) {
			return;
		}
		animCount = 0;
		
		// robot commands
		if (grid[robotPos[1]][robotPos[0]] == 2) { // goal reached
			randRobotPos();
			
		}	else { // move robot
			switch (optPolicy[getState(robotPos[0],robotPos[1])]){
				case "up":
					robotPos[1] -= 1;
					break;
				case "down":
					robotPos[1] += 1;
					break;
				case "left":
					robotPos[0] -= 1;
					break;
				case "right":
					robotPos[0] += 1;
					break;
				default:
					randRobotPos();
					break;
			}
		}		
	}
	
	// GUI escape!
	public void keyPressed(KeyEvent k) {
		if (k.getKeyCode() == KeyEvent.VK_ESCAPE){
			System.exit(0);
		}
	}
	public void keyReleased(KeyEvent k) {}
	public void keyTyped(KeyEvent k) {}
	
}
