package com.orionletizi.sequencer.theory;

import org.jfugue.theory.Intervals;
import org.jfugue.theory.Note;

import java.util.ArrayList;
import java.util.List;

public class TIntervals {

  private static final String CHROMATIC = "1 b2 2 b3 3 4 b5 5 b6 6 b7 7";

  private final Intervals intervals;
  private final TNote[] notes = new TNote[128];

  public TIntervals(final String intervalString) {
    this.intervals = new Intervals(intervalString);
  }

  public void setRoot(TNote root) {
    intervals.setRoot(new Note(root.getValue()));
  }

  public List<TNote> getNotes() {
    final List<Note> notes = intervals.getNotes();
    final List<TNote> rv = new ArrayList(notes.size());
    for (Note note : notes) {
      rv.add(new TNote(note.getValue()));
    }
    return rv;
  }

  public static TIntervals chromatic() {
    return new TIntervals(CHROMATIC);
  }
}
