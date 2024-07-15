import javax.swing.JButton;
import java.awt.Color;

abstract class Cell {
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