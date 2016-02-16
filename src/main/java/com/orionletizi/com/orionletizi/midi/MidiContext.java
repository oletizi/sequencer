package com.orionletizi.com.orionletizi.midi;

import com.orionletizi.music.theory.Tempo;
import com.orionletizi.music.theory.TimeSignature;

public class MidiContext {
  private final double sampleRate;
  private final int ticksPerBeat;
  private TimeSignature timeSignature;
  private final double tempo;

  public MidiContext(final double sampleRate, final int ticksPerBeat, final Tempo tempo, final TimeSignature timeSignature) {

    this.sampleRate = sampleRate;
    this.ticksPerBeat = ticksPerBeat;
    this.timeSignature = timeSignature;
    this.tempo = tempo.getBPM();
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
    return ((double) ticks) / ticksPerBeat;
  }

  public double ticksToMillisecond(long ticks) {
    return (ticksToBeats(ticks) / tempo) * 60 * 1000;
  }

  public long millisecondsToTicks(long millis) {
    final long frameCount = (long) sampleRate * millis / 1000;
    return frameToTick(frameCount);
  }

  public long beatsToTick(double beat) {
    return (long) (beat * ticksPerBeat);
  }

  public double ticksToBars(long tick) {
    return ticksToBeats(tick) / timeSignature.getBeatsPerBar();
  }

  public Tempo getTempo() {
    return Tempo.newTempoFromBPM(tempo);
  }

  public TimeSignature getTimeSignature() {
    return timeSignature;
  }
}
