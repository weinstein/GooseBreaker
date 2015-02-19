package controller;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import graphics.CameraController;
import graphics.GameRenderer;
import physics.BreadBall;
import physics.Goose;
import physics.IceWorld;
import sound.AudioController;

public class TutorialModeController extends GameController {
  private enum State {
    GAME_OVER(null, null),
    ICE_BREAK("Each piece of ice is worth bonus points.", GAME_OVER),
    POINTS("Try to score 100 points by breaking some ice!", ICE_BREAK),
    GEESE("Try to lure the geese by throwing pieces of bread.", POINTS),
    THROW_BREAD("Fling up from the bottom of the screen to throw bread.", GEESE),
    ZOOM("Pinch to zoom in or out.", THROW_BREAD),
    PAN("Drag with one finger to look around.", ZOOM),
    INIT(null, PAN);
    
    private State next;
    private String msg;
    private State(String msg, State next) {
      this.msg = msg;
      this.next = next;
    }
    
    public String getInstructions() {
      return this.msg;
    }
    
    public State getNext() {
      return this.next;
    }
  }
  
  private State curState;
  private int breadSupply;
  private BreadBall protoBread;
  private long startTime;
  
  
  public TutorialModeController(IceWorld world, GameRenderer renderer,
      AudioController audio) {
    super(world, renderer, audio);
    this.curState = State.INIT;
    this.breadSupply = 0;
    this.protoBread = new BreadBall(0.0f);
    this.startTime = System.currentTimeMillis();
  }
  
  private boolean shouldMoveToNextState() {
    switch (this.curState) {
    case INIT:
      return System.currentTimeMillis() - this.startTime > 1000;
    case PAN:
      float dist = Math.abs(this.getRenderer().getCamera().getWorldCenterX()) + Math.abs(this.getRenderer().getCamera().getWorldCenterY());
      return dist > 25;
    case ZOOM:
      float zoomFactor = this.getRenderer().getCamera().getZoomFactor();
      return zoomFactor < 1.5 * CameraController.MIN_ZOOM_FACTOR || zoomFactor > (2.0f / 3.0f) * CameraController.MAX_ZOOM_FACTOR;
    case THROW_BREAD:
      return this.breadSupply == 0 && this.getPiecesOfFoodInFlight().isEmpty();
    case GEESE:
      Body gooseBody = this.getGameWorld().getAgentBodies().iterator().next();
      float gooseTraveledDist = Math.abs(gooseBody.getPosition().x) + Math.abs(gooseBody.getPosition().y);
      return this.getGameWorld().hasAnyAgentActed() && this.getPiecesOfFoodInFlight().isEmpty() && gooseTraveledDist > 25;
    case POINTS:
      return this.getScore() > 100 && this.getPiecesOfFoodInFlight().isEmpty();
    case ICE_BREAK:
      return this.getNontrivialIceSheetCount() > 3 && this.breadSupply == 0 && !this.getGameWorld().hasAnyAgentActed() && this.getPiecesOfFoodInFlight().isEmpty();
    default:
      return false;
    }
  }
  
  @Override
  public boolean hasGameEnded() {
    return this.curState == State.GAME_OVER;
  }


  @Override
  public boolean hasEnoughBreadFor(float size) {
    return this.breadSupply > 0 || this.curState == State.GEESE;
  }
  
  @Override
  public void throwBreadBall(float size, float x0, float y0, float xyAngle, float zAngle, float speed) {
    if (!hasEnoughBreadFor(size)) {
      return;
    }
    --this.breadSupply;
    super.throwBreadBall(size, x0, y0, xyAngle, zAngle, speed);
  }
  
  @Override
  public void render() {
    super.renderGameElements();
    if (this.curState.ordinal() <= State.THROW_BREAD.ordinal() && this.curState != State.GEESE) {
      this.getRenderer().drawFoodSupply(this.breadSupply, this.protoBread);
    }
    if (this.curState.ordinal() <= State.POINTS.ordinal()) {
      this.getRenderer().drawScore(this.getScore(), this.getNontrivialIceSheetCount());
    }
    this.getRenderer().maybeDrawUserMessage(this.getUserMessageOrNull());
  }
  
  @Override
  public void step(float dt, int vi, int pi) {
    super.step(dt, vi, pi);
    if (this.shouldMoveToNextState()) {
      this.moveToNextState();
    }
  }
  
  private void moveToNextState() {
    this.curState = this.curState.getNext();
    if (this.curState == State.THROW_BREAD) {
      this.breadSupply = 2;
    } else if (this.curState == State.GEESE) {
      for (Body b : this.getGameWorld().getFoodPieceBodies()) {
        this.getGameWorld().destroyBody(b);
        this.getGameWorld().step(0, 0, 0);
      }
      this.getGameWorld().getFoodPieceBodies().clear();
      this.getPiecesOfFoodInFlight().clear();
      
      Goose goose = new Goose(5.0f);
      this.getGameWorld().addAgent(goose).setTransform(new Vec2(0, 0), 0);
    } else if (this.curState == State.POINTS) {
      for (Body b : this.getGameWorld().getFoodPieceBodies()) {
        this.getGameWorld().destroyBody(b);
        this.getGameWorld().step(0, 0, 0);
      }
      this.getGameWorld().getFoodPieceBodies().clear();
      this.getPiecesOfFoodInFlight().clear();
      this.resetScore();
      this.breadSupply = 5;
    } else if (this.curState == State.ICE_BREAK) {
      this.breadSupply = 5;
    }
    this.showUserMessage(this.curState.getInstructions(), 4000);
  }

}
