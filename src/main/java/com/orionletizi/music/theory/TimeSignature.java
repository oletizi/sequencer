package com.orionletizi.music.theory;

public class TimeSignature {
  private final int beatsPerBar;
  private final int beatUnit;

  public TimeSignature(final int beatsPerBar, final int beatUnit) {
    this.beatsPerBar = beatsPerBar;
    this.beatUnit = beatUnit;
  }

  @Override
  public String toString() {
    return beatsPerBar + "/" + beatUnit;
  }
}
