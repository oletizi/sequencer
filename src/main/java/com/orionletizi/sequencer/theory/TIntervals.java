package com.orionletizi.sequencer.theory;

import org.jfugue.theory.Intervals;
import org.jfugue.theory.Note;

import java.util.ArrayList;
import java.util.List;

public class TIntervals {

  private final Intervals intervals;

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
}
