package it.polito.oma.etp.solver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import it.polito.oma.etp.reader.InstanceData;

public abstract class TabuSearch {
	protected InstanceData instance;	
	protected Solution currentSolution;
	protected Solution bestSolution;
	protected int iteration = 0;
	protected TabuList tabuList;
	
	public TabuSearch(InstanceData instanceData) {
		this.instance = instanceData;
		tabuList = new TabuList();
	}
	
	/**
	 * Starts the Tabu Search algorithm starting from the given initial
	 * solution, using the given instance data.
	 * @param instanceData		data describing the problem instance.
	 * @param initialSolution	initial solution from which the Tabu Search
	 * 							algorithm starts.
	 */
	public void solve() {
		int iteration = 0;
		
		//while(true) {
		/*TODO debug*/while(bestSolution.getFitness() > 0) {
			int nextPairIndex = 0;
			
			/*TODO debug*/System.out.println("\n***** Iteration " + iteration + " *****");
			
			Neighbor validNeighbor = null;
			
			// No valid neighbor in the neighborhood
			while(validNeighbor == null) {
				try {
					/*TODO debug*/System.out.println("nextPairIndex: " + nextPairIndex);
					ExamPair neighborhoodGeneratingPair = getNextPair(nextPairIndex);
					ArrayList<Neighbor> neighborhood = getNeighborhood(neighborhoodGeneratingPair);
					
					/*TODO debug*/System.out.println("Penalizing pairs: " + currentSolution.getPenalizingPairs());
					/*TODO debug*/System.out.println("Neighborhood: " + neighborhood);
					
					validNeighbor = selectBestValidNeighbor(neighborhood);
					++nextPairIndex;
				} catch(IndexOutOfBoundsException e) {
					// TODO what happens here? (empty Tabu List?)
					// There's no valid neighborhood for every exam pair
				}
			}
			
			if(validNeighbor != null)
				move(validNeighbor);
			
			/*TODO debug*/System.out.println("\n");
			
			++iteration;
			
//			try {
//				Thread.sleep(30);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		//TODO remember to output solution on file 
	}
	
	/**
	 * Given a penalizing pair of exams, returns the corresponding neighborhood
	 * (i.e., the set of all possible feasible moves.) ordered by ascending fitness value. 
	 * @param examPair	exam pair to be rescheduled.
	 * @return			A list of Neighbor objects ordered by ascending fitnss value containing:
	 * 					1)	the exam that should be rescheduled
	 * 					2)	the new timeslot in which the exam should be rescheduled in
	 * 					3)	the corresponding fitness value
	 * 					Return null if no feasible neighbor is found.
	 */
	private ArrayList<Neighbor> getNeighborhood(ExamPair examPair) {
		// Retrieving the timeslots in which the two exams have been scheduled
		int firstExamSlot = currentSolution.getTimeslot(examPair.getExam1());
		int secondExamSlot = currentSolution.getTimeslot(examPair.getExam2());
		/*TODO debug*/ //System.out.println("firstExamSlot = " + firstExamSlot + "\nsecondExamSlot = " + secondExamSlot);
		
		// Computing support variables for excluding timeslots between the two exams 
		int lowestIndex = (firstExamSlot < secondExamSlot) ? firstExamSlot : secondExamSlot;
		int highestIndex = (firstExamSlot < secondExamSlot) ? secondExamSlot : firstExamSlot;
		/*TODO debug*/ //System.out.println("lowestIndex = " + lowestIndex + "\nhighestIndex = " + highestIndex);
		
		// Neighborhood corresponding to both exams
		ArrayList<Neighbor> neighborhood = new ArrayList<Neighbor>();
		
		// For all possible timeslots
		for(int newTimeslot = 0; newTimeslot < instance.getTmax(); ++newTimeslot) {
			if(	/*	Skipping timeslots between the two exams: these positions will
					increase the fitness value for sure */
				newTimeslot < lowestIndex || newTimeslot > highestIndex
			) {
				try {
					/**
					 *  Retrieving neighbor objects, containing its fitness and its corresponding schedule
					 *  and adding them to the neighborhood set
					 */
					neighborhood.add(currentSolution.getNeighbor(examPair.getExam1(), newTimeslot));
					neighborhood.add(currentSolution.getNeighbor(examPair.getExam2(), newTimeslot));
				} catch(InvalidMoveException e) {
					continue;
				}
			}
		}
		
		if(!neighborhood.isEmpty())
			// Ordering the neighborhood by increasing fitness value
			Collections.sort(neighborhood);
		
		return neighborhood;
	}
	
	/**
	 * Determines whether all neighbors belonging to the neighborhood passed
	 * as argument (ordered by ascending fitness value) can be used for the 
	 * next move or not. 
	 * @param neighborhood	the neighborhood (ordered by ascending fitness value) 
	 * 						to be checked with respected to the Tabu list.
	 * @return				the first acceptable neighbor (that will be the best
	 * 						one: this assumes that the neighborhood is already
	 * 						ordered by ascending fitness value) that can be used 
	 * 						for the next move according to the Tabu list or
	 * 						null if no valid neighbor was found.
	 */
	private Neighbor selectBestValidNeighbor(ArrayList<Neighbor> neighborhood) {
		if(neighborhood == null || neighborhood.isEmpty())
			return null;
		
		/*TODO debug*/System.out.println("Tabu List: " + tabuList);
		
		Neighbor validNeighbor = null;
		for(Neighbor neighbor: neighborhood) {
			
			// This move is in the Tabu List
			if(tabuList.find(neighbor) != -1)
				/*TODO debug*/System.out.println("Neighbor " + neighbor + " has been found in the Tabu List");
				
				// Aspiration criteria satisfied
				if(neighbor.getFitness() < bestSolution.getFitness()) {
					validNeighbor = neighbor;
					break;
				}
			// This move is not in the Tabu List
			else {
				validNeighbor = neighbor;
				break;
			}
		}
			
		return validNeighbor;
	}
	
	/**
	 * Returns the next exam pair belonging to the penalizingPairs list.
	 * This is used when an exam pair has an empty valid neighborhood, hence
	 * requiring searching for another neighborhood.
	 * @param nextPairIndex					Next exam pair index to be retrieved.		
	 * @return								The exam pair that will be used to generate
	 * 										another neighborhood to be explored by the Tabu
	 * 										Search algorithm.
	 * @throws IndexOutOfBoundsException	If the exam pair index is out of bound, meaning
	 * 										that there are no more exam pairs having a
	 * 										valid non-empty neighborhood.
	 */
	protected abstract ExamPair getNextPair(int nextPairIndex) throws IndexOutOfBoundsException;
	
	/**
	 * 
	 * @param neighbor
	 */
	private void move(Neighbor neighbor) {
		/*TODO debug*/System.out.println("Move: <e" + neighbor.getMovingExam() + ", from t" + currentSolution.getTimeslot(neighbor.getMovingExam()) + " to t" + neighbor.getNewTimeslot() + ">");
		
		// Retrieving current solution's infos before performing the move
		int movingExam = neighbor.getMovingExam();
		int oldTimeslot = currentSolution.getTimeslot(movingExam);
		/*TODO debug*/ //System.out.println("movingExam's index inside move(): " + movingExam);
		/*TODO debug*/ //System.out.println("old fitness = " + currentSolution.getFitness());
		
		// Inserting this move in the Tabu List
		tabuList.add(new Neighbor(movingExam, oldTimeslot));
		
		/*TODO debug*/System.out.print("Tabu list: "+ tabuList);
		
		updateSolution(movingExam, oldTimeslot, neighbor);
	}
	
	/**
	 * update the current solution with the chosen move
	 * @param int						the exam to be moved
	 * @param oldTimeSlod				the time-slot where the exam is moved
	 * @param neighbor					the neighbor chosen by the algorithm
	 */
	private void updateSolution(int movingExam, int oldTimeslot, Neighbor neighbor) {		
		// Updating the current solution
		currentSolution.updateTe(movingExam, oldTimeslot, neighbor.getNewTimeslot());
		currentSolution.updateSchedule(neighbor); 
		currentSolution.setFitness(neighbor.getFitness());
		currentSolution.updatePenalizingPairs(neighbor);
		
		if(currentSolution instanceof OptimizationSolution)
			((OptimizationSolution)currentSolution).initializeDistanceMatrix();
				
		/*TODO debug*/ //System.out.println("oldTimeslot = " + oldTimeslot);
		/*TODO debug*/currentSolution.initializeFitness();
		/*TODO debug*/System.out.println("\nFitness: " + currentSolution.getFitness() + "\nCalculating the fitness from scratch: " + currentSolution.getFitness());

		// Updating bestSolution if necessary
		updateBestSolution();
	}
	
	/**
	 * Updates the best solution with the current one
	 * if necessary
	 */
	protected abstract void updateBestSolution();
	
	/**
	 * Prints the solution into a file.
	 */
	private void printSolution(int te[][]) {
		try
	     {
	          FileOutputStream bestSolution = new FileOutputStream("bestSolution.txt");
	          PrintStream write = new PrintStream(bestSolution);
	          
	          for(int j = 0; j < instance.getE()+1; ++j) {
	        	  if(j == 0)
	        		  write.print("\t");
	  			  write.print("E" + j + "\t"); 
	  		  }
	          
	          write.println();
	          
	          for(int i = 0; i < instance.getTmax(); ++i) {
	        	  	write.print("T" + (i+1) + "\t");
					for(int j = 0; j < instance.getE(); ++j) {
						write.print(te[i][j] + "\t"); 
					}
					write.println();
	          }
	      }
	      catch (IOException e)
	      {
	          System.out.println("Errore: " + e);
	          System.exit(1);
	      }
	}
	
	/**
	 * Returns the proper solution corresponding to this
	 * Tabu Search implementation.
	 * @return	the Tabu Search solution.
	 */
	public Solution getSolution() {
		return bestSolution;
	}
}