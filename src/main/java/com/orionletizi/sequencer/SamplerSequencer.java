package com.orionletizi.sequencer;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;

import javax.sound.midi.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class SamplerSequencer extends UGen {
  private final AudioContext ac;
  private final List<InstrumentTrack> instrumentTracks = new ArrayList<>();
  private final long finalTick;
  private long currentFrame = 0;
  private Set<Bead> endListeners = new HashSet<>();

  public SamplerSequencer(AudioContext ac, List<Receiver> instruments, URL midiSource) throws InvalidMidiDataException, IOException {
    super(ac);
    this.ac = ac;
    final Sequence sequence = MidiSystem.getSequence(midiSource);
    finalTick = sequence.getTickLength();
    final Track[] tracks = sequence.getTracks();
    info("Tracks: " + tracks);
    info("Instruments: " + instruments);
    for (int i = 0; i < tracks.length && i < instruments.size(); i++) {
      final InstrumentTrack instrumentTrack = new InstrumentTrack(ac.getSampleRate(), sequence.getResolution(), instruments.get(i), tracks[i]);
      this.instrumentTracks.add(instrumentTrack);
      instrumentTrack.prepareTrack();
    }
  }

  public void addEndListener(Bead listener) {
    endListeners.add(listener);
  }

  @Override
  public void calculateBuffer() {
    boolean isEnd = false;
    for (int i = 0; i < ac.getBufferSize(); i++) {
      currentFrame++;
      for (InstrumentTrack instrumentTrack : instrumentTracks) {
        final long currentTick = instrumentTrack.notifyFrame(currentFrame);
        if (!isEnd) {
          isEnd = (currentTick >= finalTick);
        }
      }
    }
    if (isEnd) {
      for (Bead endListener : endListeners) {
        endListener.message(this);
      }
      pause(true);
      kill();
    }
  }

  private class InstrumentTrack {

    private final float sampleRate;
    private final int ticksPerBeat;
    private final Receiver instrument;
    private final Track track;
    private final Map<Long, Set<MidiEvent>> eventsByTick = new HashMap<>();
    private int currentTempo = 120;

    public InstrumentTrack(float sampleRate, int ticksPerBeat, Receiver instrument, Track track) {
      this.sampleRate = sampleRate;
      this.ticksPerBeat = ticksPerBeat;
      this.instrument = instrument;
      this.track = track;
    }

    public void prepareTrack() {
      for (int i = 0; i < track.size(); i++) {
        final MidiEvent midiEvent = track.get(i);
        final long tick = midiEvent.getTick();
        Set<MidiEvent> events = eventsByTick.get(tick);
        if (events == null) {
          // TODO: find the first tempo event and set the tempo
          events = new HashSet<>();
          eventsByTick.put(tick, events);
        }
        events.add(midiEvent);
        info("tick: " + tick + ", events: " + events);
      }
    }

    public long notifyFrame(long frame) {
      // convert frame to tick
      // frame -> time
      // frames / second = frame / timeInSeconds
      // (samples / second) * timeInSeconds = frame
      // timeInSeconds = frame / (samples / second)
      final double timeInSeconds = frame / sampleRate;
      // time -> beat
      // beats / minute = currentBeat / (timeInSeconds / 60)
      // (beats / minute) * (seconds * 60) = beat
      final double currentBeat = currentTempo * (timeInSeconds / 60);
      // beat -> tick
      // ticks / beat = currentTick / currentBeat
      // (ticks / beat) * currentBeat = currentTick
      final long currentTick = (long) (ticksPerBeat * currentBeat);
      notifyTick(currentTick);
      if (true && currentTick % 1000 == 0) {
        info("ac time: " + (ac.getTime() / 1000) + "s, buffer size: " + bufferSize + ", buffer time: " + ac.samplesToMs(bufferSize) / 1000 + "s");
        info("frame: " + frame + ", timeInSeconds: " + timeInSeconds + ", currentTempo: " + currentTempo + ", currentBeat: " + currentBeat
            + ", currentTick: " + currentTick);
      }
      return currentTick;
    }

    public void notifyTick(long tick) {
      final Set<MidiEvent> midiEvents = eventsByTick.get(tick);
      if (midiEvents != null) {
        for (MidiEvent midiEvent : midiEvents) {
          // TODO: check for tempo events and set the current tempo
          info("tick: " + tick + ", time: " + ac.getTime() + ", message: " + midiEvent.getMessage());
          instrument.send(midiEvent.getMessage(), -1);
        }
      }
    }
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }
}
