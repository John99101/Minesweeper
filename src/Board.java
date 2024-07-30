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

    public void initializeBoard(JPanel panel) { // initialisieren des grids/boards
        for (int i = 0; i < size; i++) { // alle reihen
            for (int j = 0; j < size; j++) { // alle spalten
                JButton button = new JButton(); // button als Zelle
                button.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE)); // größe der Zelle setzen
                button.setBackground(Color.GRAY); // Farbe der Zelle
                button.setFocusPainted(false); 
                int row = i;
                int col = j;
                button.addMouseListener(new MouseAdapter() { // wenn button geklickt, Zelle aufgedeckt
                    @Override
                    public void mouseClicked(MouseEvent e) { // bei linksklick aufdecken, rechtsklick markieren
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            reveal(row, col);
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            mark(row, col);
                        }
                    }
                });
                panel.add(button); // button zu grid hinzufügen
                buttons[i][j] = button; // ganzes Board mit buttons als zellen füllen
                cells[i][j] = new SafeCell(); // Default zu SafeCell, Minen werden später gesetzt
            }
        }
    }

    public void placeMines() { // plaziert die minen auf das board
        Random random = new Random();
        int count = 0; 
        while (count < mines) {
            int x = random.nextInt(size); //random auswählen welches Zelle eine Mine wird
            int y = random.nextInt(size); //random auswählen welches Zelle eine Mine wird
            if (!cells[x][y].isMine()) { // abfragen ob Zelle schon eine Mine ist, falls nicht Zelle zu Minenzelle machen und counter inkrementieren
                cells[x][y] = new MineCell(); // Zelle dann zur Mine machen
                count++;
            }
        }
    }

    public void calculateNumbers() { // berechnen wie viele Minen an Feld angrenzen
        for (int i = 0; i < size; i++) { // für alle Reihen
            for (int j = 0; j < size; j++) { // für alle Spalten
            	if (!cells[i][j].isMine()) { // falls Zelle keine Mine 
                    int count = countAdjacentMines(i, j); // abfragen wie viele Minen an Feld angrenzen
                    ((SafeCell) cells[i][j]).setAdjacentMines(count); // Wert setzen
                }
            }
        }
    }

    public void reveal(int x, int y) {
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
        saveScore(); // Punktestand speichern
        Object[] options = {"Return to Menu", "Restart"};
        int choice = JOptionPane.showOptionDialog(null, // dialogfenster das über gameFrame erscheint
                "Game Over! You have no lives left. What do you want to do?",
                "Game Over",
                JOptionPane.YES_NO_OPTION, // Ja/nein buttons
                JOptionPane.PLAIN_MESSAGE, // Nachrichttyp nur Text
                null, // kein icon 
                options, // Ja option(Return to menu)
                options[0]); // Nein option (Restart)
        if (choice == JOptionPane.YES_OPTION) { // falls ja spiel schließen und ins Menü
            showMenu();
        } else if (choice == JOptionPane.NO_OPTION) { // falls nein alte game instanz schließen, neue erstellen
            restartGame();
        }
    }

    private void gameWon() {
    	timer.stop(); // timer stoppen
        saveScore(); // Punktestand ins scoreboard speichern
        Object[] options = {"Return to Menu", "Restart"}; // array mit Texten für buttons
        int choice = JOptionPane.showOptionDialog(null, // das selbe wie in gameOver, nur mit anderem Text
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

    public void startTimer(int timeLimit) { // Timer starten
        timeElapsed = timeLimit; // Startzeit setzen
        timer = new Timer(1000, e -> { // jede Sekunde
            timeElapsed--; // dekrementieren
            timerLabel.setText("Time: " + timeElapsed); // Label auf aktuelle Zeit setzen
            if (timeElapsed <= 0) { // falls Zeit abgelaufen = game over 
                timer.stop();
                gameOver();
            }
        });
        timer.start();
    }

    private void revealAllMines() { // alle Zellen mit Minen aufdecken
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (cells[i][j].isMine()) { // falls Zelle Mine ist
                    cells[i][j].reveal(buttons[i][j]); // Zelle aufdecken
                }
            }
        }
    }

    private void saveScore() { // Punktestand in Scoreboard(Datei) speichern
        int points = (timeElapsed) * lives; // Punkte berechnen
        List<Score> scores = Score.loadScores(); // liste von Punktenständen laden
        scores.add(new Score(playerName, points)); // Punktestand hinzufügen
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Minesweeper.SCORE_FILE))) { // über Outputstream zu Datei herstellen
            oos.writeObject(scores); // über Outputstream Datei schreiben
        } catch (IOException e) { // falls Fehler handlen und loggen
            e.printStackTrace();
        }
    }

    private void restartGame() { // Spiel neu Starten
    	Minesweeper.gameFrame.dispose(); // altes spiel schließen
        new Minesweeper(size, mines, timeElapsed, playerName); // neues Spiel erstellen
    }

    private void showMenu() { // Menü zeigen
    	Minesweeper.gameFrame.dispose(); // altes Spiel schließen

        Minesweeper.showMenu(); // Menü anzeigen
    }
}
