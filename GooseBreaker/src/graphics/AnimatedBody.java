package graphics;

import org.jbox2d.dynamics.Body;

public abstract class AnimatedBody implements Animation {
  private Body body;
  
  public void setBody(Body body) {
    this.body = body;
  }
  
  public Body getBody() {
    return this.body;
  }
}
