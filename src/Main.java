import java.util.*;

public class Main {
    static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to the row reducer.");

//        int[] rowColumn = getRowsAndColumns();
//        RowReduction rr = new RowReduction(rowColumn[0], rowColumn[1]);
//        rr.populateMatrix(in);
        RowReduction rr = new RowReduction(3, 4);
        rr.matrix = new double[][] { //test matrix
                new double[]{1, 2, 3, 6},
                new double[]{2, -3, 2, 14},
                new double[]{3, 1, -1, -2}
        };
        rr.printMatrix();

        System.out.println("Performing forward phase of algorithm...");
        rr.forward();
        if (rr.checkInconsistent()) {
            System.out.println("System is inconsistent. No solutions.");
        } else {
            System.out.println("Performing backward phase of algorithm...");
            rr.backward();
            rr.printSolutions();
        }
    }

    private static int[] getRowsAndColumns() {
        System.out.print("Enter the number of rows in the augmented matrix: ");
        int rows = in.nextInt();
        System.out.print("Enter the number of columns in the augmented matrix: ");
        int columns = in.nextInt();
        in.nextLine(); //consume newline (i hate java)
        return new int[]{rows, columns};
    }
}

class RowReduction {
    private final int rows, cols;
    private List<int[]> pivots = new ArrayList<>();
    double[][] matrix;

    public RowReduction(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.matrix = new double[rows][cols];
    }

    /**
     * Populates matrix from stdin.
     * @param in scanner to use
     */
    public void populateMatrix(Scanner in) {
        for (int i = 0; i < rows; i++) {
            System.out.printf("Enter all values for row %d as a space separated list.\n", i + 1);
            String[] parts = in.nextLine().split(" ");

            while (parts.length > cols) {
                System.out.println("Too many values. Try again.");
                parts = in.nextLine().split(" ");
            }

            double[] row = Arrays.stream(parts).mapToDouble(Double::parseDouble).toArray();
            matrix[i] = row;
        }
    }

    public void printMatrix() {
        for (double[] row : matrix) {
            System.out.print("[  ");
            for (double v : row) {
                System.out.printf("%.4f  ", v);
            }
            System.out.println("]");
        }
    }

    /**
     * Checks if a system in echelon form is inconsistent. To be called after forward().
     * @return true if system is inconsistent
     */
    public boolean checkInconsistent() {
        for (int[] pivot : pivots) {
            if (pivot[1] == cols-1) return true; //if pivot in final column
        }
        return false;
    }

    /**
     * Prints solutions of the system. To be called after backward(), and only on consistent systems.
     */
    public void printSolutions() {
        if (pivots.size() == cols - 1) { //pivot in every column (except augment) -> unique solution
            for (int[] pivot : pivots) {
                System.out.printf("X%d = %f\n", pivot[1], matrix[pivot[0]][cols-1]);
            }
        } else {
            Set<Integer> pivotCols = new HashSet<>();
            for (int[] pivot : pivots) {
                pivotCols.add(pivot[1]);
            }
            for (int row = 0; row < rows; row++) {
                List<Integer> basicCols = new ArrayList<>();
                List<Integer> freeCols = new ArrayList<>();
                for (int col = 0; col < cols-1; col++) {
                    if(pivotCols.contains(col)) {
                        if (matrix[row][col] != 0) basicCols.add(col);
                    } else {
                        freeCols.add(col);
                    }
                }

                StringBuffer out = new StringBuffer();
                for (Integer col : basicCols) {
                    out.append("X").append(col).append(" = ");
                }
                out.append(matrix[row][cols-1]);
                for (Integer col : freeCols) {
                    out.append(" - ").append(matrix[row][col]).append("X").append(col+1);
                }
                System.out.println(out);
            }
        }
    }

    /**
     * Performs backward phase of row reduction algorithm.
     */
    public void backward() {
        List<int[]> reversePivots = new ArrayList<>(pivots);
        Collections.reverse(reversePivots);
        for (int[] pivot : reversePivots) {
            int startRow = pivot[0], col = pivot[1];
            for (int row = startRow - 1; row >= 0; row--) {
                double valueAbove = matrix[row][col];
                if (valueAbove != 0) {
                    replace(row, startRow, -valueAbove);
                }
            }
            printMatrix();
            System.out.println();
        }
    }

    /**
     * Performs the forward phase of the row reduction algorithm.
     * After completion, the matrix of this instance *should* be in echelon form.
     */
    public void forward() {
        /*
        1) Scan rows left to right to find first non-zero value
        2) Swap (if necessary) so this value is in topmost unconsidered row
        3) Scale this value to 1
        4) Use row replacement so all entries below this 1 are 0.
        5) Repeat as necessary until out of columns
         */

        int currRow = 0, currCol = 0; //row and col of current "search space" i.e. rows and columns not yet considered
        // Step 1
        int[] rowAndCol = scanForNonZero(currRow, currCol);
        while(rowAndCol != null) {
            currCol = rowAndCol[1];

            // Step 2
            if (rowAndCol[0] != currRow) {
                swap(currRow, rowAndCol[0]);
            }

            // Step 3
            double scaleFac = 1 / matrix[currRow][currCol]; //value * scaleFac = 1 -> 1 / value = scaleFac
            scale(currRow, scaleFac);

            //Step 4
            for (int row = currRow + 1; row < rows; row++) {
                double valueBelow = matrix[row][currCol];
                if (valueBelow != 0) {
                    replace(row, currRow, -valueBelow);
                }
            }

            pivots.add(new int[]{currRow, currCol});
            rowAndCol = scanForNonZero(++currRow, ++currCol);

            printMatrix();
            System.out.println();
        }

    }

    /**
     * Scans matrix from (startRow, startCol) downward and then left until a non-zero value is found.
     * Returns the row and column of that value. Returns null if no more non-zero values.
     *
     * @param startRow index of row to start search
     * @param startCol index of column to start search
     * @return array with {row, column} of found value
     */
    private int[] scanForNonZero(int startRow, int startCol) {
        for (int col = startCol; col < cols; col++) {
            for (int row = startRow; row < rows; row++) {
                if (matrix[row][col] != 0) return new int[]{row, col};
            }
        }
        return null;
    }

    /**
     * Swaps two rows.
     *
     * @param R1 index of first row
     * @param R2 index of second row
     */
    private void swap(int R1, int R2) {
        double[] temp = matrix[R2];
        matrix[R2] = matrix[R1];
        matrix[R1] = temp;
    }

    /**
     * Scales the given row by the given constant factor
     *
     * @param row    index of row
     * @param factor constant factor (real number)
     */
    private void scale(int row, double factor) {
        for (int i = 0; i < cols; i++) {
            matrix[row][i] *= factor;
        }
    }

    /**
     * Performs the elementary row replacement operation.
     * Form: R1 = R1 + (factor)R2
     *
     * @param R1     index of target row
     * @param R2     index of source row
     * @param factor real number factor to scale by (enter 1 for no scaling)
     */
    private void replace(int R1, int R2, double factor) {
        for (int i = 0; i < cols; i++) {
            matrix[R1][i] += matrix[R2][i] * factor;
        }
    }
}