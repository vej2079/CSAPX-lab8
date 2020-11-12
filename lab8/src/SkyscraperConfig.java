import com.sun.security.auth.login.ConfigFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Represents a single configuration in the skyscraper puzzle.
 *
 * @author RIT CS
 * @author Victoria Jones
 */
public class SkyscraperConfig implements Configuration {
    /** empty cell value */
    public final static int EMPTY = 0;

    /** empty cell value display */
    public final static char EMPTY_CELL = '.';

    /** The current board */
    private int[][] board;

    /** A map of each direction (N,E,S,W) and the associated numbers
     * the sides of the board */
    private HashMap<String, ArrayList<Integer>> direction;

    /** The dimensions of the board */
    private int dim;

    /** The current row to be tested */
    private int currRow;

    /** The current column to be tested */
    private int currCol;


    /**
     * Constructor, constructs a new SkyscraperConfig instance.
     *
     * @param filename the filename
     *  <p>
     *  Read the board file.  It is organized as follows:
     *  DIM     # square DIMension of board (1-9)
     *  lookNS   # DIM values (1-DIM) left to right
     *  lookEW   # DIM values (1-DIM) top to bottom
     *  lookSN   # DIM values (1-DIM) left to right
     *  lookWE   # DIM values (1-DIM) top to bottom
     *  row 1 values    # 0 for empty, (1-DIM) otherwise
     *  row 2 values    # 0 for empty, (1-DIM) otherwise
     *  ...
     *
     *  @throws FileNotFoundException if file not found
     */
    SkyscraperConfig(String filename) throws FileNotFoundException {
        this.currCol = -1;
        this.currRow = 0;
        this.direction = new HashMap<>();
        this.direction.put("N", new ArrayList<Integer>());
        this.direction.put("E", new ArrayList<Integer>());
        this.direction.put("S", new ArrayList<Integer>());
        this.direction.put("W", new ArrayList<Integer>());
        List<String> dirs = Arrays.asList("N", "E", "S", "W");
        Scanner f = new Scanner(new File(filename));
        this.dim = f.nextInt();
        this.board = new int[dim][dim];
        int index = 0;
        while (f.hasNextInt()) {
            if (index < 4) {
                if (direction.get(dirs.get(index)).size() < dim) {
                    this.direction.get(dirs.get(index)).add(f.nextInt());
                } else {
                    ++index;
                }
            } else {
                int row;
                int col;
                for (row = 0; row < dim; ++row) {
                    for (col = 0; col < dim; ++col) {
                        this.board[row][col] = f.nextInt();
                    }
                }
            }
        }
        f.close();
    }

    /**
     * Copy constructor, contructs an identical instance to the
     * given instance.
     *
     * @param copy SkyscraperConfig instance
     */
    public SkyscraperConfig(SkyscraperConfig copy) {
        this.dim = copy.dim;
        this.currCol = copy.currCol;
        this.currRow = copy.currRow;
        this.board = new int[copy.dim][copy.dim];
        this.direction = copy.direction;
        for (int row=0; row < this.dim; row++) {
            System.arraycopy(copy.board[row], 0, this.board[row],
                    0, this.dim);
        }
    }


    /**
     * Tests if the current configuration a goal.
     *
     * @return true if goal; false otherwise.
     */
    @Override
    public boolean isGoal() {
        return !findEmpty();
    }

    /**
     * Gets the successors of the current board/ placements.
     *
     * @returns Collection of Configurations
     */
    @Override
    public Collection<Configuration> getSuccessors() {
        findEmpty();
        List<Configuration> successors = new LinkedList<Configuration>();
        for (int num=1; num <= this.dim; num++) {
            if (notInRowCol(num)) {
                SkyscraperConfig child = new SkyscraperConfig(this);
                child.board[currRow][currCol] = num;
                successors.add(child);
            }
        }
        return successors;
    }


    /**
     * Searches the board for the next empty spot.
     *
     * @return true: if there is an empty spot,
     * false: if there is not an empty spot.
     */
    private boolean findEmpty() {
        for (int row = 0; row < dim; ++row) {
            for (int col = 0; col < dim; ++col) {
                if ((board[row][col] == EMPTY) || (board[row][col] == EMPTY_CELL)) {
                    this.currRow = row;
                    this.currCol = col;
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Checks if current board configuration is valid.
     *
     * @returns true if config is valid, false otherwise
     */
    @Override
    public boolean isValid() {
        if (direction.get("W").get(currRow) == 1) {
            if ((this.board[currRow][0] != dim) &&
                    (this.board[currRow][0] != EMPTY)) {
                return false;
            }
        }
        if (direction.get("E").get(currRow) == 1) {
            if ((this.board[currRow][dim - 1] != dim) &&
                    (this.board[currRow][dim - 1] != EMPTY)) {
                return false;
            }
        }
        if (direction.get("N").get(currCol) == 1) {
            if ((this.board[0][currCol] != dim) &&
                    (this.board[0][currCol] != EMPTY)) {
                return false;
            }
        }
        if (direction.get("S").get(currCol) == 1) {
            if ((this.board[dim - 1][currCol] != dim) &&
                    (this.board[dim - 1][currCol] != EMPTY)) {
                return false;
            }
        }
        boolean hasEmptyRow = false;
        for (int col=0; col < dim; col++) {
            if (board[currRow][col] == EMPTY) {
                hasEmptyRow = true;
            }
        }
        if (!hasEmptyRow) {
            if (direction.get("W").get(currRow) != numOfMaxW()) {
                return false;
            }
            if (direction.get("E").get(currRow) != numOfMaxE()) {
                return false;
            }
        }
        boolean hasEmptyCol = false;
        for (int row=0; row < dim; row++) {
            if (board[row][currCol] == EMPTY) {
                hasEmptyCol = true;
            }
        }
        if (!hasEmptyCol) {
            if (direction.get("N").get(currCol) != numOfMaxN()) {
                return false;
            }
            if (direction.get("S").get(currCol) != numOfMaxS()) {
                return false;
            }
        }
        return true;
    }


    /**
     * Returns the number of times a maximum was encountered
     * when traversing through the current column from top
     * to bottom (the North direction).
     *
     * @return The number of times a maximum was encountered
     * in the current column.
     */
    private int numOfMaxN() {
        int currMax = 0;
        int numMax = 0;
        for (int row=0; row < dim; row++) {
            if (board[row][currCol] > currMax) {
                numMax++;
                currMax = board[row][currCol];
            }
        }
        return numMax;
    }


    /**
     * Returns the number of times a maximum was encountered
     * when traversing through the current row from left
     * to right (the West direction).
     *
     * @return The number of times a maximum was encountered
     * in the current row.
     */
    private int numOfMaxW() {
        int currMax = 0;
        int numMax = 0;
        for (int num : board[currRow]) {
            if (num > currMax) {
                numMax++;
                currMax = num;
            }
        }
        return numMax;
    }


    /**
     * Returns the number of times a maximum was encountered
     * when traversing through the current column from bottom
     * to top (the South direction).
     *
     * @return The number of times a maximum was encountered
     * in the current column.
     */
    private int numOfMaxS() {
        int currMax = 0;
        int numMax = 0;
        for (int num = dim-1; num >= 0; num--) {
            if (board[num][currCol] > currMax) {
                numMax++;
                currMax = board[num][currCol];
            }
        }
        return numMax;
    }


    /**
     * Returns the number of times a maximum was encountered
     * when traversing through the current row from left
     * to right (the East direction).
     *
     * @return The number of times a maximum was encountered
     * in the current row.
     */
    private int numOfMaxE() {
        int currMax = 0;
        int numMax = 0;
        for (int num = dim-1; num >= 0; num--) {
            if (board[currRow][num] > currMax) {
                numMax++;
                currMax = board[currRow][num];
            }
        }
        return numMax;
    }


    /**
     * Tests if the given number is in the current row or
     * column.
     *
     * @param num The testing number.
     * @return True: given number is not in the current spot's
     * row or column, will otherwise be false.
     */
    private boolean notInRowCol(int num) {
        for (int row=0; row < dim; row++) {
            if (board[row][currCol] == num) {
                return false;
            }
        }
        for (int col=0; col < dim; col++) {
            if (board[currRow][col] == num) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the current board in a string format.
     *
     * @return String representing configuration board & grid w/ look values.
     * The format of the output for the problem solving initial config is:
     *
     *   1 2 4 2
     *   --------
     * 1|. . . .|3
     * 2|. . . .|3
     * 3|. . . .|1
     * 3|. . . .|2
     *   --------
     *   4 2 1 2
     */
    @Override
    public String toString() {
        String res = "  ";
        for (int north=0; north < dim; north++) {
            res += direction.get("N").get(north) + " ";
        }
        res += "\n  ";
        for (int dash=0; dash < dim*2; dash++) {
            res += "-";
        }
        for (int row=0; row < dim; row++) {
            res += "\n" + direction.get("W").get(row) + "|";
            for (int col=0; col < dim; col++) {
                res += board[row][col];
                if (col != dim-1) {
                    res += " ";
                }
            }
            res += "|" + direction.get("E").get(row);
        }
        res += "\n  ";
        for (int dash=0; dash < dim*2; dash++) {
            res += "-";
        }
        res += "\n  ";
        for (int south=0; south < dim; south++) {
            res += direction.get("S").get(south) + " ";
        }
        return res;
    }
}

