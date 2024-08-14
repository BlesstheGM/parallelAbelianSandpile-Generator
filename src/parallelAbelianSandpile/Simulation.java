package parallelAbelianSandpile;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;
import java.util.Arrays;
import javax.imageio.ImageIO;
import java.io.BufferedReader;

public class Simulation {

    static long startTime = 0;
	 static long endTime = 0;

    private static void tick(){ //start timing
        startTime = System.currentTimeMillis();
	 }
    
	 private static void tock(){ //end timing
		  endTime=System.currentTimeMillis(); 
	 }
    
    public static int[][] readArrayFromCSV(String filePath) {
		  int [][] array = null;
	         try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	             String line = br.readLine();
	             if (line != null) {
	                 String[] dimensions = line.split(",");
	                 int width = Integer.parseInt(dimensions[0]);
	                 int height = Integer.parseInt(dimensions[1]);
	                 System.out.printf("Rows: %d, Columns: %d\n", width, height); //Do NOT CHANGE  - you must ouput this

	                 array = new int[height][width];
	                 int rowIndex = 0;

	                 while ((line = br.readLine()) != null && rowIndex < height) {
	                     String[] values = line.split(",");
	                     for (int colIndex = 0; colIndex < width; colIndex++) {
	                         array[rowIndex][colIndex] = Integer.parseInt(values[colIndex]);
	                     }
	                     rowIndex++;
	                 }
	             }

	         } catch (IOException e) {
	            e.printStackTrace();
	         }
	         return array;
    }
       
    public static void main(String [] args) throws IOException {
    
        ParallelGrid myGrid;
        
        if (args.length!=2) {   //input is the name of the input and output files
    	      System.out.println("Incorrect number of command line arguments provided.");   	
    		   System.exit(0);
    	  }
        
    	  /* Read argument values */
  		  String inputFileName = args[0];  //input file name
		  String outputFileName=args[1]; // output file name
    
    	  // Read from input .csv file
        int[][] readGrid = (readArrayFromCSV(inputFileName));
         	   
        ForkJoinPool pool = new ForkJoinPool(); 
        int size = readGrid.length;
        int value = readGrid[0][0];
        boolean changed;
        int[][] testGrid = new int[size + 2][size + 2];
        int[][] update = new int[size+2][size + 2];
        
        for (int i=0; i<testGrid.length; i++) {
            for (int j=0; j<testGrid[0].length; j++) {
               update[i][j] = testGrid[i][j];
            }
        }
    
        // Initialize the inner grid and the copy grid with 4s
        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= size; j++) {
               testGrid[i][j] = value;
               update[i][j] = value;
            }
        }
    
    	  int counter=0;
      
        tick(); //start timer

        do{
            myGrid = new ParallelGrid(testGrid, update, 1, size + 1, 1, size + 1);
            changed = pool.invoke(myGrid);
            counter++;
            if (changed) {
               myGrid.update();
            }
          } while (changed);

        counter -= 1; // explain this
        tock(); //end time

		
        System.out.println("Simulation complete, writing image...");
      
        //write grid as an image - you must do this.
        try {
            myGrid.gridToImage(outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

    	  //Do NOT CHANGE below!
    	  //simulation details - you must keep these lines at the end of the output in the parallel versions      	System.out.printf("\t Rows: %d, Columns: %d\n", simulationGrid.getRows(), simulationGrid.getColumns());
		  System.out.printf("Number of steps to stable state: %d \n",counter);
		  System.out.printf("Time: %d ms\n",endTime - startTime );			/*  Total computation time */		

      }
   
    }

