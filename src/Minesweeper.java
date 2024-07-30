import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.List;
import java.util.Random;

public class Minesweeper {
    private static final int CELL_SIZE = 30;  // Zellgröße in UI 
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



    

    public static void main(String[] args) {
        Menu.showMenu(); // Menü öffnen
    }
}