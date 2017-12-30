package it.polito.oma.etp.solver;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class TabuList {
	private static int MAX_SIZE = 7;
	
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
	
	public boolean contains(int exam, int t) {
		boolean b = false;
		
		Iterator<Neighbor> i;
		Neighbor n;
		for(i = tabuList.iterator(); i.hasNext();) {
			n = i.next();
			if(n.getMovingExam() == exam && n.getNewTimeslot() == t)
				b = true;
		}
		
		return b;		
	}
	
	public static int getMAX_SIZE() {
		return MAX_SIZE;
	}

	public static void setMAX_SIZE(int MAX_SIZE) {
		TabuList.MAX_SIZE = MAX_SIZE;
	}
	
	public Neighbor getLastEntry() {
		return tabuList.getLast();
	}
	
	@Override
	public String toString() {
		return Arrays.toString(tabuList.toArray());
	}
}

















