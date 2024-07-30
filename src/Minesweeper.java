import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.List;
import java.util.Random;

public class Minesweeper {
    private static final int CELL_SIZE = 30;  // Zellgröße in UI 
    private static final double EASY_MINE_PERCENTAGE = 0.1; // Prozentsatz wie viele Felder Minen sind bei Schwierigkeit easy
    private static final double MEDIUM_MINE_PERCENTAGE = 0.15; // Prozentsatz wie viele Felder Minen sind bei Schwierigkeit medium
    private static final double HARD_MINE_PERCENTAGE = 0.2; // Prozentsatz wie viele Felder Minen sind bei Schwierigkeit hard
    private static final String SCORE_FILE = System.getProperty("user.home") + File.separator + "scores.dat"; // Pfad zur Scorefile(zum speichern der scores)

    private int size; // Feldgröße
    private int mines; // Anzahl der Minen
    private int lives = 3; // Anzahl der Leben 
    private int timeLimit; // Zeitlimit
    private JButton[][] buttons; // swing button initialisierer (um buttons zu erstellen)
    private Cell[][] cells; // swing Cell initialisierer (um Zellen abzufragen)
    private boolean[][] revealed; // angeklickte Felder 
    private boolean[][] marked; // markierte Felder 
    private Timer timer; // initialisierung Timer 
    private int timeElapsed = 0; // wieviel Zeit vergangen ist in Sekunden
    private JLabel timerLabel; // Label für Timer initialisieren
    private JLabel livesLabel; // Label für Leben initialisieren
    private JFrame gameFrame; // gameFrame initialisieren(der Rahmen in dem die UI ist)
    private String playerName; // Spielername

    public Minesweeper(int size, int mines, int timeLimit, String playerName) {  // Konstruktor
        this.size = size;
        this.mines = mines;
        this.timeLimit = timeLimit;
        this.playerName = playerName;

        gameFrame = new JFrame("Minesweeper");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // closed den Gameframe sobald man das Fenster schließt
        gameFrame.setLayout(new BorderLayout()); 

        JPanel topPanel = new JPanel(); 
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10)); // oberes Feld
        livesLabel = new JLabel("Lives: " + lives); // Label für leben
        timerLabel = new JLabel("Time: " + timeElapsed);  // Label für Zeit
        topPanel.add(livesLabel); // hinzufügen der panels zum definierten feld 
        topPanel.add(timerLabel); // hinzufügen der panels zum definierten feld 
        gameFrame.add(topPanel, BorderLayout.NORTH); // Panel zum gameframe hinzufügen

        JPanel gamePanel = new JPanel();
        gamePanel.setLayout(new GridLayout(size, size)); // setzt das Spielfeld auf die Größe
        buttons = new JButton[size][size]; 
        cells = new Cell[size][size];
        revealed = new boolean[size][size];
        marked = new boolean[size][size];
        initializeBoard(gamePanel); // initialisieren den Spielfelds
        placeMines(); // platzieren der Minen
        calculateNumbers(); // Zählt wieviele Minen neben Feld
        startTimer(); // Timer starten
        gameFrame.add(gamePanel, BorderLayout.CENTER); 
        gameFrame.setSize(new Dimension(800, 800));;
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
                cells[i][j] = new SafeCell(); // Default to SafeCell, mines will be placed later
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
        cells[x][y].reveal(buttons[x][y]); // Reveal the cell

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
        if (marked[x][y]) {
        	ImageIcon icon = new ImageIcon(getClass().getResource("images/flag.png"));
        	Image img= icon.getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_SMOOTH);
        	buttons[x][y].setIcon( new ImageIcon(img));
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
        int points = (timeLimit - timeElapsed) * lives;
        List<Score> scores = Score.loadScores();
        scores.add(new Score(playerName, points));
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORE_FILE))) {
            oos.writeObject(scores);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> {
            String playerName = nameField.getText().trim();
            String selectedSize = (String) sizeComboBox.getSelectedItem();
            String selectedDifficulty = (String) difficultyComboBox.getSelectedItem();

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
                    timeLimit = getAdjustedTimeLimit("Easy", selectedSize);
                    break;
                case "Medium":
                    mines = (int) (size * size * MEDIUM_MINE_PERCENTAGE);
                    timeLimit = getAdjustedTimeLimit("Medium", selectedSize);
                    break;
                case "Hard":
                    mines = (int) (size * size * HARD_MINE_PERCENTAGE);
                    timeLimit = getAdjustedTimeLimit("Hard", selectedSize);
                    break;
            }

            menuFrame.dispose();
            new Minesweeper(size, mines, timeLimit, playerName);
        });

        JTextArea scoreArea = new JTextArea();
        scoreArea.setEditable(false);
        JScrollPane scoreScrollPane = new JScrollPane(scoreArea);
        scoreScrollPane.setBorder(BorderFactory.createTitledBorder("High Scores"));
        List<Score> scores = Score.loadScores();
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
        menuFrame.add(new JLabel()); // Placeholder
        menuFrame.add(startButton);
        menuFrame.add(scoreScrollPane); // Add the score display

        menuFrame.setSize(new Dimension(400,400));
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setVisible(true);
    }

    private static int getAdjustedTimeLimit(String difficulty, String size) {
        switch (difficulty) {
            case "Easy":
                switch (size) {
                    case "Small":
                        return 180; // 3 minutes
                    case "Medium":
                        return 240; // 4 minutes
                    case "Large":
                        return 300; // 5 minutes
                }
            case "Medium":
                switch (size) {
                    case "Small":
                        return 135; // 2.25 minutes
                    case "Medium":
                        return 180; // 3 minutes
                    case "Large":
                        return 225; // 3.75 minutes
                }
            case "Hard":
                switch (size) {
                    case "Small":
                        return 90; // 1.5 minutes
                    case "Medium":
                        return 120; // 2 minutes
                    case "Large":
                        return 150; // 2.5 minutes
                }
        }
        return 180; // Default to 3 minutes
    }

    public static void main(String[] args) {
        showMenu();
    }
}