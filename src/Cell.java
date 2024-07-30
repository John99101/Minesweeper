import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Image;
import java.awt.MediaTracker;

abstract class Cell {
    private static final int CELL_SIZE = 30;
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
            button.setBackground(Color.RED); // Optional: You might want to remove this if using an image
            ImageIcon icon = new ImageIcon(getClass().getResource("images/bomb.png")); // Ensure correct path
            if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                System.out.println("Bomb image not loaded properly.");
            }
            Image img = icon.getImage().getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(img));
        }
    }
}