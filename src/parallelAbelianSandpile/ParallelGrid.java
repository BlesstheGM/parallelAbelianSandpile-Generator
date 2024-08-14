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

public class ParallelGrid extends RecursiveTask<Boolean> {

    public static final int THRESHOLD = 80;
    private final int[][] grid;
    private int[][] updateGrid;
    private final int startRow, endRow, startCol, endCol;


    public ParallelGrid(int[][] grid, int[][] copy,  int startRow, int endRow, int startCol, int endCol) {
        this.grid = grid;
        this.updateGrid = copy;
        this.startRow = startRow;
        this.endRow = endRow;
        this.startCol = startCol;
        this.endCol = endCol;
    }
    
        
    public void update() {
        for(int i=0; i<this.grid.length; i++ ) {
		      for( int j=0; j<this.grid[0].length; j++ ) {
				    this.grid[i][j]=updateGrid[i][j];
			   }
		  }
    }
    
    public void gridToImage(String fileName) throws IOException {
        int rows = grid.length;
        int cols = grid[0].length;
        BufferedImage dstImage = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < rows; i++) { 
            for (int j = 0; j < cols; j++) { 
                int a = 255; // Alpha
                int r = 0; // Red
                int g = 0; // Green
                int b = 0; // Blue

                switch (grid[i][j]) {
                    case 0:
                        // Default color (black), no change needed
                        break;
                    case 1:
                        g = 255;
                        break;
                    case 2:
                        b = 255;
                        break;
                    case 3:
                        r = 255;
                        break;
                    default:
                        // Optional: Handle unexpected values
                        break;
                }

                int pixelColor = (a << 24) | (r << 16) | (g << 8) | b;
                dstImage.setRGB(j, i, pixelColor);
            }
        }

        File dstFile = new File(fileName);
        ImageIO.write(dstImage, "png", dstFile);
    }
    
    public boolean updateCells() {
        boolean changed = false;
        for (int i = startRow; i < endRow; i++) { // Iterate over rows
            for (int j = startCol; j < endCol; j++) { // Iterate over columns
				    updateGrid[i][j] = (grid[i][j] % 4) + 
			       (grid[i-1][j] / 4) +
			    	 grid[i+1][j] / 4 +
				    grid[i][j-1] / 4 + 
				    grid[i][j+1] / 4;
                // If changes were made
			       if (grid[i][j]!=updateGrid[i][j]) {  
					     changed=true;
				    }                  
            }               
        }
        return changed;
    }

    @Override
    protected Boolean compute() {
        if ((endRow - startRow) <= THRESHOLD) { // If the partition is small enough
            return updateCells();
        } else {
            int midRow = (startRow + endRow) / 2;

            ParallelGrid topTask = new ParallelGrid(grid, updateGrid, startRow, midRow, startCol, endCol);
            ParallelGrid bottomTask = new ParallelGrid(grid, updateGrid, midRow, endRow, startCol, endCol);

            topTask.fork();
            boolean bottomChanged = bottomTask.compute();
            boolean topChanged = topTask.join();

            return topChanged || bottomChanged;
        }
    }
 
}
