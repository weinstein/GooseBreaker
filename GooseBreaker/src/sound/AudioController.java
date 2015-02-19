package sound;

public abstract class AudioController {
  public enum Clip {
    ICE_CRUNCH("explosion"),
    BREAD_NOMS("bread_noms"),
    GOOSE_HONK1("goose_honk1"),
    GOOSE_HONK2("goose_honk2"),
    GOOSE_HONK3("goose_honk3"),
    GOOSE_HONK4("goose_honk4"),
    GOOSE_GROWL("goose_growl"),
    WOOSH("air_woosh"),
    PLUNK("water_plunk");
    
    private String ident;
    
    
    private Clip(String ident) {
      this.ident = ident;
    }
    
    public String getIdentifier() {
      return this.ident;
    }
  }
  
  public abstract void step();
  
  public abstract void playAudio(Clip clip);
  
  public abstract void stopAllAudio();
}
