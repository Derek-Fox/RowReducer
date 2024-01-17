import java.util.*;

public class Main {
    static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to the row reducer.");

//        int[] rowColumn = getRowsAndColumns();
//        double[][] matrix = createMatrix(rowColumn[0], rowColumn[1]);
//        RowReducer rr = new RowReducer();

        RowReducer rr = new RowReducer(false);
        double[][] matrix = new double[][]{
                {1, 0, -8, -3},
                {0, 1, -1, -1}
        };

        rr.reduce(matrix);
    }

    /**
     * Gets the number of rows and columns from stdin.
     *
     * @return array with {rows, cols}
     */
    private static int[] getRowsAndColumns() {
        System.out.print("Enter the number of rows in the augmented matrix: ");
        int rows = in.nextInt();
        System.out.print("Enter the number of columns in the augmented matrix: ");
        int columns = in.nextInt();
        in.nextLine(); //consume newline (i hate java)
        return new int[]{rows, columns};
    }

    /**
     * Populates matrix from stdin.
     *
     * @param rows number of rows
     * @param cols number of cols
     */
    public static double[][] createMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
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
        return matrix;
    }
}

class RowReducer {
    private int rows, cols;
    private final List<int[]> pivots = new ArrayList<>();
    private final boolean verbose;

    public RowReducer(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Entry point to perform the row reduction algorithm.
     * Determines if given augmented matrix represents a system that is consistent or inconsistent.
     * Finds the echelon form of any matrix; if consistent, finds reduced echelon form.
     * Prints the solution set of a consistent system; if there are infinite solutions, gives parameterized form
     *
     * @param matrix augmented matrix of real numbers to perform the row reduction algorithm on.
     */
    public void reduce(double[][] matrix) {
        this.rows = matrix.length; //save n and m of given matrix
        this.cols = matrix[0].length;

        System.out.println("Original matrix:");
        printMatrix(matrix);
        System.out.println("Performing forward phase of algorithm...");

        forward(matrix);
        if (checkInconsistent(matrix)) {
            System.out.println("System is inconsistent. No solutions.");
        } else {
            System.out.println("Performing backward phase of algorithm...");
            backward(matrix);
            printSolutions(matrix);
        }
    }

    /**
     * Pretty print the given matrix.
     *
     * @param matrix matrix to print
     */
    private void printMatrix(double[][] matrix) {
        for (double[] row : matrix) {
            System.out.print("[");
            for (int col = 0; col < cols; col++) {
                System.out.printf("%.4f", row[col]);
                if(col < cols-1) System.out.print(", ");
            }
            System.out.println("]");
        }
    }

    /**
     * Checks if a system in echelon form is inconsistent. To be called after forward().
     *
     * @return true if system is inconsistent
     */
    private boolean checkInconsistent(double[][] matrix) {
        for (int[] pivot : pivots) {
            if (pivot[1] == matrix[0].length - 1) return true; //if pivot in final column
        }
        return false;
    }

    /**
     * Prints solutions of the system. To be called after backward(), and only on consistent systems.
     */
    private void printSolutions(double[][] matrix) {
        int cols = matrix[0].length;
        if (pivots.size() == cols - 1) { //pivot in every column (except augment) -> unique solution
            for (int[] pivot : pivots) {
                System.out.printf("X%d = %f\n", pivot[1], matrix[pivot[0]][cols - 1]);
            }
        } else printParameterizedSolSet(matrix);
    }

    /**
     * Prints the parameterized form of the solution set of a given system.
     * Assumes the system indeed has infinite solutions.
     */
    private void printParameterizedSolSet(double[][] matrix) {
        Set<Integer> pivotCols = new HashSet<>();
        for (int[] pivot : pivots) {
            pivotCols.add(pivot[1]);
        }

        for (double[] row : matrix) {
            StringBuffer out = new StringBuffer();
            int basicCol = -1;
            for (int col = 0; col < cols - 1; col++) {
                if (pivotCols.contains(col)) {
                    if (row[col] != 0) basicCol = col;
                } else if (basicCol != -1) {
                    out.append("X").append(basicCol).append(" = ");
                    out.append(row[cols - 1]);

                    double coeff = row[col];

                    if (coeff > 0) out.append(" - ");
                    else out.append(" + ");

                    out.append(Math.abs(coeff)).append("X").append(col + 1);
                    break;
                }
            }
            System.out.println(out);
        }
    }

    /**
     * Performs backward phase of row reduction algorithm.
     */
    private void backward(double[][] matrix) {
        List<int[]> reversePivots = new ArrayList<>(pivots);
        Collections.reverse(reversePivots);
        for (int[] pivot : reversePivots) {
            int startRow = pivot[0], col = pivot[1];
            for (int row = startRow - 1; row >= 0; row--) {
                double valueAbove = matrix[row][col];
                if (valueAbove != 0) {
                    replace(matrix, row, startRow, -valueAbove);
                }
            }
            if (verbose) {
                printMatrix(matrix);
                System.out.println();
            }
        }
    }

    /**
     * Performs the forward phase of the row reduction algorithm.
     * After completion, the matrix of this instance *should* be in echelon form.
     */
    private void forward(double[][] matrix) {
        int currRow = 0, currCol = 0; //row and col of current "search space" i.e. rows and columns not yet considered
        int[] rowAndCol = scanForNonZero(matrix, currRow, currCol);
        while (rowAndCol != null) {
            currCol = rowAndCol[1];

            if (rowAndCol[0] != currRow) {
                swap(matrix, currRow, rowAndCol[0]);
            }

            double scaleFac = 1 / matrix[currRow][currCol]; //value * scaleFac = 1 -> 1 / value = scaleFac
            scale(matrix, currRow, scaleFac);

            for (int row = currRow + 1; row < rows; row++) {
                double valueBelow = matrix[row][currCol];
                if (valueBelow != 0) {
                    replace(matrix, row, currRow, -valueBelow);
                }
            }

            pivots.add(new int[]{currRow, currCol});
            rowAndCol = scanForNonZero(matrix, ++currRow, ++currCol);

            if (verbose) {
                printMatrix(matrix);
                System.out.println();
            }
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
    private int[] scanForNonZero(double[][] matrix, int startRow, int startCol) {
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
    private void swap(double[][] matrix, int R1, int R2) {
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
    private void scale(double[][] matrix, int row, double factor) {
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
    private void replace(double[][] matrix, int R1, int R2, double factor) {
        for (int i = 0; i < cols; i++) {
            matrix[R1][i] += matrix[R2][i] * factor;
        }
    }
}