package com.orionletizi.sequencer;

import org.jfugue.theory.Note;

public class Theory {
  public static final String[] NOTE_NAMES = new String[]{
      "C",
      "C#",
      "D",
      "Eb",
      "E",
      "F",
      "F#",
      "G",
      "Ab",
      "A",
      "Bb",
      "B"
  };


  public static byte parseNoteValue(String noteSpec) {
    return new Note(noteSpec).getValue();
  }
}
