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
        calculateNumbers(); // Zählt wieviele Minen ans Feld angrenzen
        startTimer(); // Timer starten
        gameFrame.add(gamePanel, BorderLayout.CENTER); 
        gameFrame.setSize(new Dimension(800, 800));;
        gameFrame.setLocationRelativeTo(null); // Center the window
        gameFrame.setVisible(true);
    }

    private void initializeBoard(JPanel panel) { // initialisieren des grids/boards
        for (int i = 0; i < size; i++) { // alle reihen
            for (int j = 0; j < size; j++) { // alle spalten
                JButton button = new JButton();  // button als Zelle 
                button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                button.setBackground(Color.GRAY); 
                button.setFocusPainted(false); 
                int row = i;
                int col = j;
                button.addMouseListener(new MouseAdapter() { // wenn button geklickt, Zelle aufgedeckt
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            reveal(row, col);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            mark(row, col);
                        }
                    }
                });
                panel.add(button); // button zu grid hinzufügen
                buttons[i][j] = button; // ganzes Board mit buttons als zellen füllen 
                cells[i][j] = new SafeCell(); // Default to SafeCell, mines will be placed later
            }
        }
    }

    private void placeMines() { // plaziert die minen auf das board
        Random random = new Random();
        int count = 0;
        while (count < mines) { 
            int x = random.nextInt(size); //random auswählen welches feld ne Mine wird 
            int y = random.nextInt(size); //random auswählen welches feld ne Mine wird 
            if (!cells[x][y].isMine()) { // abfragen ob Feld schon eine Mine ist, falls nicht Zelle zu Minenzelle machen und counter inkrementieren
                cells[x][y] = new MineCell();
                count++;
            }
        }
    }

    private void calculateNumbers() { // berechnen wie viele Minen an Feld angrenzen
        for (int i = 0; i < size; i++) { // für alle Reihen
            for (int j = 0; j < size; j++) { // für alle Spalten
                if (!cells[i][j].isMine()) { // falls Zelle keine Mine 
                    int count = countAdjacentMines(i, j); // abfragen wie viele Minen an Feld angrenzen
                    ((SafeCell) cells[i][j]).setAdjacentMines(count); // Wert setzen 
                }
            }
        }
    }

    private void reveal(int x, int y) { // Zelle aufdecken
        if (revealed[x][y] || marked[x][y]) { // falls schon setzen nichts tun
            return;
        }
        revealed[x][y] = true; // als aufgedeckt setzen
        cells[x][y].reveal(buttons[x][y]); // Zelle aufdecken

        if (cells[x][y].isMine()) { // falls die Zelle ne Mine ist leben abziehen
            lives--;
            livesLabel.setText("Lives: " + lives); // auf in label ändern
            if (lives == 0) { 
                gameOver();
                return;
            }
        } else { // falle Zellen drum herum aufdecken, die auch keine Mine angrenzend haben
            int count = countAdjacentMines(x, y); // abfragen wie viele Minen an Feld angrenzen
            if (count == 0) { 
                for (int dx = -1; dx <= 1; dx++) { // reihe 
                    for (int dy = -1; dy <= 1; dy++) { // spalte
                        int nx = x + dx; //alle Felder drumherum abfragen
                        int ny = y + dy; //alle Felder drumherum abfragen
                        if (nx >= 0 && nx < size && ny >= 0 && ny < size && !revealed[nx][ny]) { // wenn Zellen in board und keine Mine angrenzen
                            reveal(nx, ny); // Zellen aufdecken
                        }
                    }
                }
            }
        }

        if (isGameWon()) {
            gameWon();
        }
    }

    private void mark(int x, int y) { // felder markieren 
        if (revealed[x][y]) { // falls schon aufgedeckt nichts tun
            return;
        }
        marked[x][y] = !marked[x][y]; // falls marked, nicht mehr marken
        if (marked[x][y]) { // falls marked, icon setzen
        	ImageIcon icon = new ImageIcon(getClass().getResource("images/flag.png")); // image aus imageordner
        	Image img= icon.getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_SMOOTH); // image scalen
        	buttons[x][y].setIcon( new ImageIcon(img)); // icon setzen
        } else {
        buttons[x][y].setIcon(null); // icon aus Zelle entfernen
        }
    }

    private int countAdjacentMines(int x, int y) { // anliegende Minen erkennen
        int count = 0; 
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx; // anliegende Zellen prüfen
                int ny = y + dy; // anliegende Zellen prüfen
                if (nx >= 0 && nx < size && ny >= 0 && ny < size && cells[nx][ny].isMine()) { // falls anliegende Zelle Mine 
                    count++;
                }
            }
        }
        return count; // ausgeben wieviele Minen sind 
    }

    private boolean isGameWon() { // checken ob Spiel gewonnen ist 
        for (int i = 0; i < size; i++) { 
            for (int j = 0; j < size; j++) {
                if (!cells[i][j].isMine() && !revealed[i][j]) { // abfragen ob auf board Zellen die keine Minen sind nicht aufgedeckt sind
                    return false; 
                }
            }
        }
        return true; // falls ganzes board aufgedeckt und Minen -> gewonnen
    }

    private void gameOver() { // abfragen ob spiel verloren und speichern des scores
        timer.stop(); // timer stoppen
        revealAllMines(); // alle Minen aufdecken
        saveScore(); // Punktestand ins scoreboard speichern
        Object[] options = {"Return to Menu", "Restart"}; // array mit Texten für buttons
        int choice = JOptionPane.showOptionDialog(gameFrame, // Dialogfenster das über gameframe erscheint 
                "Game Over! You have no lives left. What do you want to do?", 
                "Game Over",
                JOptionPane.YES_NO_OPTION, // Ja/nein buttons 
                JOptionPane.PLAIN_MESSAGE, // Nachrichttyp nur Text  
                null, // kein icon 
                options, // Ja option(Return to menu)
                options[0]); // Nein option (Restart)
        if (choice == JOptionPane.YES_OPTION) { // falls ja spiel schließen und ins Menü
            gameFrame.dispose();
            showMenu();
        } else if (choice == JOptionPane.NO_OPTION) { // falls nein alte game instanz schließen, neue erstellen
            gameFrame.dispose();
            new Minesweeper(size, mines, timeLimit, playerName);
        }
    }

    private void gameWon() { // falls spiel gewonnen
        timer.stop(); // timer stoppen
        saveScore(); // Punktestand ins scoreboard speichern
        Object[] options = {"Return to Menu", "Restart"}; // array mit Texten für buttons
        int choice = JOptionPane.showOptionDialog(gameFrame, // Dialogfenster das über gameframe erscheint 
                "Congratulations! You won! What do you want to do?",
                "Congratulations",
                JOptionPane.YES_NO_OPTION, // Ja/nein buttons
                JOptionPane.PLAIN_MESSAGE, // Nachrichttyp nur Text 
                null, // kein icon 
                options, // Ja option(Return to menu)
                options[0]); // Nein option (Restart)
        if (choice == JOptionPane.YES_OPTION) { // falls ja spiel schließen und ins Menü
            gameFrame.dispose();
            showMenu();
        } else if (choice == JOptionPane.NO_OPTION) { // falls nein alte game instanz schließen, neue erstellen
            gameFrame.dispose();
            new Minesweeper(size, mines, timeLimit, playerName);
        }
    }

    private void startTimer() { // Timer starten 
    	timeElapsed = timeLimit; // Startzeit setzen
        timer = new Timer(1000, e -> { // jede Sekunde
            timeElapsed--; // runterzählen
            timerLabel.setText("Time: " + timeElapsed); // Label auf aktuellen Wert setzen
            if (timeElapsed <= 0) { // falls Zeit abgelaufen game Over 
                timer.stop();
                gameOver();
            }
        });
        timer.start(); // timer starten
    }

    private void revealAllMines() { // alle Zellen mit Minen aufdecken 
        for (int i = 0; i < size; i++) { 
            for (int j = 0; j < size; j++) { // für jede Zelle 
                if (cells[i][j].isMine()) { // falls Zelle Mine ist 
                    cells[i][j].reveal(buttons[i][j]); // Zelle aufdecken
                }
            }
        }
    }

    private void saveScore() { // Punktestand in Scoreboard speichern 
        int points = (timeLimit - timeElapsed) * lives; // Punkte berechnen 
        List<Score> scores = Score.loadScores(); // liste von Punktenständen laden 
        scores.add(new Score(playerName, points)); // Punktestand hinzufügen 
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORE_FILE))) { // über Outputstream zu Datei herstellen
            oos.writeObject(scores); // über Outputstream Datei schreiben 
        } catch (IOException e) { // falls Fehler handlen und loggen 
            e.printStackTrace(); 
        }
    }

    private static void showMenu() { // Menü zeigen
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