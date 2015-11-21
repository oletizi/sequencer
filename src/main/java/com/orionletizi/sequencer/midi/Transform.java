package com.orionletizi.sequencer.midi;

import org.jfugue.theory.Note;

public interface Transform {
  Note transform(Note note);
}
