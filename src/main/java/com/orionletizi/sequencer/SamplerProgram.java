package com.orionletizi.sequencer;

import net.beadsproject.beads.data.Sample;

import java.io.File;

/**
 * Created by orion on 11/16/15.
 */
public interface SamplerProgram {
  File getSampleFileForNoteName(String noteString);

  File getSampleFileForNote(byte i);

  Sample getSampleForNote(byte i);
}
