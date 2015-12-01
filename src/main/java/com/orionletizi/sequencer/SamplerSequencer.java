package com.orionletizi.sequencer;

import com.orionletizi.com.orionletizi.midi.message.MidiMetaMessage;
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
    private final Map<Long, List<MidiEvent>> eventsByTick = new HashMap<>();
    private double currentTempo = 120d;
    private long currentTick = -1;
    private boolean initialTempoIsSet = false;

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
        List<MidiEvent> events = eventsByTick.get(tick);
        if (events == null) {
          events = new ArrayList<>();
          eventsByTick.put(tick, events);
        }
        if (!initialTempoIsSet) {
          initialTempoIsSet = checkAndSetTempo(midiEvent);

        }
        // TODO: find the first tempo event and set the tempo
        events.add(midiEvent);
      }
    }

    private boolean checkAndSetTempo(MidiEvent midiEvent) {
      boolean rv = false;
      final MidiMessage message = midiEvent.getMessage();
      if (message instanceof MetaMessage) {
        final MidiMetaMessage meta = new MidiMetaMessage((MetaMessage) message);
        if (meta.isSetTempo()) {
          currentTempo = meta.getTempo().getBPM();
          rv = true;
        }
      }
      return rv;
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
      final long thisTick = (long) (ticksPerBeat * currentBeat);

      if (thisTick != currentTick) {
        notifyTick(thisTick);
        if (false && thisTick % 1000 == 0) {
        info("ac time: " + (ac.getTime() / 1000) + "s, buffer size: " + bufferSize + ", buffer time: " + ac.samplesToMs(bufferSize) / 1000 + "s");
        info("frame: " + frame + ", timeInSeconds: " + timeInSeconds + ", currentTempo: " + currentTempo + ", currentBeat: " + currentBeat
            + ", currentTick: " + thisTick);
        }
      }
      currentTick = thisTick;
      return currentTick;
    }

    public void notifyTick(long tick) {
      final List<MidiEvent> midiEvents = eventsByTick.get(tick);
      if (midiEvents != null) {
        for (int i = 0; i < midiEvents.size(); i++) {
          final MidiEvent midiEvent = midiEvents.get(i);
          final boolean tempoWasSet = checkAndSetTempo(midiEvent);
          if (tempoWasSet) {
            info("tempo set: tempo: " + currentTempo + ", tick: " + tick + ", time: " + ac.getTime() + ", message: " + midiEvent.getMessage());
          }
          instrument.send(midiEvent.getMessage(), -1);
        }
      }
    }
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }
}
