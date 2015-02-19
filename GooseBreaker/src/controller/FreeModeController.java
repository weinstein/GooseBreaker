package controller;


import graphics.GameRenderer;
import physics.IceWorld;
import sound.AudioController;

/**
 * Free mode: throw as much bread as you want. The game never ends.
 * @author Jack
 *
 */
public class FreeModeController extends GameController {

  public FreeModeController(IceWorld world, GameRenderer renderer, AudioController audio) {
    super(world, renderer, audio);
  }

  /**
   * The game never ends!
   */
  @Override
  public boolean hasGameEnded() {
    return false;
  }

  /**
   * There is always enough bread to go around.
   */
  @Override
  public boolean hasEnoughBreadFor(float size) {
    return true;
  }

}
