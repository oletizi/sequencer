package com.orionletizi.sequencer;

import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.DelayEvent;
import net.beadsproject.beads.ugens.SamplePlayer;
import org.jfugue.midi.MidiParser;
import org.jfugue.theory.Note;

import javax.sound.midi.MidiEvent;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Sequencer extends MidiParser {

  private static final Logger logger = LoggerImpl.forClass(Sequencer.class);


  private final Map<Byte, Set<NoteOnEvent>> playingNotes = new HashMap<>();

  private final Map<Long, TickEvents> tickEvents = new TreeMap<>();
  private final AudioContext ac;
  private final SampleSet sampleSet;

  private int tempo;

  public Sequencer(final AudioContext ac, final SampleSet sampleSet) {
    this.ac = ac;
    this.sampleSet = sampleSet;
  }

  public void play() {

    for (Map.Entry<Long, TickEvents> entry : tickEvents.entrySet()) {
      final TickEvents events = entry.getValue();
      // Add every note on to the playing set
      for (NoteOnEvent noteOn : events.getNoteOnEvents()) {
        Set<NoteOnEvent> playing = playingNotes.get(noteOn.note.getValue());
        if (playing == null) {
          playing = new HashSet<>();
        }
        playing.add(noteOn);
      }

      // Remove every playing note corresponding to the notes off
      for (NoteOffEvent noteOff : events.getNoteOffEvents()) {
        final Set<NoteOnEvent> playing = playingNotes.get(noteOff.note.getValue());
        if (playing != null) {
          final long tickOff = noteOff.event.getTick();
          for (NoteOnEvent noteOn : playing) {
            new NoteOffTrigger(ac, noteOff.event.getTick() - noteOn.tickStart, noteOn);
          }
          playing.clear();
        }
      }
    }

    // TODO: Make metronome configurable
    // TODO: Make metronome sensitive to tempo change events

    final Metronome metronome = new Metronome(ac, this.tempo);
    metronome.start();
    ac.start();
  }

  @Override
  public void fireTempoChanged(int tempoBPM) {
    super.fireTempoChanged(tempoBPM);
    // TODO: add tempo change events
    logger.info("TEMPO: " + tempoBPM);
    this.tempo = tempoBPM;
  }

  @Override
  public void fireNotePressed(MidiEvent event, Note note) {
    super.fireNotePressed(event, note);
    final long tick = event.getTick();
    final long startTime = this.ticksToMs(tick);
//logger.info("notePressed: tick: " + tick + ", start time: " + startTime + ", note: " + note + ", duration: " + note.getDuration());
    final File sampleFile = sampleSet.getSampleFileForNote(note.getValue());
    try {
      if (sampleFile != null && sampleFile.exists()) {
        final Sample sample = new Sample(sampleFile.getAbsolutePath());
        final SamplePlayer player = new SamplePlayer(ac, sample);
        final NoteOnEvent noteEvent = new NoteOnEvent(ac, (float) startTime, note, player);
        getTickEvents(event.getTick()).addNoteOn(noteEvent);
      } else {
        logger.info("No sample for note: " + note);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void fireNoteReleased(MidiEvent event, Note note) {
    super.fireNoteReleased(event, note);
    getTickEvents(event.getTick()).addNoteOff(new NoteOffEvent(event, note));
  }

  private TickEvents getTickEvents(long tick) {
    TickEvents ticks = this.tickEvents.get(tick);
    if (ticks == null) {
      ticks = new TickEvents();
      tickEvents.put(tick, ticks);
    }
    return ticks;
  }

  private class NoteOffTrigger extends DelayEvent {

    private final NoteOnEvent onEvent;
    private boolean triggered = false;

    public NoteOffTrigger(AudioContext context, double delay, NoteOnEvent onEvent) {
      super(context, delay);
      this.onEvent = onEvent;
      context.out.addDependent(this);
    }

    @Override
    public void trigger() {
      if (!triggered) {
        logger.info("Note off triggered: " + onEvent.note);
        onEvent.player.pause(true);
        // TODO: Figure out how to remove the triggers that have already fired.
        triggered = true;
      }
    }
  }

  private class TickEvents {

    private final Set<NoteOnEvent> noteOnEvents = new HashSet<>();
    private final Set<NoteOffEvent> noteOffEvents = new HashSet<>();

    public void addNoteOn(final NoteOnEvent event) {
      noteOnEvents.add(event);
    }

    public void addNoteOff(final NoteOffEvent event) {
      noteOffEvents.add(event);
    }

    public Set<NoteOnEvent> getNoteOnEvents() {
      return new HashSet<>(noteOnEvents);
    }

    public Set<NoteOffEvent> getNoteOffEvents() {
      return new HashSet<>(noteOffEvents);
    }
  }

  private class NoteOffEvent {
    private final MidiEvent event;
    private final Note note;

    public NoteOffEvent(final MidiEvent event, final Note note) {
      this.event = event;
      this.note = note;
    }
  }

  private class NoteOnEvent extends DelayEvent {
    private final double tickStart;
    private final Note note;
    private final SamplePlayer player;
    private boolean triggered = false;

    public NoteOnEvent(AudioContext context, double tickStart, Note note, SamplePlayer player) {
      super(context, tickStart);
      context.out.addDependent(this);
      this.tickStart = tickStart;
      this.note = note;
      this.player = player;
    }

    @Override
    public void trigger() {
      if (!triggered) {
        ac.out.addInput(player);
        player.start();
        logger.info("Note on triggered: " + this.note);
        triggered = true;
      }
    }
  }
}
