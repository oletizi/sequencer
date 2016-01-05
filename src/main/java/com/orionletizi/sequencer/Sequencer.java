package com.orionletizi.sequencer;

import com.orionletizi.com.orionletizi.midi.MidiContext;
import com.orionletizi.com.orionletizi.midi.MidiUtils;
import com.orionletizi.com.orionletizi.midi.message.MidiMetaMessage;
import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.UGen;

import javax.sound.midi.*;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Sequencer extends UGen {
  private static final Logger logger = LoggerImpl.forClass(Sequencer.class);
  private final AudioContext ac;
  private final List<InstrumentTrack> instrumentTracks = new ArrayList<>();
  private final long finalTick;
  private long currentFrame = 0;
  private Set<Bead> endListeners = new HashSet<>();

  public Sequencer(final AudioContext ac, final List<Receiver> instruments, final URL midiSource) throws InvalidMidiDataException, IOException {
    this(ac, instruments, MidiSystem.getSequence(midiSource));
  }

  public Sequencer(final AudioContext ac, final List<Receiver> instruments, final Sequence sequence) throws InvalidMidiDataException {
    this(ac, instruments, Arrays.asList(sequence));
  }

  public Sequencer(final AudioContext ac, final List<Receiver> instruments, final List<Sequence> sequences) throws InvalidMidiDataException {
    super(ac);
    this.ac = ac;
    final Sequence sequence = MidiUtils.merge(sequences);
    finalTick = sequence.getTickLength();
    final Track[] tracks = sequence.getTracks();
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
      final long thisTick = new MidiContext(sampleRate, ticksPerBeat, currentTempo).frameToTick(frame);
      if (thisTick != currentTick) {
        notifyTick(thisTick);
      }
      currentTick = thisTick;
      return currentTick;
    }

    public void notifyTick(long tick) {
      final List<MidiEvent> midiEvents = eventsByTick.get(tick);
      if (midiEvents != null) {
        info("midi events by tick: " + tick + ", events: " + midiEvents);
        for (final MidiEvent midiEvent : midiEvents) {
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
    //System.out.println(getClass().getSimpleName() + ": " + s);
    logger.info(s);
  }
}
