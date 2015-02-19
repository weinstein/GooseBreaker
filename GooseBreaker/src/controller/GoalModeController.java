package controller;

import io.LevelIO;
import graphics.GameRenderer;
import physics.IceWorld;
import sound.AudioController;

public class GoalModeController extends LimitedBreadModeController {
  private int numIceSheetsGoal;

  public GoalModeController(IceWorld world, GameRenderer renderer, AudioController audio) {
    super(world, renderer, audio);
  }
  

  @Override
  protected boolean shouldStartGameEndTimer() {
    return super.shouldStartGameEndTimer() && this.getNontrivialIceSheetCount() >= this.numIceSheetsGoal;
  }

  @Override
  public boolean hasEnoughBreadFor(float size) {
    return super.hasEnoughBreadFor(size);
  }
  
  @Override
  public void initFromLevelIO(LevelIO level) {
    super.initFromLevelIO(level);
    this.numIceSheetsGoal = level.getNumIceSheetsGoal();
  }

}
