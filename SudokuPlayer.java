/*
 * Yanfeng Jin (Tony) and Uriel Ulloa - HW2
 * All group members were present and contributing to during all work on this project.
 * We have neither given nor received any unauthorized aid in this project. 
 
 *AC-3 implementation was coded by us while the rest of the code including the UI code was provided to us by professor Ananya Christman.
 */

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;

public class SudokuPlayer implements Runnable, ActionListener {

    // final values must be assigned in vals[][]
    int[][] vals = new int[9][9];
    Board board = null;



    /// --- AC-3 Constraint Satisfication --- ///
   
    
    // Useful but not required Data-Structures;
    ArrayList<Integer>[] globalDomains = new ArrayList[81];
    ArrayList<Integer>[] neighbors = new ArrayList[81];
    Queue<Arc> globalQueue = new LinkedList<Arc>();
    
    LinkedHashMap<Integer,ArrayList<Integer>> sortedCells = new LinkedHashMap<Integer,ArrayList<Integer>>();
        

	/*
 	* This method sets up the data structures and the initial global constraints
 	* (by calling allDiff()) and makes the initial call to backtrack().
 	*/

    private final void init(){
        //Do NOT remove these 3 lines (required for the GUI)
        board.Clear();
        ops = 0;
        recursions = 0;


        /**
         *  YOUR CODE HERE:
         *  Create Data structures ( or populate the ones defined above ).
         *  These will be the data structures necessary for AC-3.
         **/
        
        // Set up global domain
        for (int i = 0; i < 81; i++) {
        	int row = i/9;
        	int col = i%9;
        	ArrayList<Integer> eachDomain = new ArrayList<Integer>();
        	// If the cell is empty, then the domain is 1 to 9
        	if (vals[row][col] == 0) {
        		for (int domainNum = 1; domainNum < 10; domainNum++) {
        			eachDomain.add(domainNum);
        		}
        	}
        	// If the cell is already filled, then the domain is just that number in the cell.
        	else {
        		eachDomain.add(vals[row][col]);
        	}
        	globalDomains[i] = eachDomain;
        }
        
        allDiff();
        
         // Initial call to backtrack() on cell 0 (top left)
        boolean success = backtrack(0,globalDomains);
        
        // Update the board value when successful
        for (int i=0; i<81;i++) {
        	int row = i/9;
        	int col = i%9;
        	vals[row][col] = globalDomains[i].get(0);
        }

        // Prints evaluation of run
        Finished(success);

    }

    

    // We are doing everything in one function. 
    private final void allDiff(){
        
    	// Set up neighbors
    	for (int i=0; i<81; i++) {
    		neighbors[i] = new ArrayList<Integer>();
    		int row = i/9;
    		int col = i%9;
    		
    		// Add every cell in the same row to its neighbors
    		// rowFirst is the number in the first column of the row
    		int rowFirst = row*9;
    		// The number in the last column of the row
    		int rowLast = (row+1)*9-1;
    		// cellNum denotes the number inside the cell
    		for (int cellNum = rowFirst; cellNum <= rowLast; cellNum++) {
    			// If it is not the cell itself, we add it to the cell's neighbors
    			if (cellNum != i) {
    				neighbors[i].add(cellNum);
    			}
    		}
    		
    		
    		// Add every cell in the same column to its neighbors
    		// For each row
    		for (int rowNum = 0; rowNum < 9; rowNum++) {
    			// If it is not the cell itself, we add it to the cell's neighbors
    			if (rowNum != row) {
    				neighbors[i].add(col+9*rowNum);
    			}
    		}
    		
    		
    		// Add every cell in the same box to the neighbor
    		// Calculate the box the current number is in
    		int boxRow = row/3;
    		int boxCol = col/3;
    		for (int cellNum = 0; cellNum < 81; cellNum++) {
    			// Calculate each cell's box 
    			int cellBoxRow = cellNum/9/3;
    			int cellBoxCol = (cellNum%9)/3;
    			if (cellBoxRow==boxRow && cellBoxCol==boxCol && cellNum != i) {
    				neighbors[i].add(cellNum);
    			}
    		}
    	}
    	
    	// Add every arc to the globalqueue.
    	for (int cellNum = 0; cellNum < 81; cellNum ++) {
    		for (int constraintCell : neighbors[cellNum]) {
    			Arc tempArc = new Arc(cellNum, constraintCell);
    			globalQueue.add(tempArc);
    		}
    	}
    	
    }


    // This is the Recursive AC3.  
    private final boolean backtrack(int cellNum, ArrayList<Integer>[] Domains) {

        recursions += 1;
        if (cellNum>80) {
        	// only update the global domains when it is successful
        	globalDomains = Domains;
        	return true;
        }
        
        int cellRow = cellNum/9;
        int cellCol = cellNum%9;
        
        // If the current value on the board is already assigned, go check the next one.
        if (vals[cellRow][cellCol]!=0) {
        	if (backtrack(cellNum+1, Domains)) {
        		return true;
        	}
        }
        
        // Update the domains with the AC3 algorithm, return false if one the variable's domains becomes empty 
        Boolean AC3Results = AC3(Domains);
        if (!AC3Results) {
        	return false;
        }
        
        //For each value in the domain of the current cell
        for (int index = 0; index <Domains[cellNum].size();index++) {

        	// Saving the current domain status in order to try values. 
            ArrayList<Integer>[] tempDomains = new ArrayList[81];
            for (int i=0; i<tempDomains.length;i++) {
            	ArrayList<Integer> eachDomains = Domains[i];
            	ArrayList<Integer> eachTempDomains = new ArrayList<Integer>();
            	for (int eachDomain : eachDomains) {
            		eachTempDomains.add(eachDomain);
            	}
            	tempDomains[i] = eachTempDomains;
            }
        	
        	
        	int cellDomainValue = tempDomains[cellNum].get(index);

        	// Try assigning a value to the domain of the current cell
        	tempDomains[cellNum].clear();
        	tempDomains[cellNum].add(cellDomainValue);
        	
        	// Recursively call backtrack on the next cell 
        	if (backtrack(cellNum+1, tempDomains)) {
        		return true;
        	} 
        }
       
        return false;

    }

    // This is the actual AC-3 Algorithm 
    private final boolean AC3(ArrayList<Integer>[] Domains) {
    	
    	// Create a local queue so that we don't change the global constraints
    	Queue<Arc> localQueue = new LinkedList<Arc>();
    	for (Arc tempArc : globalQueue) {
    		localQueue.add(tempArc);
    	}
    	    	
    	while (!localQueue.isEmpty()) {
    		Arc tempArc = localQueue.poll();
    		
    		// Check if we need to revise x's domain
    		Boolean revised = Revise(tempArc, Domains);
    		
    		// If we revised x's domains
    		if (revised) {
    			// Returns false if x's domain is empty. No solution will be found in this case.
    			if (Domains[tempArc.Xi].size()==0) {
	    			return false;
	    		}
    			// Else, for every other neighbor of Xi, create new arc and them to the queue.
    			for (int neighbor : neighbors[tempArc.Xi]) {
    				if (neighbor!=tempArc.Xj) {
    					localQueue.add(new Arc(neighbor,tempArc.Xi));
    				}
    			}
    		}		
    	}
		return true;
    }
    
    

     // This is the Revise() method defined in the book
     private final boolean Revise(Arc t, ArrayList<Integer>[] Domains){
         ops += 1;
         Boolean revised = false;
         
         // For each value in Xi's domain, if Xj's domain only contains this value, then they are not consistent
         // Need to remove this value from Xi's domain.
         for (int xiValueIndex = 0; xiValueIndex < Domains[t.Xi].size(); xiValueIndex++) {
        	 if (Domains[t.Xj].size() == 1 && Domains[t.Xj].get(0) == Domains[t.Xi].get(xiValueIndex)) {
        		 Domains[t.Xi].remove(xiValueIndex);
        		 revised = true;
        	 }
         }
         return revised;
 	}

   
    // Custom Solver that uses most constrained variable to decide which variables to modify next
    private final void customSolver(){
    
    		recursions = 0;
    	
		    board.Clear();
	        System.out.println("Running custom algorithm");
	        // Set up global domain
	        for (int i = 0; i < 81; i++) {
	        	int row = i/9;
	        	int col = i%9;
	        	ArrayList<Integer> eachDomain = new ArrayList<Integer>();
	        	// If the cell is empty, then the domain is 1 to 9
	        	if (vals[row][col] == 0) {
	        		for (int domainNum = 1; domainNum < 10; domainNum++) {
	        			eachDomain.add(domainNum);
	        		}
	        	}
	        	// If the cell is already filled, then the domain is just that number in the cell.
	        	else {
	        		eachDomain.add(vals[row][col]);
	        	}
	        	globalDomains[i] = eachDomain;
	        }
	        
	        allDiff();
	        
	        // Set up a linkedhashmap that maps cell numbers to domains, which is then sorted
	        // by domain size. (The one with the most constraints first) 
	        setUpSortedCells(globalDomains);
	        sortedCells = sortCells(sortedCells);
	        
	         // Initial call to customBacktrack() on cell 0 (top left)
	        boolean success = customBacktrack(0,globalDomains,sortedCells);
	        
	        // Update the board value when successful
	        for (int i=0; i<81;i++) {
	        	int row = i/9;
	        	int col = i%9;
	        	vals[row][col] = globalDomains[i].get(0);
	        }
		   Finished(success);
    	       
    }
    
    // Sets up the sorted cells with cell number - domain pairs. 
    private void setUpSortedCells (ArrayList<Integer>[] Domains) {
    	for (int i=0; i<81; i++) {
    		sortedCells.put(i, Domains[i]);
    	}
    }
    
    // Function to sort the linkedhashmap according to the length of its domains. 
    private LinkedHashMap<Integer, ArrayList<Integer>> sortCells (LinkedHashMap<Integer, ArrayList<Integer>> cells){
    	ArrayList<Map.Entry<Integer, ArrayList<Integer>>> entries 
    	=  new ArrayList<Map.Entry<Integer, ArrayList<Integer>>>(cells.entrySet());
    	
    	// Set up comparator for our sorting criteria
    	Collections.sort(entries, new Comparator<Map.Entry<Integer, ArrayList<Integer>>>() {
    		  public int compare(Map.Entry<Integer, ArrayList<Integer>> a, Map.Entry<Integer, ArrayList<Integer>> b){
    		    if (a.getValue().size() > b.getValue().size()) {
    		    	return 1;
    		    } else if (a.getValue().size() < b.getValue().size()) {
    		    	return -1;
    		    } else {
    		    	return 0;
    		    }
    		  }
    	});
    	
    	// Sort cells (ones with the most constraints first)
    	LinkedHashMap<Integer, ArrayList<Integer>> sortedCells = new LinkedHashMap<Integer,ArrayList<Integer>>();
    	for (Map.Entry<Integer, ArrayList<Integer>> entry : entries) {
    		  sortedCells.put(entry.getKey(), entry.getValue());
    	}
    	return sortedCells;
    }
    
    // backtrack modified for our custom algorithm
    private boolean customBacktrack(int cellNum, ArrayList<Integer>[] Domains, 
    		LinkedHashMap<Integer, ArrayList<Integer>> sortedDomains) {
    	
    	recursions += 1;
    	
    	// Make a copy of the sorted domains
    	LinkedHashMap<Integer, ArrayList<Integer>> tempSortedDomains = new LinkedHashMap<Integer, ArrayList<Integer>>();
    	for (Map.Entry<Integer, ArrayList<Integer>> entry : sortedDomains.entrySet()) {
    		tempSortedDomains.put(entry.getKey(), entry.getValue());
    	}
    	
        if (sortedDomains.size() == 0) {
        	// only update the global domains when it is successful
        	globalDomains = Domains;
        	return true;
        }
        
        int cellRow = cellNum/9;
        int cellCol = cellNum%9;
                
        // If the current value on the board is already assigned, go check the next cell with the most constraints.
        if (vals[cellRow][cellCol]!=0) {
        	// Get the cell with the most constraints, delete it from the linked hashmap 
        	// and run custom backtrack on it.
        	int firstCellNum = tempSortedDomains.entrySet().iterator().next().getKey();
        	tempSortedDomains.remove(firstCellNum);
        	if (customBacktrack(firstCellNum, Domains, tempSortedDomains)) {
        		return true;
        	}
        }
        
        // Update the domains with the AC3 algorithm, return false if one the variable's domains becomes empty 
        Boolean AC3Results = AC3(Domains);
        
        
        if (!AC3Results) {
        	return false;
        }
        
        //For each value in the domain of the current cell
        for (int index = 0; index <Domains[cellNum].size();index++) {

        	// Saving the current domain status in order to try values. 
            ArrayList<Integer>[] tempDomains = new ArrayList[81];
            for (int i=0; i<tempDomains.length;i++) {
            	ArrayList<Integer> eachDomains = Domains[i];
            	ArrayList<Integer> eachTempDomains = new ArrayList<Integer>();
            	for (int eachDomain : eachDomains) {
            		eachTempDomains.add(eachDomain);
            	}
            	tempDomains[i] = eachTempDomains;
            }     
            
            // Make another copy of the sorted domains before trying to assign a value to the cell
            LinkedHashMap<Integer, ArrayList<Integer>> tempSortedDomains2 = 
            		new LinkedHashMap<Integer, ArrayList<Integer>>();
            for (int key : sortedDomains.keySet()) {
            	tempSortedDomains2.put(key, tempDomains[key]);
            }
            tempSortedDomains2 = sortCells(tempSortedDomains2);        
            
            int cellDomainValue = tempDomains[cellNum].get(index);

        	// Try assigning a value to the domain of the current cell
        	tempDomains[cellNum].clear();
        	tempDomains[cellNum].add(cellDomainValue);
        	
        	// Get the cell with the most constraints, remove it from the linked hash map
        	int firstCellNum2 = tempSortedDomains2.entrySet().iterator().next().getKey();
        	tempSortedDomains2.remove(firstCellNum2);
        	// Recursively call backtrack on the cell with the most constraints.
        	if (customBacktrack(firstCellNum2, tempDomains, tempSortedDomains2)) {
        		return true;
        	} 
        }
       
        return false;

    }
    
    
    

    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    public final boolean valid(int x, int y, int val){
        ops +=1;
        if (vals[x][y] == val)
            return true;
        if (rowContains(x,val))
            return false;
        if (colContains(y,val))
            return false;
        if (blockContains(x,y,val))
            return false;
        return true;
    }

    public final boolean blockContains(int x, int y, int val){
        int block_x = x / 3;
        int block_y = y / 3;
        for(int r = (block_x)*3; r < (block_x+1)*3; r++){
            for(int c = (block_y)*3; c < (block_y+1)*3; c++){
                if (vals[r][c] == val)
                    return true;
            }
        }
        return false;
    }

    public final boolean colContains(int c, int val){
        for (int r = 0; r < 9; r++){
            if (vals[r][c] == val)
                return true;
        }
        return false;
    }

    public final boolean rowContains(int r, int val) {
        for (int c = 0; c < 9; c++)
        {
            if(vals[r][c] == val)
                return true;
        }
        return false;
    }

    private void CheckSolution() {
        // If played by hand, need to grab vals
        board.updateVals(vals);

        /*for(int i=0; i<9; i++){
	        for(int j=0; j<9; j++)
	        	System.out.print(vals[i][j]+" ");
	        System.out.println();
        }*/
        
        for (int v = 1; v <= 9; v++){
            // Every row is valid
            for (int r = 0; r < 9; r++)
            {
                if (!rowContains(r,v))
                {
                    board.showMessage("Value "+v+" missing from row: " + (r+1));// + " val: " + v);
                    return;
                }
            }
            // Every column is valid
            for (int c = 0; c < 9; c++)
            {
                if (!colContains(c,v))
                {
                    board.showMessage("Value "+v+" missing from column: " + (c+1));// + " val: " + v);
                    return;
                }
            }
            // Every block is valid
            for (int r = 0; r < 3; r++){
                for (int c = 0; c < 3; c++){
                    if(!blockContains(r, c, v))
                    {
                        return;
                    }
                }
            }
        }
        board.showMessage("Success!");
    }

    

    /// ---- GUI + APP Code --- ////
    /// ----   DO NOT EDIT  --- ////
    enum algorithm {
        AC3, Custom
    }
    class Arc implements Comparable<Object>{
        int Xi, Xj;
        public Arc(int cell_i, int cell_j){
            if (cell_i == cell_j){
                try {
                    throw new Exception(cell_i+ "=" + cell_j);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            Xi = cell_i;      Xj = cell_j;
        }

        public int compareTo(Object o){
            return this.toString().compareTo(o.toString());
        }

        public String toString(){
            return "(" + Xi + "," + Xj + ")";
        }
    }

    enum difficulty {
        easy, medium, hard, random
    }

    public void actionPerformed(ActionEvent e){
        String label = ((JButton)e.getSource()).getText();
        if (label.equals("AC-3"))
            init();
        else if (label.equals("Clear"))
            board.Clear();
        else if (label.equals("Check"))
            CheckSolution();
            //added
        else if(label.equals("Custom"))
            customSolver();
    }

    public void run() {
        board = new Board(gui,this);
        
        long start=0, end=0;
       
        while(!initialize());
        if (gui)
            board.initVals(vals);
        else {
            board.writeVals();
            System.out.println("Algorithm: " + alg);
            switch(alg) {
                default:
                case AC3:
                	start = System.currentTimeMillis();
                    init();
                    end = System.currentTimeMillis();
                    break;
                case Custom: //added
                	start = System.currentTimeMillis();
                	customSolver();
                	end = System.currentTimeMillis();
                    break;
            }
            
            CheckSolution();
            
            if(!gui)
            	System.out.println("time to run: "+(end-start));
        }
    }

    public final boolean initialize(){
        switch(level) {
            case easy:
                vals[0] = new int[] {0,0,0,1,3,0,0,0,0};
                vals[1] = new int[] {7,0,0,0,4,2,0,8,3};
                vals[2] = new int[] {8,0,0,0,0,0,0,4,0};
                vals[3] = new int[] {0,6,0,0,8,4,0,3,9};
                vals[4] = new int[] {0,0,0,0,0,0,0,0,0};
                vals[5] = new int[] {9,8,0,3,6,0,0,5,0};
                vals[6] = new int[] {0,1,0,0,0,0,0,0,4};
                vals[7] = new int[] {3,4,0,5,2,0,0,0,8};
                vals[8] = new int[] {0,0,0,0,7,3,0,0,0};
                break;
            case medium:
                vals[0] = new int[] {0,4,0,0,9,8,0,0,5};
                vals[1] = new int[] {0,0,0,4,0,0,6,0,8};
                vals[2] = new int[] {0,5,0,0,0,0,0,0,0};
                vals[3] = new int[] {7,0,1,0,0,9,0,2,0};
                vals[4] = new int[] {0,0,0,0,8,0,0,0,0};
                vals[5] = new int[] {0,9,0,6,0,0,3,0,1};
                vals[6] = new int[] {0,0,0,0,0,0,0,7,0};
                vals[7] = new int[] {6,0,2,0,0,7,0,0,0};
                vals[8] = new int[] {3,0,0,8,4,0,0,6,0};
                break;
            case hard:
            	vals[0] = new int[] {1,2,0,4,0,0,3,0,0};
            	vals[1] = new int[] {3,0,0,0,1,0,0,5,0};  
            	vals[2] = new int[] {0,0,6,0,0,0,1,0,0};  
            	vals[3] = new int[] {7,0,0,0,9,0,0,0,0};    
            	vals[4] = new int[] {0,4,0,6,0,3,0,0,0};    
            	vals[5] = new int[] {0,0,3,0,0,2,0,0,0};    
            	vals[6] = new int[] {5,0,0,0,8,0,7,0,0};    
            	vals[7] = new int[] {0,0,7,0,0,0,0,0,5};    
            	vals[8] = new int[] {0,0,0,0,0,0,0,9,8};  
                break;
            case random:
            default:
                ArrayList<Integer> preset = new ArrayList<Integer>();
                while (preset.size() < numCells)
                {
                    int r = rand.nextInt(81);
                    if (!preset.contains(r))
                    {
                        preset.add(r);
                        int x = r / 9;
                        int y = r % 9;
                        if (!assignRandomValue(x, y))
                            return false;
                    }
                }
                break;
        }
        return true;
    }

    public final boolean assignRandomValue(int x, int y){
        ArrayList<Integer> pval = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));

        while(!pval.isEmpty()){
            int ind = rand.nextInt(pval.size());
            int i = pval.get(ind);
            if (valid(x,y,i)) {
                vals[x][y] = i;
                return true;
            } else
                pval.remove(ind);
        }
        System.err.println("No valid moves exist.  Recreating board.");
        for (int r = 0; r < 9; r++){
            for(int c=0;c<9;c++){
                vals[r][c] = 0;
            }    }
        return false;
    }

    private void Finished(boolean success){
        if(success) {
            board.writeVals();
            board.showMessage("Solved in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
        } else {
            board.showMessage("No valid configuration found in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
        }
    }

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

        char c='*';


        while(c!='e'&& c!='m'&&c!='n'&&c!='h'&&c!='r'){
            c = scan.nextLine().charAt(0);

            if(c=='e')
                level = difficulty.valueOf("easy");
            else if(c=='m')
                level = difficulty.valueOf("medium");
            else if(c=='h')
                level = difficulty.valueOf("hard");
            else if(c=='r')
                level = difficulty.valueOf("random");
            else{
                System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
            }
            //System.out.println("2: "+c+" "+level);
        }

        System.out.println("Gui? y or n ");
        c=scan.nextLine().charAt(0);

        if (c=='n')
            gui = false;
        else
            gui = true;

        //System.out.println("c: "+c+", Difficulty: " + level);

        //System.out.println("Difficulty: " + level);

        if(!gui){
            System.out.println("Algorithm? AC3 (1) or Custom (2)");
            if(scan.nextInt()==1)
                alg = algorithm.valueOf("AC3");
            else
                alg = algorithm.valueOf("Custom");
        }

        SudokuPlayer app = new SudokuPlayer();
       
        app.run();
      
    }


    class Board {
        GUI G = null;
        boolean gui = true;

        public Board(boolean X, SudokuPlayer s) {
            gui = X;
            if (gui)
                G = new GUI(s);
        }

        public void initVals(int[][] vals){
            G.initVals(vals);
        }

        public void writeVals(){
            if (gui)
                G.writeVals();
            else {
                for (int r = 0; r < 9; r++) {
                    if (r % 3 == 0)
                        System.out.println(" ----------------------------");
                    for (int c = 0; c < 9; c++) {
                        if (c % 3 == 0)
                            System.out.print (" | ");
                        if (vals[r][c] != 0) {
                            System.out.print(vals[r][c] + " ");
                        } else {
                            System.out.print("_ ");
                        }
                    }
                    System.out.println(" | ");
                }
                System.out.println(" ----------------------------");
            }
        }

        public void Clear(){
            if(gui)
                G.clear();
        }

        public void showMessage(String msg) {
            if (gui)
                G.showMessage(msg);
            System.out.println(msg);
        }

        public void updateVals(int[][] vals){
            if (gui)
                G.updateVals(vals);
        }

    }

    class GUI {
        // ---- Graphics ---- //
        int size = 40;
        JFrame mainFrame = null;
        JTextField[][] cells;
        JPanel[][] blocks;

        public void initVals(int[][] vals){
            // Mark in gray as fixed
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (vals[r][c] != 0) {
                        cells[r][c].setText(vals[r][c] + "");
                        cells[r][c].setEditable(false);
                        cells[r][c].setBackground(Color.lightGray);
                    }
                }
            }
        }

        public void showMessage(String msg){
            JOptionPane.showMessageDialog(null,
                    msg,"Message",JOptionPane.INFORMATION_MESSAGE);
        }

        public void updateVals(int[][] vals) {

           // System.out.println("calling update");
            for (int r = 0; r < 9; r++) {
                for (int c=0; c < 9; c++) {
                    try {
                        vals[r][c] = Integer.parseInt(cells[r][c].getText());
                    } catch (java.lang.NumberFormatException e) {
                        System.out.println("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        showMessage("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        return;
                    }
                }
            }
        }

        public void clear() {
            for (int r = 0; r < 9; r++){
                for (int c = 0; c < 9; c++){
                    if (cells[r][c].isEditable())
                    {
                        cells[r][c].setText("");
                        vals[r][c] = 0;
                    } else {
                        cells[r][c].setText("" + vals[r][c]);
                    }
                }
            }
        }

        public void writeVals(){
            for (int r=0;r<9;r++){
                for(int c=0; c<9; c++){
                    cells[r][c].setText(vals[r][c] + "");
                }   }
        }

        public GUI(SudokuPlayer s){

            mainFrame = new javax.swing.JFrame();
            mainFrame.setLayout(new BorderLayout());
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel gamePanel = new javax.swing.JPanel();
            gamePanel.setBackground(Color.black);
            mainFrame.add(gamePanel, BorderLayout.NORTH);
            gamePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            gamePanel.setLayout(new GridLayout(3,3,3,3));

            blocks = new JPanel[3][3];
            for (int i = 0; i < 3; i++){
                for(int j =2 ;j>=0 ;j--){
                    blocks[i][j] = new JPanel();
                    blocks[i][j].setLayout(new GridLayout(3,3));
                    gamePanel.add(blocks[i][j]);
                }
            }

            cells = new JTextField[9][9];
            for (int cell = 0; cell < 81; cell++){
                int i = cell / 9;
                int j = cell % 9;
                cells[i][j] = new JTextField();
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setSize(new java.awt.Dimension(size, size));
                cells[i][j].setPreferredSize(new java.awt.Dimension(size, size));
                cells[i][j].setMinimumSize(new java.awt.Dimension(size, size));
                blocks[i/3][j/3].add(cells[i][j]);
            }

            JPanel buttonPanel = new JPanel(new FlowLayout());
            mainFrame.add(buttonPanel, BorderLayout.SOUTH);
            //JButton DFS_Button = new JButton("DFS");
            //DFS_Button.addActionListener(s);
            JButton AC3_Button = new JButton("AC-3");
            AC3_Button.addActionListener(s);
            JButton Clear_Button = new JButton("Clear");
            Clear_Button.addActionListener(s);
            JButton Check_Button = new JButton("Check");
            Check_Button.addActionListener(s);
            //buttonPanel.add(DFS_Button);
            JButton Custom_Button = new JButton("Custom");
            Custom_Button.addActionListener(s);
            //added
            buttonPanel.add(AC3_Button);
            buttonPanel.add(Custom_Button);
            buttonPanel.add(Clear_Button);
            buttonPanel.add(Check_Button);

            mainFrame.pack();
            mainFrame.setVisible(true);

        }
    }

    Random rand = new Random();

    // ----- Helper ---- //
    static algorithm alg = algorithm.AC3;
    static difficulty level = difficulty.easy;
    static boolean gui = true;
    static int ops;
    static int recursions;
    static int numCells = 15;
    static DecimalFormat myformat = new DecimalFormat("###,###");
}
