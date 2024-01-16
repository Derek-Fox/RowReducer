import java.util.*;

public class Main {
    static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Welcome to the row reducer.");
        int[] rowColumn = getRowsAndColumns();
        RowReduction rr = new RowReduction(rowColumn[0], rowColumn[1]);
        rr.populateMatrix(in);
        rr.printMatrix();

        System.out.println("Performing forward phase of algorithm...");
        rr.forward();
        //implement algorithm lol
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
    double[][] matrix;

    public RowReduction(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.matrix = new double[rows][cols];
    }

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
            System.out.println(Arrays.toString(row));
        }
    }

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
        if (rowAndCol == null) return; //TODO: this means no more non-zero values. What to do about this?

        // Step 2
        if(rowAndCol[0] != currRow) swap(currRow, rowAndCol[0]);

        // Step 3
        double scaleFac = 1 / matrix[0][0]; //value * scaleFac = 1 -> 1 / value = scaleFac
        scale(currRow, scaleFac);

        for (int row = currRow; row < rows; row++) {

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
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                if (matrix[col][row] != 0) return new int[]{row, col};
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
    private void replace(int R2, int R1, double factor) {
        for (int i = 0; i < cols; i++) {
            matrix[R2][i] += matrix[R1][i] * factor;
        }
    }
}