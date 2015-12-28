package com.orionletizi.com.orionletizi.midi;

public class MidiContext {
  private final double sampleRate;
  private final int ticksPerBeat;
  private final double tempo;

  public MidiContext(final double sampleRate, final int ticksPerBeat, final double tempo) {

    this.sampleRate = sampleRate;
    this.ticksPerBeat = ticksPerBeat;
    this.tempo = tempo;
  }

  public long frameToTick(long frame) {
    // convert frame to tick
    // frame -> time
    // frames / second = frame / timeInSeconds
    // (samples / second) * timeInSeconds = frame
    // timeInSeconds = frame / (samples / second)
    final double timeInSeconds = frame / sampleRate;
    // time -> beat
    // beats / minute = currentBeat / (timeInSeconds / 60)
    // (beats / minute) * (seconds * 60) = beat
    final double currentBeat = tempo * (timeInSeconds / 60);
    // beat -> tick
    // ticks / beat = currentTick / currentBeat
    // (ticks / beat) * currentBeat = currentTick
    return (long) (ticksPerBeat * currentBeat);
  }

  public int getTicksPerBeat() {
    return ticksPerBeat;
  }

  public double ticksToBeats(long ticks) {
    return ticks / ticksPerBeat;
  }

  public double ticksToMillisecond(int ticks) {
    return (ticksToBeats(ticks) / tempo) * 60 * 1000;
  }

  public long millisecondsToTicks(long millis) {
    final long frameCount = (long) sampleRate * millis / 1000;
    return frameToTick(frameCount);
  }
}
