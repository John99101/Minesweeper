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
    public static final String SCORE_FILE = System.getProperty("user.home") + File.separator + "scores.dat"; // Pfad zur Scorefile(zum speichern der scores)

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
    public static JFrame gameFrame; // gameFrame initialisieren(der Rahmen in dem die UI ist)
    private String playerName; // Spielername
    private Board board; // Objekt von Board initialisieren
    
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
        board = new Board(size, mines, 3, timerLabel, livesLabel, playerName); // Initialize the Board with 3 lives
        board.initializeBoard(gamePanel); // Initialize board with buttons
        board.placeMines(); // Place mines
        board.calculateNumbers(); // Calculate numbers for the cells
        board.startTimer(timeLimit); // Timer starten
        gameFrame.add(gamePanel, BorderLayout.CENTER); 
        gameFrame.setSize(new Dimension(800, 800));
        gameFrame.setLocationRelativeTo(null); // Center the window
        gameFrame.setVisible(true);
    }



    public static void showMenu() { // Menü zeigen
        JFrame menuFrame = new JFrame("Minesweeper Menu"); // Frame für Menü erstellen
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // closed den Gameframe sobald man das Fenster schließt
        menuFrame.setLayout(new GridLayout(6, 2)); // Grid erstellen 

        JLabel nameLabel = new JLabel("Enter your name:"); // Label für Namenseingabe 
        JTextField nameField = new JTextField(); // Input Textfeld für Namen

        JLabel sizeLabel = new JLabel("Select size:"); // Label für Größe des Boards
        JComboBox<String> sizeComboBox = new JComboBox<>(new String[]{"Small", "Medium", "Large"}); // dropdown für Input der Größe des Boards

        JLabel difficultyLabel = new JLabel("Select difficulty:"); // Label für Schwierigkeit
        JComboBox<String> difficultyComboBox = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"}); // dropdown für Input der Schwierigkeit

        JButton startButton = new JButton("Start Game"); // Button um das Game zu starten
        startButton.addActionListener(e -> { // Listener für button
            String playerName = nameField.getText().trim(); // extrahiert Text aus Namensfeld
            String selectedSize = (String) sizeComboBox.getSelectedItem(); // extrahiert Feldgröße aus dropdown
            String selectedDifficulty = (String) difficultyComboBox.getSelectedItem(); // extrahiert Schwierigkeit aus dropdown

            if (playerName.isEmpty()) { // errorhandling falls Name nicht angegeben
            	JOptionPane.showMessageDialog(menuFrame, "Please enter your name."); 
                return;
            }

            int size = 0;
            int mines = 0;
            int timeLimit = 0;

            switch (selectedSize) { // Gewählte Feldgröße "übersetzen"
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

            switch (selectedDifficulty) { // gewählte Schwierigkeit "übersetzen"
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

            menuFrame.dispose(); // Menü schließen
            new Minesweeper(size, mines, timeLimit, playerName); // Spiel mit gewählten Einstellungen erstellen
        });

        JTextArea scoreArea = new JTextArea(); // Platz für Scoreboard 
        scoreArea.setEditable(false); // nicht editierbar 
        JScrollPane scoreScrollPane = new JScrollPane(scoreArea); // scrollbar hinzufügen
        scoreScrollPane.setBorder(BorderFactory.createTitledBorder("High Scores")); // Umrahmung mit Titel
        List<Score> scores = Score.loadScores(); // Punktestände laden
        scores.sort((s1, s2) -> Integer.compare(s2.points, s1.points)); // nach höchsten Punkten sortieren
        for (Score score : scores) {
            scoreArea.append(score.toString() + "\n"); // Punktestände appenden
        }
        // Komponenten zu Frames hinzufügen
        menuFrame.add(nameLabel);
        menuFrame.add(nameField);
        menuFrame.add(sizeLabel);
        menuFrame.add(sizeComboBox);
        menuFrame.add(difficultyLabel);
        menuFrame.add(difficultyComboBox);
        menuFrame.add(new JLabel()); // Placeholder
        menuFrame.add(startButton);
        menuFrame.add(scoreScrollPane);

        menuFrame.setSize(new Dimension(400,400)); // größe des MenüFrames 
        menuFrame.setLocationRelativeTo(null);  // setzen wo das Menü erscheint (mittig)
        menuFrame.setVisible(true); // sichtbar machen
    }

    private static int getAdjustedTimeLimit(String difficulty, String size) {  // Zeitlimit zu schwierigkeit und Boardgröße mappen
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
        showMenu(); // Menü öffnen
    }
}