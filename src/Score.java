import java.io.*;
import java.util.ArrayList;
import java.util.List;

class Score implements Serializable { //implementiert Serializable(um Dateien schreiben zu können)
    String playerName; 
    int points;

    public Score(String playerName, int points) { // Konstruktor
        this.playerName = playerName;
        this.points = points;
    }

    @Override
    public String toString() { // toString anpassen, damit lesbarer Output entsteht
        return playerName + ": " + points + " points";
    }

    public static List<Score> loadScores() { // Laden von Scores aus der Datei
        List<Score> scores = new ArrayList<>();
        String scoreFile = System.getProperty("user.home") + File.separator + "scores.dat"; // Dateispeicherort
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(scoreFile))) { // Inputstream
            scores = (List<Score>) ois.readObject(); // aus inputstream Objekt lesen
        } catch (IOException | ClassNotFoundException e) { // error handling mit Output
            e.printStackTrace();
        }
        return scores;
    }
}