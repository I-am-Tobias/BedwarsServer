package samann.bwplugin.airwars.items;

public record Progress(State state, double progress) {
  public enum State { COOLDOWN, ACTIVE, READY }

  public Progress(State state, double done, double total) {
    this(state, done / total);
  }
}
