import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.List;

public class Board {
    private static final int CELL_SIZE = 30;
    private int size;
    private int mines;
    private int lives;
    private JButton[][] buttons;
    private Cell[][] cells;
    private boolean[][] revealed;
    private boolean[][] marked;
    private int timeElapsed;
    private Timer timer;
    private JLabel timerLabel;
    private JLabel livesLabel;
    private String playerName;

    public Board(int size, int mines, int lives, JLabel timerLabel, JLabel livesLabel, String playerName) {
        this.size = size;
        this.mines = mines;
        this.lives = lives;
        this.timerLabel = timerLabel;
        this.livesLabel = livesLabel;
        this.playerName = playerName;
        this.buttons = new JButton[size][size];
        this.cells = new Cell[size][size];
        this.revealed = new boolean[size][size];
        this.marked = new boolean[size][size];
    }

    public void initializeBoard(JPanel panel) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                button.setBackground(Color.GRAY);
                button.setFocusPainted(false);
                int row = i;
                int col = j;
                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            reveal(row, col);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            mark(row, col);
                        }
                    }
                });
                panel.add(button);
                buttons[i][j] = button;
                cells[i][j] = new SafeCell(); // Default to SafeCell, mines will be placed later
            }
        }
    }

    public void placeMines() {
        Random random = new Random();
        int count = 0;
        while (count < mines) {
            int x = random.nextInt(size);
            int y = random.nextInt(size);
            if (!cells[x][y].isMine()) {
                cells[x][y] = new MineCell();
                count++;
            }
        }
    }

    public void calculateNumbers() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!cells[i][j].isMine()) {
                    int count = countAdjacentMines(i, j);
                    ((SafeCell) cells[i][j]).setAdjacentMines(count);
                }
            }
        }
    }

    public void reveal(int x, int y) {
        if (revealed[x][y] || marked[x][y]) {
            return;
        }
        revealed[x][y] = true;
        cells[x][y].reveal(buttons[x][y]);

        if (cells[x][y].isMine()) {
            lives--;
            livesLabel.setText("Lives: " + lives);
            if (lives == 0) {
                gameOver();
                return;
            }
        } else {
            int count = countAdjacentMines(x, y);
            if (count == 0) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < size && ny >= 0 && ny < size && !revealed[nx][ny]) {
                            reveal(nx, ny);
                        }
                    }
                }
            }
        }

        if (isGameWon()) {
            gameWon();
        }
    }

    public void mark(int x, int y) {
        if (revealed[x][y]) {
            return;
        }
        marked[x][y] = !marked[x][y];
        if (marked[x][y]) {
            ImageIcon icon = new ImageIcon(getClass().getResource("images/flag.png"));
            Image img = icon.getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_SMOOTH);
            buttons[x][y].setIcon(new ImageIcon(img));
        } else {
            buttons[x][y].setIcon(null);
        }
    }

    private int countAdjacentMines(int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < size && ny >= 0 && ny < size && cells[nx][ny].isMine()) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean isGameWon() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!cells[i][j].isMine() && !revealed[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void gameOver() {
        timer.stop();
        revealAllMines();
        saveScore();
        Object[] options = {"Return to Menu", "Restart"};
        int choice = JOptionPane.showOptionDialog(null,
                "Game Over! You have no lives left. What do you want to do?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == JOptionPane.YES_OPTION) {
            showMenu();
        } else if (choice == JOptionPane.NO_OPTION) {
            restartGame();
        }
    }

    private void gameWon() {
        timer.stop();
        saveScore();
        Object[] options = {"Return to Menu", "Restart"};
        int choice = JOptionPane.showOptionDialog(null,
                "Congratulations! You won! What do you want to do?",
                "Congratulations",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == JOptionPane.YES_OPTION) {
            showMenu();
        } else if (choice == JOptionPane.NO_OPTION) {
            restartGame();
        }
    }

    public void startTimer(int timeLimit) {
        timeElapsed = timeLimit;
        timer = new Timer(1000, e -> {
            timeElapsed--;
            timerLabel.setText("Time: " + timeElapsed);
            if (timeElapsed <= 0) {
                timer.stop();
                gameOver();
            }
        });
        timer.start();
    }

    private void revealAllMines() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j].isMine()) {
                    cells[i][j].reveal(buttons[i][j]);
                }
            }
        }
    }

    private void saveScore() {
        int points = (timeElapsed) * lives;
        List<Score> scores = Score.loadScores();
        scores.add(new Score(playerName, points));
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Minesweeper.SCORE_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restartGame() {
    	Minesweeper.gameFrame.dispose();
        new Minesweeper(size, mines, timeElapsed, playerName);
    }

    private void showMenu() {
    	Minesweeper.gameFrame.dispose();

        Minesweeper.showMenu();
    }
}
