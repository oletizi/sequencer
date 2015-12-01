package com.orionletizi.music.theory;

public class Tempo {
  private final double tempo;

  private Tempo(final double microsecondsPerQuarterNote) {
    tempo = microsecondsPerQuarterNote;
  }

  public double getBPM() {
    return 60d * 1000d * 1000d / tempo;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ": " + getBPM() + "bpm";// / 60d / 1000d / 1000d);
  }

  public static Tempo newTempoFromMicroseconds(final double us) {
    return new Tempo(us);
  }

  public static Tempo newTempoFromBPM(int bpm) {
    //  1 / bpm                     = min / beat
    // (1 / bpm) * 60 * 1000 * 1000 = us  / beat
    return new Tempo((1 / bpm) * 60 * 1000 * 1000);
  }

}
