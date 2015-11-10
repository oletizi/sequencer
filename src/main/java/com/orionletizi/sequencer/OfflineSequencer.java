package com.orionletizi.sequencer;

import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
import org.jfugue.midi.MidiParser;
import org.jfugue.theory.Note;

import javax.sound.midi.MidiEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class OfflineSequencer extends MidiParser {

  private static final Logger logger = LoggerImpl.forClass(OfflineSequencer.class);

  private final Map<Byte, NoteEvent> onNotes = new HashMap<>();
  private final Map<Long, Set<NoteEvent>> tickEvents = new HashMap<>();

  private long maxTick;

  public void play() {
    for (long i = 0; i <= maxTick; i++) {
      final Set<NoteEvent> events = tickEvents.get(i);
      if (events != null) {
        for (NoteEvent event : events) {
          final long durationInTicks = event.tickDuration;
          final long durationInMs = this.ticksToMs(durationInTicks);
          logger.info("Play note: " + event.note + " for " + durationInMs + "ms");
        }
      }
    }
  }

  @Override
  public void fireNotePressed(MidiEvent event, Note note) {
    super.fireNotePressed(event, note);
    final long tick = event.getTick();
    logger.info("notePressed: tick: " + tick + ", " + note + ", duration: " + note.getDuration());
    logger.info("  ms: " + this.ticksToMs(tick));
    onNotes.put(note.getValue(), new NoteEvent(tick, note));
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

  private class NoteEvent {
    private final long tickStart;
    private long tickDuration;
    private final Note note;

    public NoteEvent(long tickStart, Note note) {
      this(tickStart, -1, note);
    }

    public NoteEvent(long tickStart, long tickDuration, Note note) {

      this.tickStart = tickStart;
      this.tickDuration = tickDuration;
      this.note = note;
    }

    public void setTickDuration(final long tickDuration) {
      this.tickDuration = tickDuration;
    }
  }
}
