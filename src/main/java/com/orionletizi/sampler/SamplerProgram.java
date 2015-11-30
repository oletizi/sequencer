package com.orionletizi.sampler;

import net.beadsproject.beads.data.Sample;

import java.util.Set;

public interface SamplerProgram {
  Sample getSampleForNote(byte i, byte velocity);

  /**
   * Returns all of the notes that, if they are currently playing, should be turned off when the given note off
   * event occurs. This allows support for continuing to play a note even if a note off occurs and for turning off
   * multiple notes when a given note off occurs (e.g., hh groups)
   *
   * @param note
   * @return
   */
  Set<Byte> getOffNotesForNoteOff(byte note, byte onVelocity);

  Set<Byte> getOffNotesForNoteOn(byte note);
}
