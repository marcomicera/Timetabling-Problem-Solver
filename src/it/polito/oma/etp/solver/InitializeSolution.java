package it.polito.oma.etp.solver;

import it.polito.oma.etp.reader.InstanceData;

public class InitializeSolution {
	
	private static InstanceData idata;
	/** Boolean matrix whose elements ij are set to 1 if exam i and exam j
	 * are conflictual and in the same timeslot */
	private static int[][] U;
	
	/**
	 * Given a set of data relative to an instance of the problem, it returns back a first
	 * rough feasible solution obtained using the Tabu Search algorithm.
	 * @param instanceData instance of the problem
	 * @return A feasible solution of the instance in the form of the matrix TE
	 */
	public static int[][] getFeasibleSolution(InstanceData instanceData){
		idata = instanceData;
		int E = idata.getE();
		U = new int[E][E];
		int[][] te = generateUnfeasibleTE();
		//TODO make te feasible.
		return te;
	}
	
	/**
	 * An exam will be placed in the timeslot having the most non-conflictual exams in it. So there is a call
	 * to a function that calculates which is the fullest timeslot. When an exam will not be able to find 
	 * a timeslot, an unfeasibility is introduced and it is assigned to the timeslot with less conflictual
	 * exams in it. The field U is updated accordingly.
	 * @return An unfeasible solution for the given instance (having conflictual exams
	 * in the same timeslot).
	 */
	private static int [][] generateUnfeasibleTE(){
						
		int examnumber = idata.getE();
		int N[][] = idata.getN();
		int tmax = idata.getTmax();
		int te[][] = new int [tmax][examnumber];
		// array that counts how much exams are assigned in every timeslot
		int texamsCounter[] = new int[tmax];
		// array that stores which is the timeslot to visit first, according to its assigned exams
		int timeslotOrder[] = new int[tmax];
		// boolean array that tells me if a given exam was already assigned in a timeslot.
		int assignedExams[] = new int[examnumber];

		// first step: put e0 in t0
		te[0][0] = 1;
		assignedExams[0] = 1;
		texamsCounter[0]++;
		
		// cycling through all exams
		for(int exam = 1; exam < examnumber; exam++) {
			
			/* IN: texamsCounter, OUT: timeslotOrder*
			 * Given in input the number of exams in every timeslot, it generates the timeslot ordering. */
			timeslotOrder = getTimeslotOrder(texamsCounter);
			
			// cycling through all timeslots
			for(int t = 0; t < tmax; t++) {
				
				// Check if this exam was already assigned
				if (assignedExams[exam] == 1)
					break;
					
				// checking exams in conflict
				boolean conflict = false;
				
				for(int conflictualExam = 0; conflictualExam < examnumber; conflictualExam++) {
					// are exam and conflictualExam in conflict? If not, search next exam for a conflict.
					if (N[exam][conflictualExam] != 0) {
						/* Here we know exam and conflictualExam are in conflict. I'd like to put exam in
						 * timeslot, but first I check if conflictualExam is already in timeslot.
						 * If it is there, I need to change timeslot, otherwise i look the next conflictualExam. */
						if(te[timeslotOrder[t]][conflictualExam] == 1) {
							conflict = true;
							break;
						}						
					}
				} // END FOR conflictualExam
				
				/* We are here for 2 motivations:
				 * 1. we checked all conflictualExams and no one is in timeslot (conflict = false) -> write in timeslot.
				 * 2. we found that a conflictualExam is in timeslot (conflict = true) -> look next timeslot */
				if(conflict == false) {
					te[timeslotOrder[t]][exam] = 1;
					// This exam is assigned, do not assign it again.
					assignedExams[exam] = 1;
					texamsCounter[timeslotOrder[t]]++;
				}		
			} // END FOR timeslot (increasing slots)
					
			/* If at the end of the timeslots checking, the exam can't still be assigned, we introuduce infeasibility.
			 * We assigne exam to the timeslot with less conflictual exams and update U accordingly. */
			if (assignedExams[exam] == 0) {
				/*TODO debug*/System.out.println("This exam couldn't find a timeslot: e" + exam /*+ " - " + ++failCounter*/);
				
				// number of conflicts for each timeslot
				int[] numberOfConflicts = new int[tmax];
				
				// cycling timeslots, counting the number of conflicts
				for(int t = 0; t < tmax; t++) {
					numberOfConflicts[t] = 0;
					
					for(int confExam = 0; confExam < examnumber; confExam++) {
						
						// are exam and confExam in conflict?
						if(N[exam][confExam] != 0) {
							// yes; is confExam in timeslot t? Increase the number of conflicts for that timeslot
							if(te[t][confExam] == 1) {
								numberOfConflicts[t]++;
							}		
						}
					}// end FOR confExam
					
				}// end FOR timeslots
				
				int minConflicts = examnumber;
				int myTimeslot = 0;
				// searching for the timeslot with the minimum of conflict for the given exam
				for(int t = 0; t < numberOfConflicts.length; t++) {
					if(numberOfConflicts[t] < minConflicts) {
						minConflicts = numberOfConflicts[t];
						myTimeslot = t;
					}
				}
				/*TODO debug*/System.out.println("The timeslot with less conflicts for exam e" + exam + " is t" + myTimeslot
												 + " with " + minConflicts + " conflictual exams. \n");
				
				/* Now the exam is placed in the timeslot myTimeslot and the relative U elements are set at 1, remembering it is
				 * introducing an unfeasibility in the solution.*/
				te[myTimeslot][exam] = 1;
				assignedExams[exam] = 1;
				texamsCounter[myTimeslot]++;
				// cycling through exams in myTimeslot
				for(int e = 0; e < examnumber; e++) {
					// looks only allocated exams
					if(te[myTimeslot][e] == 1) {
						// and check if they are conflictual with exam
						if(N[exam][e] != 0) {
							U[exam][e] = 1;
							U[e][exam] = 1;
						}
					}
				}
				
			}// end IF exam cannot be placed
			
		} // END FOR exam
		
		/*TODO debug*/System.out.println("Unfeasibility matrix:");
		for(int i = 0; i < examnumber; i++) {
			for(int j = i + 1; j < examnumber; j++) {
				if(U[i][j] == 1)
					System.out.println("Unfeasibility between exam e" + i + " and e" + j);
			}	
		}
		System.out.println();
		
		return te;
	}
	
	/**
	 * Given an array containing the occurrencies of exams for every timeslot (index) it calculates 
	 * the timeslot with most exams. Then returns an array with an increasing number of exams in the
	 * following format:
	 * 3, 6, 12, 5, 1, 0 ...
	 * This means the algorithm will visit in order t3, t6, t12, --- 
	 * @param TEcounter
	 * @return
	 */
	private static int[] getTimeslotOrder(int[] TEcounter) {
		
		int tec[] = TEcounter.clone();
		int orderTec[] = new int[idata.getTmax()];
		int max = -1;
		int ind = 0;
		
		// For every element of the Timeslot Order array
		for(int x = 0; x < idata.getTmax(); x++) {			
			// For every elemnt of the Exams in timeslot counter, find the max
			for(int i = 0; i < idata.getTmax(); i++) {
				
				if(tec[i] > max) { 
					max = tec[i];
					ind = i;
				}
			}
			max = -1;
			tec[ind] = -1;
			orderTec[x] = ind;
			
		}
		return orderTec;
	}

}
