package controller;

public class UnlockableByLevel extends Unlockable {
  
  private String prereqLevel;
  private String prereqMode;
  private int prereqScore;
  private HighScoreManager highScoreMan;
  
  public UnlockableByLevel(HighScoreManager highScoreMan, String prereqLevel, String prereqMode, int prereqScore) {
    this.highScoreMan = highScoreMan;
    this.prereqLevel = prereqLevel;
    this.prereqScore = prereqScore;
  }

  @Override
  public boolean isUnlocked() {
    System.out.println(highScoreMan.getAllScores(prereqLevel, prereqMode));
    int highestScore = highScoreMan.getAllScores(prereqLevel, prereqMode).get(0);
    return highestScore >= prereqScore;
  }

}
