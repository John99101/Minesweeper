import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Minesweeper {
    private static final int CELL_SIZE = 30;
    private static final double EASY_MINE_PERCENTAGE = 0.1;
    private static final double MEDIUM_MINE_PERCENTAGE = 0.15;
    private static final double HARD_MINE_PERCENTAGE = 0.2;
    private static final String SCORE_FILE = System.getProperty("user.home") + File.separator + "scores.dat";

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
    private String playerName;

    public Minesweeper(int size, int mines, int timeLimit, String playerName) {
        this.size = size;
        this.mines = mines;
        this.timeLimit = timeLimit;
        this.playerName = playerName;

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
        revealAllMines();
        saveScore();
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
        } else if (choice == JOptionPane.NO_OPTION) {
            gameFrame.dispose();
            new Minesweeper(size, mines, timeLimit, playerName);
        }
    }

    private void gameWon() {
        timer.stop();
        saveScore();
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
        } else if (choice == JOptionPane.NO_OPTION) {
            gameFrame.dispose();
            new Minesweeper(size, mines, timeLimit, playerName);
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
        int points = (timeLimit - timeElapsed) * lives;
        List<Score> scores = loadScores();
        scores.add(new Score(playerName, points));
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORE_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {  e.printStackTrace();
        }
    }

    private static List<Score> loadScores() {
        List<Score> scores = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORE_FILE))) {
            scores = (List<Score>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return scores;
    }

    private static void showMenu() {
        JFrame menuFrame = new JFrame("Minesweeper Menu");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setLayout(new GridLayout(6, 2));

        JLabel nameLabel = new JLabel("Enter your name:");
        JTextField nameField = new JTextField();

        JLabel sizeLabel = new JLabel("Select size:");
        JComboBox<String> sizeComboBox = new JComboBox<>(new String[]{"Small", "Medium", "Large"});

        JLabel difficultyLabel = new JLabel("Select difficulty:");
        JComboBox<String> difficultyComboBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});

        JLabel timeLimitLabel = new JLabel("Select time limit:");
        JComboBox<Integer> timeLimitComboBox = new JComboBox<>(new Integer[]{90, 180, 270});

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> {
            String playerName = nameField.getText().trim();
            String selectedSize = (String) sizeComboBox.getSelectedItem();
            String selectedDifficulty = (String) difficultyComboBox.getSelectedItem();
            int selectedTimeLimit = (int) timeLimitComboBox.getSelectedItem();

            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(menuFrame, "Please enter your name.");
                return;
            }

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
                    mines = (int) (size * size * EASY_MINE_PERCENTAGE);
                    timeLimit = selectedTimeLimit;
                    break;
                case "Medium":
                    mines = (int) (size * size * MEDIUM_MINE_PERCENTAGE);
                    timeLimit = (int) (selectedTimeLimit * 0.75);
                    break;
                case "Hard":
                    mines = (int) (size * size * HARD_MINE_PERCENTAGE);
                    timeLimit = (int) (selectedTimeLimit * 0.5);
                    break;
            }

            menuFrame.dispose();
            new Minesweeper(size, mines, timeLimit, playerName);
        });

        JTextArea scoreArea = new JTextArea();
        scoreArea.setEditable(false);
        JScrollPane scoreScrollPane = new JScrollPane(scoreArea);
        scoreScrollPane.setBorder(BorderFactory.createTitledBorder("High Scores"));
        List<Score> scores = loadScores();
        scores.sort((s1, s2) -> Integer.compare(s2.points, s1.points)); // Sort by points descending
        for (Score score : scores) {
            scoreArea.append(score.toString() + "\n");
        }

        menuFrame.add(nameLabel);
        menuFrame.add(nameField);
        menuFrame.add(sizeLabel);
        menuFrame.add(sizeComboBox);
        menuFrame.add(difficultyLabel);
        menuFrame.add(difficultyComboBox);
        menuFrame.add(timeLimitLabel);
        menuFrame.add(timeLimitComboBox);
        menuFrame.add(new JLabel());
        menuFrame.add(startButton);
        menuFrame.add(scoreScrollPane); // Add the score display

        menuFrame.pack();
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setVisible(true);
    }

    public static void main(String[] args) {
        showMenu();
    }
}

class Cell {
    private boolean isMine;
    private boolean isRevealed;

    public Cell(boolean isMine) {
        this.isMine = isMine;
        this.isRevealed = false;
    }

    public boolean isMine() {
        return isMine;
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public void reveal(JButton button) {
        this.isRevealed = true;
        if (isMine) {
            button.setBackground(Color.RED);
            button.setText("X");
        }
    }
}

class SafeCell extends Cell {
    private int adjacentMines;

    public SafeCell() {
        super(false);
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }

    @Override
    public void reveal(JButton button) {
        super.reveal(button);
        if (!isMine() && isRevealed()) {
            button.setBackground(Color.WHITE);
            button.setText(adjacentMines > 0 ? Integer.toString(adjacentMines) : "");
        }
    }
}

class MineCell extends Cell {
    public MineCell() {
        super(true);
    }
}

class Score implements Serializable {
    String playerName;
    int points;

    public Score(String playerName, int points) {
        this.playerName = playerName;
        this.points = points;
    }

    @Override
    public String toString() {
        return playerName + ": " + points + " points";
    }
}