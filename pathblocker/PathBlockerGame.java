//21050111023 EFE ŞAHİN
//21050111024 SELİN SARGIN

/*
1) Why you prefer the search algorithm you choose?
We choose BFS(graph version) because if there is a solution for the problem. it guarantees the shortest way. BFS explores all possible ways in a level-by-level
manner. Which ensures that when we reach the goal, we found the shortest posssible way in terms of number of moves.
This is important for this game because we want the make the yellow square(Start square) to reach the goal as less move as possible
Also we used Graph version because the graph version avoids revisiting the same state, making it more time and memory efficient.



2) Can you achieve the optimal result? Why? Why not?
Yes we can, BFS is optimal when the all movements are same cost and this game all movements have same cost. 
Every moment travelling in a line until an block is reached. And every move costs 1 so BFS will find the shortest way to reach the goal.




3) How you achieved efficiency for keeping the states?
To increase efficiency, the current position, grid configuration, and parent state are stored in each case,
this allows us to reconstruct the path once the goal is reached efficiently. 
We also used a HashSet to track visited states by representing each state as a unique string, 
preventing redundant calculations and ensuring each state is processed only once. 
Additionally, as the yellow square moves, the cells it passes over are blocked, 
which narrows the search space and further increases efficiency.



4) If you prefer to use DFS (tree version) then do you need to avoid cycles?
No, there is no need to avoid when using DFS(tree version) because when the yellow square(Start square) moves over a cell, the cell becomes blocked 
and cannot be revisited. So it prevents cycles from forming, each cell can be a part of the path at most once.


5) What will be the path-cost for this problem?
in this problem path-cost is the number of moves made by square to the reach the goal. Every move involves traveling in one direction until a block is hit.
and each move counted as move unit of cost. So we need to less move to decrease the cost and BFS is the best way to do it. it guarentees the shortest way for reach the goal.


*/
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.*;

public class PathBlockerGame {
    private static final int GRIDSIZE = 16;
    private static final char EMPTY = '.';
    private static final char BLOCKED = '#';
    private static final char START = 'S';
    private static final char GOAL = 'G';

    private char[][] grid;
    private Point start, goal;
    private int pngCounter;

    public static void main(String[] args) {
        for (int level = 1; level <= 10; level++) {
            String levelFileName = String.format("level%02d.txt", level);
            PathBlockerGame game = new PathBlockerGame();
            game.loadLevels(levelFileName);
            game.solveLevel(String.format("level%02d", level));
        }
    }

    public PathBlockerGame() {
        grid = new char[GRIDSIZE][GRIDSIZE];
        pngCounter = 1;
    }

    public void loadLevels(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            for (int i = 0; i < GRIDSIZE; i++) {
                String line = reader.readLine();
                if (line == null || line.length() < GRIDSIZE) {
                    throw new IllegalArgumentException(
                            "Each level diagram must have 16 rows and 16 characters per row");
                }
                for (int j = 0; j < GRIDSIZE; j++) {
                    grid[i][j] = line.charAt(j);
                    if (grid[i][j] == START) {
                        start = new Point(i, j);
                    } else if (grid[i][j] == GOAL) {
                        goal = new Point(i, j);
                    }
                }
            }

            if (start == null || goal == null) {
                throw new IllegalStateException(
                        "No S(Start) or G(Goal) found in level file. Please check your file...");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void solveLevel(String levelDirectory) {
        new File(levelDirectory).mkdir();
        saveGridAsPng(grid, start, String.format("%s/%04d.png", levelDirectory, pngCounter++));
        bfs(levelDirectory);
    }

    private void bfs(String levelDirectory) {

        Queue<State> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        State initialState = new State(start, deepCopyGrid(grid), null);
        queue.add(initialState);
        visited.add(stateToString(initialState));

        State goalState = null;

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            Point currentPos = currentState.position;

            if (currentPos.equals(goal)) {
                goalState = currentState;
                break;
            }

            for (Direction dir : Direction.values()) {
                Point nextPosition = moveUntilBlocked(currentPos.x, currentPos.y, dir, currentState.grid);
                if (isWithinBounds(nextPosition) && currentState.grid[nextPosition.x][nextPosition.y] != BLOCKED) {

                    char[][] newGrid = deepCopyGrid(currentState.grid);
                    boolean passedGoal = markPath(currentPos.x, currentPos.y, nextPosition, newGrid);
                    State newState = new State(nextPosition, newGrid, currentState);

                    String stateString = stateToString(newState);
                    if (!visited.contains(stateString)) {
                        visited.add(stateString);

                        if (passedGoal || nextPosition.equals(goal)) {
                            goalState = newState;
                            break;
                        }

                        queue.add(newState);
                    }
                }
            }
            if (goalState != null)
                break;
        }

        if (goalState != null) {

            List<State> path = new ArrayList<>();
            State state = goalState;
            while (state != null) {
                path.add(state);
                state = state.parent;
            }
            Collections.reverse(path);

            for (int i = 1; i < path.size(); i++) {
                State s = path.get(i);
                saveGridAsPng(s.grid, s.position, String.format("%s/%04d.png", levelDirectory, pngCounter++));
            }
        } else {
            System.out.println("Solution couldn't found!");
        }
    }

    private Point moveUntilBlocked(int x, int y, Direction direction, char[][] currentGrid) {
        int newX = x, newY = y;
        while (true) {
            int tempX = newX + direction.dx;
            int tempY = newY + direction.dy;

            if (newX == goal.x && newY == goal.y) {
                break;
            }
            if (tempX < 0 || tempX >= GRIDSIZE || tempY < 0 || tempY >= GRIDSIZE
                    || currentGrid[tempX][tempY] == BLOCKED) {
                break;
            }
            newX = tempX;
            newY = tempY;
        }
        return new Point(newX, newY);
    }

    private boolean markPath(int startX, int startY, Point end, char[][] grid) {
        int x = startX, y = startY;
        boolean passedGoal = false;
        grid[x][y] = BLOCKED;
        while (x != end.x || y != end.y) {

            if (x == goal.x && y == goal.y) {
                passedGoal = true;
            }
            if (x < end.x)
                x++;
            else if (x > end.x)
                x--;
            if (y < end.y)
                y++;
            else if (y > end.y)
                y--;
            grid[x][y] = BLOCKED;
        }
        if (x == goal.x && y == goal.y) {
            passedGoal = true;
        }
        return passedGoal;
    }

    private char[][] deepCopyGrid(char[][] originalGrid) {
        char[][] newGrid = new char[GRIDSIZE][GRIDSIZE];
        for (int i = 0; i < GRIDSIZE; i++) {
            System.arraycopy(originalGrid[i], 0, newGrid[i], 0, GRIDSIZE);
        }
        return newGrid;
    }

    private boolean isWithinBounds(Point p) {
        return p.x >= 0 && p.x < GRIDSIZE && p.y >= 0 && p.y < GRIDSIZE;
    }

    private void saveGridAsPng(char[][] gridToSave, Point yellowPosition, String filename) {
        int cellSize = 10; 
        int imageSize = GRIDSIZE * cellSize;
        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        for (int i = 0; i < GRIDSIZE; i++) {
            for (int j = 0; j < GRIDSIZE; j++) {
                if (i == yellowPosition.x && j == yellowPosition.y) {
                    
                    g.setColor(Color.YELLOW);
                } else {
                    switch (gridToSave[i][j]) {
                        case EMPTY -> g.setColor(Color.WHITE);
                        case BLOCKED -> g.setColor(Color.BLACK);
                        case GOAL -> g.setColor(Color.RED);
                    }
                }
                g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
            }
        }

        try {
            ImageIO.write(image, "png", new File(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String stateToString(State state) {
        StringBuilder sb = new StringBuilder();
        sb.append(state.position.x).append(",").append(state.position.y).append(";");
        for (int i = 0; i < GRIDSIZE; i++) {
            sb.append(new String(state.grid[i]));
        }
        return sb.toString();
    }

    enum Direction {
        UP(-1, 0), DOWN(1, 0), LEFT(0, -1), RIGHT(0, 1);

        int dx, dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }
    }

    private class State {
        Point position;
        char[][] grid;
        State parent;

        State(Point position, char[][] grid, State parent) {
            this.position = position;
            this.grid = grid;
            this.parent = parent;
        }
    }
}
