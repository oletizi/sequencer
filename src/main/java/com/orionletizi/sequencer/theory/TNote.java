package com.orionletizi.sequencer.theory;

import org.jfugue.theory.Note;

public class TNote {
  private final Note note;

  public TNote(final String noteName) {
    this.note = new Note(noteName);
  }

  public TNote(final byte noteValue) {
    this.note = new Note(noteValue);
  }

  public byte getValue() {
    return this.note.getValue();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof TNote && note.getValue() == ((TNote) obj).getValue();
  }

  @Override
  public int hashCode() {
    return getValue();
  }
}
