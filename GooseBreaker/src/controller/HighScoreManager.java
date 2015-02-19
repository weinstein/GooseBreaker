package controller;

import java.util.List;

public abstract class HighScoreManager {

  public abstract void putHighScore(String level, String mode, String user, int score);
  
  public abstract List<Integer> getAllScores(String level, String mode);
  
  public abstract List<String> getAllScoreNames(String level, String mode);
  
  public abstract boolean isHighScore(String levelName, String mode, int score);
  
}
