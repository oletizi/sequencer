package com.orionletizi.sampler;

import net.beadsproject.beads.data.Sample;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public interface SamplerProgram {
  Sample getSampleForNote(int note, int velocity);

  /**
   * Returns all of the notes that, if they are currently playing, should be turned off when the given note off
   * event occurs. This allows support for continuing to play a note even if a note off occurs and for turning off
   * multiple notes when a given note off occurs (e.g., hh groups)
   *
   * @param note the midi byte for the note in question
   * @return all midi bytes for notes that should be turned off by this note
   */
  Set<Integer> getOffNotesForNoteOff(int note, int onVelocity);

  Set<Integer> getOffNotesForNoteOn(int note);

  SamplerProgram copyTo(File destinationDirectory) throws IOException;
}
