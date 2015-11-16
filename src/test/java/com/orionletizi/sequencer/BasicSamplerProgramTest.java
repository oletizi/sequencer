package com.orionletizi.sequencer;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class BasicSamplerProgramTest {

  private File sourceDirectory;

  @Before
  public void before() throws Exception {
    sourceDirectory = new File(ClassLoader.getSystemResource("samples/piano").getFile());
  }

  @Test
  public void testLoad() throws Exception {
    final BasicSamplerProgram program = new BasicSamplerProgram(sourceDirectory);
    File sampleFile = program.getSampleFileForNoteName("A4");
    assertEquals("piano-A4.wav", sampleFile.getName());

    sampleFile = program.getSampleFileForNote((byte) 57);
    assertEquals("piano-A4.wav", sampleFile.getName());

  }
}