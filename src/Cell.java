import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Image;
import java.awt.MediaTracker;

abstract class Cell { // abstrakte Klasse für einzelne Zellen
    private static final int CELL_SIZE = 30; // Zellengröße
	private boolean isMine; //ob sie eine Mine ist 
    private boolean isRevealed; // ob sie augedeckt ist 

    public Cell(boolean isMine) { // Konstruktor
        this.isMine = isMine;
        this.isRevealed = false;
    }

    public boolean isMine() { // Wert zum abfragen ob die Zelle eine Mine ist 
        return isMine;
    }

    public boolean isRevealed() { // Wert zum abfragen ob die Zelle aufgedeckt ist 
        return isRevealed;
    }

    public void reveal(JButton button) { // aufdecken einer Zelle
        this.isRevealed = true; // Aufdeckwert true setzen
        if (isMine) { // falls Zelle eine Mine 
            button.setBackground(Color.RED); // Hintergrund rot 
            ImageIcon icon = new ImageIcon(getClass().getResource("images/bomb.png")); // Pfad zu Bld
            if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) { // falls Probleme mit Bild error handler + logging 
                System.out.println("Bomb image not loaded properly."); 
            }
            Image img = icon.getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_SMOOTH); // Bild anpassen
            button.setIcon(new ImageIcon(img)); // bild in Zelle setzen
        }
    }
}