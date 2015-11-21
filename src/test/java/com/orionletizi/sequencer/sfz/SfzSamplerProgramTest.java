package com.orionletizi.sequencer.sfz;

import net.beadsproject.beads.data.Sample;
import org.jfugue.theory.Note;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;


public class SfzSamplerProgramTest {

  private URL programResource;
  private SfzParser parser;
  private File programRoot;
  private SfzSamplerProgram program;

  @Before
  public void before() throws Exception {
    programRoot = new File(ClassLoader.getSystemResource("sfz/mellotron/").getFile());
    programResource = ClassLoader.getSystemResource("sfz/mellotron/mk2flute.sfz");
    parser = new SfzParser();
    program = new SfzSamplerProgram(programRoot);
  }

  @Test
  public void test() throws Exception {
    parser.addObserver(program);
    parser.parse(programResource);

    final File file = new File(ClassLoader.getSystemResource("sfz/mellotron/A2.wav").getFile());

    assertEquals(file, program.getSampleFileForNoteName("A2"));
    assertEquals(file, program.getSampleFileForNote(new Note("A2").getValue()));
    final Sample expected = new Sample(file.getAbsolutePath());
    final Sample actual = program.getSampleForNote(new Note("A2").getValue());
    assertEquals(expected.getFileName(), actual.getFileName());
  }
}