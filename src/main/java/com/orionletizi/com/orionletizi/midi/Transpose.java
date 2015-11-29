package com.orionletizi.com.orionletizi.midi;

import org.jfugue.theory.Note;

public class Transpose implements Transform {

  private final byte offset;

  public Transpose(final int offset) {
    this.offset = (byte) offset;
  }

  @Override
  public Note transform(Note note) {
    return new Note((byte) (note.getValue() + offset));
  }
}
