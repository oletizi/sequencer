package com.orionletizi.sequencer;

import net.beadsproject.beads.data.Sample;

import java.io.File;

/**
 * Created by orion on 11/16/15.
 */
public interface SamplerProgram {
  File getSampleFileForNoteName(String noteString, byte velocity);

  File getSampleFileForNote(byte i, byte velocity);

  Sample getSampleForNote(byte i, byte velocity);
}
