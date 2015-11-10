package com.orionletizi.sequencer;

import org.jfugue.temporal.TemporalEvents;

public interface NoteListener {
  void notifyNote(TemporalEvents.NoteEvent event);
}
