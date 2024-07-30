import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Menu {

	 	private static final double EASY_MINE_PERCENTAGE = 0.1; // Prozentsatz wie viele Felder Minen sind bei Schwierigkeit easy
	    private static final double MEDIUM_MINE_PERCENTAGE = 0.15; // Prozentsatz wie viele Felder Minen sind bei Schwierigkeit medium
	    private static final double HARD_MINE_PERCENTAGE = 0.2; // Prozentsatz wie viele Felder Minen sind bei Schwierigkeit hard
	
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
	
}
