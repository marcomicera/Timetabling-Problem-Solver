package it.polito.oma.etp.solver;

import java.util.Arrays;
import java.util.LinkedList;

public class TabuList {
	private static int MAX_SIZE = 10;
	
	private LinkedList<Neighbor> tabuList;
	
	public TabuList() {
		tabuList = new LinkedList<Neighbor>();
	}

	public void add(Neighbor neighbor) {
		if(!isFull())
			tabuList.add(neighbor);
		else {
			tabuList.pop();
			tabuList.add(neighbor);
		}
	}
	
	public int find(Neighbor neighbor) {
			return tabuList.indexOf(neighbor);
	}
	
	private boolean isFull() {
		return tabuList.size() == MAX_SIZE;
	}
	
	private boolean isEmpty() {
			return tabuList.size() == 0;
	}
	
	public static int getMAX_SIZE() {
		return MAX_SIZE;
	}

	public static void setMAX_SIZE(int mAX_SIZE) {
		MAX_SIZE = mAX_SIZE;
	}
	
	public String toString() {
		return Arrays.toString(tabuList.toArray());
	}
}
















