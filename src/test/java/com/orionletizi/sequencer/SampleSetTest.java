package com.orionletizi.sequencer;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class SampleSetTest {

  private File sourceDirectory;

  @Before
  public void before() throws Exception {
    sourceDirectory = new File(ClassLoader.getSystemResource("samples/piano").getFile());
  }

  @Test
  public void testLoad() throws Exception {
    final SampleSet sampleSet = new SampleSet(sourceDirectory);
    File sampleFile = sampleSet.getSampleFileForNoteName("A4");
    assertEquals("piano-A4.wav", sampleFile.getName());

    sampleFile = sampleSet.getSampleFileForNote((byte) 57);
    assertEquals("piano-A4.wav", sampleFile.getName());

  }
}