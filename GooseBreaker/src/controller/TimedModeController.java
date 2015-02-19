package controller;

import graphics.GameRenderer;
import io.LevelIO;
import physics.IceWorld;
import sound.AudioController;

public class TimedModeController extends LimitedBreadModeController {
  private long startTimeMillis;
  private long timeLimitMillis;
  
  public TimedModeController(IceWorld world, GameRenderer renderer, AudioController audio) {
    super(world, renderer, audio);
  }
  
  public void restart(long limitMS) {
    this.startTimeMillis = System.currentTimeMillis();
    this.timeLimitMillis = limitMS;
    this.resetBreadSupply();
    this.addBreadSupply((int) (this.timeLimitMillis / 1000 / 2));
  }
  
  public long getElapsedMillis() {
    return System.currentTimeMillis() - this.startTimeMillis;
  }
  
  
  public long getRemainingMillis() {
    return this.timeLimitMillis - this.getElapsedMillis();
  }

  @Override
  public boolean hasGameEnded() {
    return this.getRemainingMillis() <= 0;
  }

  @Override
  public boolean hasEnoughBreadFor(float size) {
    return super.hasEnoughBreadFor(size) && !this.hasGameEnded();
  }
  
  @Override
  public void renderUiElements() {
    this.getRenderer().drawTimer((int) this.getRemainingMillis() / 1000);
    super.renderUiElements();
  }
  
  @Override
  public void initFromLevelIO(LevelIO level) {
    super.initFromLevelIO(level);
    this.restart(level.getTimeLimitMillis());
  }

}
