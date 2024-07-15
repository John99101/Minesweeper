import javax.swing.JButton;
import java.awt.Color;

class SafeCell extends Cell {
    private int adjacentMines;

    public SafeCell() {
        super(false);
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }

    @Override
    public void reveal(JButton button) {
        super.reveal(button);
        if (!isMine() && isRevealed()) {
            button.setBackground(Color.WHITE);
            if (adjacentMines > 0) {
                button.setText(Integer.toString(adjacentMines));
                button.setForeground(Color.BLUE); // Set text color to blue
            } else {
                button.setText("");
            }
        }
    }
}