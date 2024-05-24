import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Random;

public class Minesweeper {
    private static final int CELL_SIZE = 30;
    private int size;
    private int mines;
    private int lives = 3;
    private int timeLimit;

    private JButton[][] buttons;
    private Cell[][] cells;
    private boolean[][] revealed;
    private boolean[][] marked;
    private Timer timer;
    private int timeElapsed = 0;
    private JLabel timerLabel;
    private JLabel livesLabel;
    private JFrame gameFrame;

    public Minesweeper(int size, int mines, int timeLimit) {
        this.size = size;
        this.mines = mines;
        this.timeLimit = timeLimit;

        gameFrame = new JFrame("Minesweeper");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        livesLabel = new JLabel("Lives: " + lives);
        timerLabel = new JLabel("Time: " + timeElapsed);
        topPanel.add(livesLabel);
        topPanel.add(timerLabel);
        gameFrame.add(topPanel, BorderLayout.NORTH);

        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(size, size));
        buttons = new JButton[size][size];
        cells = new Cell[size][size];
        revealed = new boolean[size][size];
        marked = new boolean[size][size];
        initializeBoard(gamePanel);
        placeMines();
        calculateNumbers();
        startTimer();
        gameFrame.add(gamePanel, BorderLayout.CENTER);

        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null); // Center the window
        gameFrame.setVisible(true);
    }

    private void initializeBoard(JPanel panel) {
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
                cells[i][j] = new SafeCell();
            }
        }
    }

    private void placeMines() {
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

    private void calculateNumbers() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (!cells[i][j].isMine()) {
                    int count = countAdjacentMines(i, j);
                    ((SafeCell) cells[i][j]).setAdjacentMines(count);
                }
            }
        }
    }

    private void reveal(int x, int y) {
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

    private void mark(int x, int y) {
        if (revealed[x][y]) {
            return;
        }
        marked[x][y] = !marked[x][y];
        buttons[x][y].setText(marked[x][y] ? "M" : "");
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
        Object[] options = {"Return to Menu", "Restart"};
        int choice = JOptionPane.showOptionDialog(gameFrame,
                "Game Over! You have no lives left. What do you want to do?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == JOptionPane.YES_OPTION) {
            gameFrame.dispose();
            showMenu();
        } else {
            gameFrame.dispose();
            showMenu();
        }
    }

    private void gameWon() {
        timer.stop();
        Object[] options = {"Return to Menu", "Restart"};
        int choice = JOptionPane.showOptionDialog(gameFrame,
                "Congratulations! You won! What do you want to do?",
                "Congratulations",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == JOptionPane.YES_OPTION) {
            gameFrame.dispose();
            showMenu();
        } else {
            gameFrame.dispose();
            showMenu();
        }
    }

    private void startTimer() {
        timer = new Timer(1000, e -> {
            timeElapsed++;
            timerLabel.setText("Time: " + timeElapsed);
            if (timeElapsed >= timeLimit) {
                timer.stop();
                gameOver();
            }
        });
        timer.start();
    }

    private static void showMenu() {
        JFrame menuFrame = new JFrame("Minesweeper Menu");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLayout(new GridLayout(4, 2));

        JLabel sizeLabel = new JLabel("Select size:");
        JComboBox<String> sizeComboBox = new JComboBox<>(new String[]{"Small", "Medium", "Large"});

        JLabel difficultyLabel = new JLabel("Select difficulty:");
        JComboBox<String> difficultyComboBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});

        JLabel timeLimitLabel = new JLabel("Select time limit:");
        JComboBox<Integer> timeLimitComboBox = new JComboBox<>(new Integer[]{90, 180, 270});

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> {
            String selectedSize = (String) sizeComboBox.getSelectedItem();
            String selectedDifficulty = (String) difficultyComboBox.getSelectedItem();
            int selectedTimeLimit = (int) timeLimitComboBox.getSelectedItem();

            int size = 0;
            int mines = 0;
            int timeLimit = 0;

            switch (selectedSize) {
                case "Small":
                    size = 8;
                    break;
                case "Medium":
                    size = 12;
                    break;
                case "Large":
                    size = 16;
                    break;
            }

            switch (selectedDifficulty) {
                case "Easy":
                    mines = (int) (size * size * 0.1); // 10% of the grid are mines
                    timeLimit = selectedTimeLimit;
                    break;
                case "Medium":
                    mines = (int) (size * size * 0.15); // 15% of the grid are mines
                    timeLimit = (int) (selectedTimeLimit * 0.75); // Reduce time limit for medium difficulty
                    break;
                case "Hard":
                    mines = (int) (size * size * 0.2); // 20% of the grid are mines
                    timeLimit = (int) (selectedTimeLimit * 0.5); // Reduce time limit for hard difficulty
                    break;
            }

            menuFrame.dispose();
            new Minesweeper(size, mines, timeLimit);
        });

        menuFrame.add(sizeLabel);
        menuFrame.add(sizeComboBox);
        menuFrame.add(difficultyLabel);
        menuFrame.add(difficultyComboBox);
        menuFrame.add(timeLimitLabel);
        menuFrame.add(timeLimitComboBox);
        menuFrame.add(new JLabel()); // Empty label for layout
        menuFrame.add(startButton);

        menuFrame.pack();
        menuFrame.setLocationRelativeTo(null); // Center the window
        menuFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Minesweeper::showMenu);
    }
}

abstract class Cell implements Serializable {
    abstract void reveal(JButton button);
    abstract boolean isMine();
}

class MineCell extends Cell {
    @Override
    void reveal(JButton button) {
        button.setText("X"); // Show 'X' for bombs
        button.setBackground(Color.RED);
    }

    @Override
    boolean isMine() {
        return true;
    }
}

class SafeCell extends Cell {
    private int adjacentMines;
    private boolean revealed;

    void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }

    @Override
    void reveal(JButton button) {
        if (!revealed) {
            revealed = true;
            if (adjacentMines > 0) {
                button.setText(String.valueOf(adjacentMines));
            }
            button.setBackground(Color.WHITE);
        }
    }

    @Override
    boolean isMine() {
        return false;
    }
}