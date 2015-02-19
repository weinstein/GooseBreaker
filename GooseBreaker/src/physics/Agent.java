package physics;

import graphics.AnimatedBody;

import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;

/**
 *
 * An Agent represents an AI entity that exists in a Box2D physics World.
 * The Agent knows how to control a body if you give it one.
 * 
 * @author Jack
 *
 */
public abstract class Agent extends AnimatedBody {

  /**
   * Let the agent ponder it's place in the world (existentially and
   * computationally) and then act, probably by applying force/impulse to the
   * supplied body.
   * @return true iff the agent decided to take an action.
   */
  public abstract boolean step(float dt, Body myBody);
  
  
  /**
   * Add a Body to the supplied World for this Agent, and return it.
   * @param world
   * @return
   */
  public abstract Body addToWorldAsBody(World world);
}
