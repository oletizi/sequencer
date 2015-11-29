package com.orionletizi.sampler;

import net.beadsproject.beads.data.Sample;

import java.io.File;

public interface SamplerProgram {
  File getSampleFileForNoteName(String noteString, byte velocity);

  File getSampleFileForNote(byte i, byte velocity);

  Sample getSampleForNote(byte i, byte velocity);
}
