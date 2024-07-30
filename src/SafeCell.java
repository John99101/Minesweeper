import javax.swing.JButton;
import java.awt.Color;

class SafeCell extends Cell { // Sichere Zelle erbt von Cell
    private int adjacentMines;

    public SafeCell() { // Konstruktor
        super(false);
    }

    public void setAdjacentMines(int adjacentMines) { // Wert für angrenzende Minen setzen
        this.adjacentMines = adjacentMines;
    }

    @Override
    public void reveal(JButton button) { // Funktion zum aufdecken der Zelle
        super.reveal(button); // Funktion von Cell.java
        if (!isMine() && isRevealed()) { // falls Zelle keine Mine und aufgedeckt(-> safeCell)
            button.setBackground(Color.WHITE); // weißer Hintergrund
            button.setIcon(null); // kein icon setzen
            if (adjacentMines > 0) { // falls Minen anliegen
                button.setText(Integer.toString(adjacentMines)); // Text der Zelle auf anliegende Minen setzen
                button.setForeground(Color.BLUE); // Text blau machen
            } else {
                button.setText(""); // sonst Text leer lassen
            }
        }
    }

	
}