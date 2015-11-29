package com.orionletizi.com.orionletizi.midi;

import org.jfugue.theory.Note;

public interface Transform {
  Note transform(Note note);
}
