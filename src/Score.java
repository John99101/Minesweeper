import java.io.*;
import java.util.ArrayList;
import java.util.List;

class Score implements Serializable {
    String playerName;
    int points;

    public Score(String playerName, int points) {
        this.playerName = playerName;
        this.points = points;
    }

    @Override
    public String toString() {
        return playerName + ": " + points + " points";
    }

    public static List<Score> loadScores() {
        List<Score> scores = new ArrayList<>();
        String scoreFile = System.getProperty("user.home") + File.separator + "scores.dat";
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(scoreFile))) {
            scores = (List<Score>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return scores;
    }
}