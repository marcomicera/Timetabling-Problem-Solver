package it.polito.oma.etp.solver;

import it.polito.oma.etp.reader.InputReader;
import it.polito.oma.etp.reader.InstanceData;

public class ETPsolver_OMAAL_group15 {

	public static String instanceName;
	public static InstanceData idata;
	
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println(
				"Usage: java it.polito.oma.etp.solver.ETPsolver_OMAAL_group15 instance_number"
			);
			System.exit(1);
		}
		
		instanceName = args[0];
		
		idata = InputReader.getData("res\\" + instanceName);
		
		// *********************** Testing code ***********************
		// TODO delete
		/*InstanceData testInstance = new InstanceData(
			"test instance",
			40,	// S
			5,	// E
			4, 	// Tmax
			new int[][]{ // N
				{0,		5,	15,	0,	5},
				{5,		0,	0,	5,	25},
				{15,	0,	0,	10,	0},
				{0,		5,	10,	0,	25},
				{5,		25,	0,	25,	0}
			}
		);*/
		
		for(int i = 0; i < idata.getE(); i++) {
			for(int j = 0; j < idata.getE(); j++) {
				System.out.print(idata.getN()[i][j] + "\t");
			}
			System.out.println("");
		}
		
		TabuSearch.solve(idata);
	}
}
