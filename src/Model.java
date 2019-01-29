import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    protected int score;
    protected int maxTile;
    private Stack<Integer> previousScores= new Stack<>();
    private Stack<Tile[][]> previousStates = new Stack<>();
    private boolean isSaveNeeded = true;

    private void saveState(Tile[][] tiles) {
        Tile[][] result = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                result[i][j] = new Tile(tiles[i][j].value);
            }
        }

        previousStates.push(result);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousScores.empty() && !previousStates.empty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }


    public Model() {
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();

        if (emptyTiles.size() > 0)
            emptyTiles.get((int) (Math.random() * emptyTiles.size())).value = (Math.random() < 0.9 ? 2 : 4);
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTiles = new ArrayList<>();

        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value == 0)
                    emptyTiles.add(gameTiles[i][j]);
            }
        }
        return emptyTiles;
    }

    protected void resetGameTiles() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];

        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
        score = 0;
        maxTile = 0;
    }


    private boolean compressTiles(Tile[] tiles) {
        boolean flag = false;

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < FIELD_WIDTH - 1; i++) {
                if (tiles[i].value == 0 && tiles[i + 1].value > 0) {
                    tiles[i].value = tiles[i + 1].value;
                    tiles[i + 1].value = 0;
                    flag = true;
                }
            }
        }
        return flag;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean flag = false;

        for (int i = 0; i < FIELD_WIDTH - 1; i++) {
            if (tiles[i].value != 0 && tiles[i].value == tiles[i + 1].value) {
                tiles[i].value *= 2;
                tiles[i + 1] = new Tile();

                flag = true;
                score += tiles[i].value;
                if (tiles[i].value > maxTile)
                    maxTile = tiles[i].value;
            }
        }
        compressTiles(tiles);

        return flag;
    }

    public void left() {
        if (isSaveNeeded)
            saveState(gameTiles);

        boolean flag = false;

        for (int i = 0; i < FIELD_WIDTH; i++) {
            if (compressTiles(gameTiles[i]) | mergeTiles(gameTiles[i]))
                flag = true;
        }
        if (flag)
            addTile();

        isSaveNeeded = true;
    }

    public void down() {
        saveState(gameTiles);

        rotate90Clockwise(gameTiles);

        left();

        rotate90Clockwise(gameTiles);
        rotate90Clockwise(gameTiles);
        rotate90Clockwise(gameTiles);
    }

    public void up() {
        saveState(gameTiles);

        rotate90Clockwise(gameTiles);
        rotate90Clockwise(gameTiles);
        rotate90Clockwise(gameTiles);

        left();

        rotate90Clockwise(gameTiles);
    }

    public void right() {
        saveState(gameTiles);

        rotate90Clockwise(gameTiles);
        rotate90Clockwise(gameTiles);

        left();

        rotate90Clockwise(gameTiles);
        rotate90Clockwise(gameTiles);
    }

    private static void rotate90Clockwise(Tile a[][]) {
        int N = 4;
        // Traverse each cycle
        for (int i = 0; i < N / 2; i++) {
            for (int j = i; j < N - i - 1; j++) {

                // Swap elements of each cycle
                // in clockwise direction
                int temp = a[i][j].value;
                a[i][j].value = a[N - 1 - j][i].value;
                a[N - 1 - j][i].value = a[N - 1 - i][N - 1 - j].value;
                a[N - 1 - i][N - 1 - j].value = a[j][N - 1 - i].value;
                a[j][N - 1 - i].value = temp;
            }
        }
    }

    public boolean canMove() {
        if (getEmptyTiles().size() > 0)
            return true;

        for (int i = 0; i < FIELD_WIDTH - 1; i++) {
            for (int j = 0; j < FIELD_WIDTH - 1; j++) {
                if (gameTiles[i][j].value == gameTiles[i][j + 1].value)
                    return true;
            }
        }

        for (int i = 0; i < FIELD_WIDTH - 1; i++) {
            for (int j = 0; j < FIELD_WIDTH - 1; j++) {
                if (gameTiles[j][i].value == gameTiles[j + 1][i].value)
                    return true;
            }
        }
        return false;
    }

    public void randomMove() {
        int  n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case 0 :
                left();
                break;
            case 1 :
                right();
                break;
            case 2 :
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    boolean hasBoardChanged() {
        Tile[][] prevBoard = previousStates.peek();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (prevBoard[i][j].value != gameTiles[i][j].value)
                    return true;
            }
        }

        return false;
    }

    MoveEfficiency getMoveEfficiency(Move move) {
        move.move();

        MoveEfficiency efficiency = null;
        if (!hasBoardChanged())
            efficiency = new MoveEfficiency(-1, 0, move);
        else
            efficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        rollback();

        return efficiency;
    }

    void autoMove() {
        PriorityQueue queue = new PriorityQueue(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(this::left));
        queue.offer(getMoveEfficiency(this::right));
        queue.offer(getMoveEfficiency(this::up));
        queue.offer(getMoveEfficiency(this::down));

        MoveEfficiency eff = (MoveEfficiency)queue.poll();
        Move move = eff.getMove();
        move.move();
    }
}