package controller;

import graphics.BreadAnimation;
import graphics.GameRenderer;
import io.LevelIO;
import physics.BreadBall;
import physics.IceWorld;
import sound.AudioController;

/**
 * A game mode where the user has a limited number of bread balls to throw.
 * The game ends when there is no more bread to throw, and there are no bread
 * balls in flight, and the geese haven't done anything for a few seconds.
 * @author Jack
 *
 */
public class LimitedBreadModeController extends GameController {
  private static final long WAIT_FOR_GAME_END_MS = 3000;
  private BreadBall protoBread;
  private int breadSupply;
  private long gameEndTimer;

  public LimitedBreadModeController(IceWorld world, GameRenderer renderer, AudioController audio) {
    super(world, renderer, audio);
    this.breadSupply = 0;
    this.protoBread = new BreadBall(0.0f);
  }
  
  public int getBreadSupply() {
    return this.breadSupply;
  }
  
  public void addBreadSupply(int amt) {
    this.breadSupply += amt;
  }
  
  public void resetBreadSupply() {
    this.breadSupply = 0;
  }
  
  protected boolean shouldStartGameEndTimer() {
    return this.breadSupply <= 0 &&
        this.getPiecesOfFoodInFlight().isEmpty() &&
        !this.getGameWorld().hasAnyAgentActed();
  }
  
  /**
   * Each step, check if the game should start ending.
   * If it should start ending, start the game end timer.
   */
  @Override
  public void step(float dt, int posIters, int velIters) {
    super.step(dt, posIters, velIters);
    if (!this.shouldStartGameEndTimer()) {
      this.gameEndTimer = -1;
    } else if (this.gameEndTimer < 0) {
      this.gameEndTimer = System.currentTimeMillis();
    }
  }
  
  @Override
  public void throwBreadBall(float size, float x0, float y0, float xyAngle, float zAngle, float speed) {
    if (!hasEnoughBreadFor(size)) {
      return;
    }
    --this.breadSupply;
    super.throwBreadBall(size, x0, y0, xyAngle, zAngle, speed);
  }

  /**
   * True iff the game end timer has been running for WAIT_FOR_GAME_END_MS
   * milliseconds.
   */
  @Override
  public boolean hasGameEnded() {
    return this.gameEndTimer > 0 &&
        System.currentTimeMillis() - this.gameEndTimer >
            LimitedBreadModeController.WAIT_FOR_GAME_END_MS;
  }

  @Override
  public boolean hasEnoughBreadFor(float size) {
    return this.breadSupply > 0;
  }
  
  
  @Override
  public void renderUiElements() {
    this.getRenderer().drawFoodSupply(this.breadSupply, this.protoBread);
    super.renderUiElements();
  }
  
  
  @Override
  public void initFromLevelIO(LevelIO level) {
    super.initFromLevelIO(level);
    this.addBreadSupply((int) level.getBreadSupply());
  }

}
