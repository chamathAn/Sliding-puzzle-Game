import java.io.*;
import java.util.*;
import java.util.stream.IntStream;
/*
IIT No: 20211463
UoW No: w1957959
Name : Munagamage Chamath Anjula Munasinghe
 */

/**
 * @author Munagamage Chamath Anjula Munasinghe <munagamage.20211463 @ iit.ac.lk>
 * @version "21.0.2"
 * @since "21.0.2"
 */
public class Main {

    private static Queue<Integer> rowQueue = new LinkedList<>();            //  Queue for rows
    private static Queue<Integer> colQueue = new LinkedList<>();            //  Queue for columns
    private static final int[] ROW_DIRECTIONS = new int[]{-1, +1, 0, 0};    //  Array of directions for row
    private static final int[] COL_DIRECTIONS = {0, 0, -1, +1};             //  Array of directions for row
    private static String[][] matrix;                                       //  Matrix as adjacency list
    private static Boolean[][] isVisited;                                   //  Node checking array
    private static int[][] rowsOfParentNodes;                               //  Array of rows of parent nodes
    private static int[][] colsOfParentNodes;                               //  Array of columns of parent nodes

    /**
     * Program begins in main method.
     * <p>
     * First it checks input files
     * availability.
     * <p>
     */
    public static void main(String[] args) {

        File files = new File(System.getProperty("user.dir") + "/src/benchmark_series");

        if (files.exists() && files.isDirectory()) {
            File[] fileList = files.listFiles();
            continueWithFile(fileList);
        } else {
            System.out.println("╭─────────────────────╮");
            System.out.println("│   \033[0;31mFiles Not Found\u001B[0m   │");
            System.out.println("╰─────────────────────╯");
        }
    }

    /**
     * continue on file processing to display the output.
     * <p>
     * For each file, it reads the file content and map it to
     * an adjacency matrix using array data structure.
     * pass it to find the shortest path
     * of its content. And display the output
     * <p>
     * <p>
     * After displaying the output it reset the all instance variables for next file.
     * </p>
     *
     * @param fileList : all the files in directory
     * @throws IOException      : catch all failures of the I/O operations in file handling
     * @throws RuntimeException : Construct an exception for runtime error with the details of cause of its error
     */
    private static void continueWithFile(File[] fileList) {
        for (File file : fileList) {
            try {

                LineNumberReader reader = new LineNumberReader(new FileReader(file));
                String content;
                int width = 0;  //  width of matrix
                while ((content = reader.readLine()) != null) { //  to get the width of matrix
                    if (width == 0) width = content.split("").length;

                }


                int height = reader.getLineNumber();    //  height of matrix

                int startingRow = -1;
                int startingCol = -1;
                int endingRow = -1;
                int endingCol = -1;

                // Reset reader to read from the beginning of the file
                reader = new LineNumberReader(new FileReader(file));
                content = reader.readLine();
                matrix = new String[height][width];

                while (content != null) {
                    String[] chars = content.split("");
                    int currentRow = reader.getLineNumber() - 1;
                    for (int y = 0; y < chars.length; y++) {

                        //  Mark the beginning node
                        if (chars[y].equalsIgnoreCase("s")) {
                            startingRow = currentRow;
                            startingCol = y;
                        }

                        //  Mark the ending node
                        if (chars[y].equalsIgnoreCase("f")) {
                            endingRow = currentRow;
                            endingCol = y;
                        }

                        matrix[currentRow][y] = chars[y];   // Map the file content

                    }
                    content = reader.readLine();
                }

                reader.close();
                displayOutput(solveSlidingPuzzle(startingRow, startingCol, endingRow, endingCol, height, width),
                        file.getName());

            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {

                //  Reset all instance variables for next file
                rowQueue.clear();
                colQueue.clear();
                for (String[] s : matrix) {
                    Arrays.fill(s, null);
                }
                for (Boolean[] b : isVisited) {
                    Arrays.fill(b, false);
                }
                IntStream.range(0, isVisited.length).forEach(i -> Arrays.fill(colsOfParentNodes[i], -1));
                IntStream.range(0, isVisited.length).forEach(i -> Arrays.fill(rowsOfParentNodes[i], -1));
            }


        }
    }

    /**
     * Display the output.
     * <p>
     * Display the path from starting vertex to ending vertex with stylish manner.
     * <p>
     *
     * @param path     : A list contains path from starting vertex to ending vertex.
     * @param fileName : A string that contains name of the current read file.
     */
    private static void displayOutput(List<String> path, String fileName) {

        System.out.println("╭───────────────────────────────╮");
        int neededWhiteSpace = (32 - fileName.length()) / 2;
        String padding = (String.format("%" + neededWhiteSpace + "s", ""));
        System.out.println("│" + padding + fileName + padding + "│");
        System.out.println("╰───────────────────────────────╯");
        for (int i = 0; i < path.size(); i++) {
            System.out.println("0" + (i + 1) + ". " + path.get(i));
        }
        System.out.println("╰───────────────────────────────╯");
    }

    /**
     * Solve the puzzle already mapped to array.
     * <p>
     * Used Breadth First Search Algorithm for this task.
     * Check the every node until reaches to end of puzzle
     * which is "F".
     * <p>
     * <p>
     * Maintains two queues to keep track of recodes branches(contains nodes),
     * 2D array to keep track whether node is visited or not.
     * </p>
     * <p>
     * Continue to find the nodes in next branch.
     * </p>
     *
     * @param startingRow     Row of the node that begin withs to find the path which is "S".
     * @param startingCol     Column of the node that begin withs to find the path which is "S".
     * @param endingRow       Row of the node that end the path finding process which is "F".
     * @param endingCol       Column of the node that end the path finding process which is "F".
     * @param rowSizeOfMatrix Size of the rows(height) of matrix.
     * @param colSizeOfMatrix Size of the columns(width) of matrix.
     * @return path List that contains path from starting node to ending node.
     */
    private static List<String> solveSlidingPuzzle(int startingRow, int startingCol, int endingRow, int endingCol,
                                                   int rowSizeOfMatrix, int colSizeOfMatrix) {
        List<String> path = new ArrayList<>();
        int nodesInNextLayer = 0;

        boolean isReachedEnd = false;
        isVisited = new Boolean[rowSizeOfMatrix][colSizeOfMatrix];
        for (int i = 0; i < rowSizeOfMatrix; i++) {
            Arrays.fill(isVisited[i], false);
        }
        rowsOfParentNodes = new int[rowSizeOfMatrix][colSizeOfMatrix];
        colsOfParentNodes = new int[rowSizeOfMatrix][colSizeOfMatrix];

        // Mark the starting node to begin path finding
        rowQueue.offer(startingRow);
        colQueue.offer(startingCol);
        isVisited[startingRow][startingCol] = true;

        while (!rowQueue.isEmpty()) {

            // Dequeue node from queues to take further steps
            int rowOfDequeuedNode = rowQueue.poll();
            int colOfDequeuedNode = colQueue.poll();

            if (rowOfDequeuedNode == endingRow && colOfDequeuedNode == endingCol) { //  Check if node is reaches to end
                isReachedEnd = true;
                break;
            }

            //  Find nodes in next branch
            exploreNeighbours(rowOfDequeuedNode, colOfDequeuedNode, rowSizeOfMatrix, colSizeOfMatrix, nodesInNextLayer);

        }

        //  wrap and return processed path
        if (isReachedEnd) path.addAll(processThePath(startingRow, startingCol, endingRow, endingCol));
        return path;
    }

    /**
     * Process the path from starting node to ending node.
     * <p>
     * Read two 2D arrays contains rows and columns of each parent
     * nodes' rows and columns of current node that queuing from queue.
     * <p>
     * <p>
     * Process the path.
     * </p>
     *
     * @param startingRow Row of the node that begin withs to find the path which is "S".
     * @param startingCol Column of the node that begin withs to find the path which is "S".
     * @param endingRow   Row of the node that end the path finding process which is "F".
     * @param endingCol   Column of the node that end the path finding process which is "F".
     * @return path List that contains path from starting node to ending node.
     */
    private static List<String> processThePath(int startingRow, int startingCol, int endingRow, int endingCol) {
        List<String> path = new ArrayList<>();
        int rowOfCurrentNode = endingRow;
        int colOfCurrentNode = endingCol;
        path.add("Done!");
        path.add("Ends at \u001B[1;34m(" + (endingRow + ", " + endingCol) + ")\u001B[0m");

        //  Loop till checking node becomes starting node
        while (rowOfCurrentNode != startingRow || colOfCurrentNode != startingCol) {
            int rowOfParentNode = rowsOfParentNodes[rowOfCurrentNode][colOfCurrentNode];
            int colOfParentNode = colsOfParentNodes[rowOfCurrentNode][colOfCurrentNode];

            //  Check the directions and add to path
            if (rowOfCurrentNode > rowOfParentNode)
                path.add("Move \u001B[1m\u001B[38;2;0;127;255mDown\u001B[0m to " + "\u001B[1;34m(" + rowOfParentNode +
                        ", " + colOfParentNode + ")\u001B[0m");
            else if (rowOfParentNode > rowOfCurrentNode)
                path.add("Move \u001B[1m\u001B[38;2;0;127;255mUp\u001B[0m to " + "\u001B[1;34m(" + rowOfParentNode +
                        ", " + colOfParentNode + ")\u001B[0m");
            else if (colOfParentNode < colOfCurrentNode)
                path.add("Move \u001B[1m\u001B[38;2;0;127;255mRight\u001B[0m to " + "\u001B[1;34m(" + rowOfParentNode +
                        ", " + colOfParentNode + ")\u001B[0m");
            else if (colOfParentNode > colOfCurrentNode)
                path.add("Move \u001B[1m\u001B[38;2;0;127;255mLeft\u001B[0m to " + "\u001B[1;34m(" + rowOfParentNode +
                        ", " + colOfParentNode + ")\u001B[0m");

            rowOfCurrentNode = rowOfParentNode;
            colOfCurrentNode = colOfParentNode;
        }
        path.add("Start at \u001B[1;34m(" + (startingRow + ", " + startingCol) + ")\u001B[0m");
        Collections.reverse(path);
        return path;
    }

    /**
     * Find the neighbour nodes in next branch in adjacency matrix.
     * <p>
     * Search next valid neighbour nodes and add it them to queues.
     * Mark the isVisited array as the node is visited.
     * <p>
     * <p>
     * add parent nodes(dequeued nodes) rows and columns into two 2D arrays
     * on corresponding child nodes(new nodes) positions in those 2D arrays
     * </p>
     *
     * @param rowOfDequeuedNode row of parent nodes(dequeued node)
     * @param colOfDequeuedNode column of parent nodes(dequeued node)
     * @param rowSizeOfMatrix   Size of the rows(height) of matrix.
     * @param colSizeOfMatrix   Size of the columns(width) of matrix.
     */
    private static void exploreNeighbours(int rowOfDequeuedNode, int colOfDequeuedNode, int rowSizeOfMatrix,
                                          int colSizeOfMatrix, int nodesLeftInLayer) {
        int rowOfNewNeighbour;
        int colOfNewNeighbour;
        for (int i = 0; i < 4; i++) {

            rowOfNewNeighbour = rowOfDequeuedNode + ROW_DIRECTIONS[i];
            colOfNewNeighbour = colOfDequeuedNode + COL_DIRECTIONS[i];

            //  if the new nodes out of bounds
            if (rowOfNewNeighbour < 0 || rowOfNewNeighbour >= rowSizeOfMatrix || colOfNewNeighbour < 0 ||
                    colOfNewNeighbour >= colSizeOfMatrix)
                continue;

            // skip visited nodes or rocks in matrix
            if (isVisited[rowOfNewNeighbour][colOfNewNeighbour]) continue;
            if (matrix[rowOfNewNeighbour][colOfNewNeighbour].equals("0")) continue;

            //  keep track new nodes in queues and visibility
            rowQueue.offer(rowOfNewNeighbour);
            colQueue.offer(colOfNewNeighbour);
            isVisited[rowOfNewNeighbour][colOfNewNeighbour] = true;

            //  keep track records of parent info on arrays' corresponding position of current node
            rowsOfParentNodes[rowOfNewNeighbour][colOfNewNeighbour] = rowOfDequeuedNode;
            colsOfParentNodes[rowOfNewNeighbour][colOfNewNeighbour] = colOfDequeuedNode;
            nodesLeftInLayer++;

        }
    }


}
