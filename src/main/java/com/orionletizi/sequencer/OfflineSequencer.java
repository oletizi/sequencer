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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OfflineSequencer extends MidiParser {

  private static final Logger logger = LoggerImpl.forClass(OfflineSequencer.class);

  private final Map<Byte, NoteEvent> onNotes = new HashMap<>();
  private final Map<Long, Set<NoteEvent>> tickEvents = new HashMap<>();
  private final AudioContext ac;
  private final SampleSet sampleSet;

  private long maxTick;
  private int tempo;

  public OfflineSequencer(final AudioContext ac, final SampleSet sampleSet) {
    this.ac = ac;
    this.sampleSet = sampleSet;
  }

  public void play() {
    for (long i = 0; i <= maxTick; i++) {
      final Set<NoteEvent> events = tickEvents.get(i);
      if (events != null) {
        for (NoteEvent event : events) {
          final double durationInTicks = event.tickDuration;
          final double durationInMs = this.ticksToMs((long) durationInTicks);
          final double startTick = event.tickStart;
          final long startMs = this.ticksToMs((long) startTick);
          logger.info("Tick: " + startTick + ", ms: " + startMs + ", play note: " + event.note + " for " + durationInMs + "ms");
          logger.info("Play sample: " + sampleSet.getSampleFileForNote(event.note.getValue()));
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
    logger.info("notePressed: tick: " + tick + ", " + note + ", duration: " + note.getDuration());
    final long startTime = this.ticksToMs(tick);
    logger.info("  ms: " + startTime);
    final File sampleFile = sampleSet.getSampleFileForNote(note.getValue());
    try {
      final Sample sample = new Sample(sampleFile.getAbsolutePath());
      final SamplePlayer player = new SamplePlayer(ac, sample);
      final NoteEvent noteEvent = new NoteEvent(ac, (float) startTime, note, player);
      ac.out.addDependent(noteEvent);
      onNotes.put(note.getValue(), noteEvent);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void fireNoteReleased(MidiEvent event, Note note) {
    super.fireNoteReleased(event, note);
    final long tick = event.getTick();
    maxTick = Math.max(maxTick, tick);
    logger.info("noteReleased: tick: " + tick + ", note: " + note);
    final NoteEvent noteOn = onNotes.get(note.getValue());
    if (noteOn != null) {
      noteOn.setTickDuration(tick - noteOn.tickStart);
      Set<NoteEvent> eventsAtTick = tickEvents.get(tick);
      if (eventsAtTick == null) {
        eventsAtTick = new HashSet<>();
        tickEvents.put(tick, eventsAtTick);
      }

      eventsAtTick.add(noteOn);
    }
  }

  private class NoteEvent extends DelayEvent {
    private final double tickStart;
    private double tickDuration;
    private final Note note;
    private final SamplePlayer player;
    private boolean triggered = false;

    public NoteEvent(AudioContext context, double tickStart, Note note, SamplePlayer player) {
      super(context, tickStart);
      this.tickStart = tickStart;
      this.note = note;
      this.player = player;
    }

    public void setTickDuration(final double tickDuration) {
      this.tickDuration = tickDuration;
    }

    @Override
    public void trigger() {
      if (!triggered) {
        ac.out.addInput(player);
        player.start();
        logger.info("I got triggered!");
        triggered = true;
      }
    }
  }
}
