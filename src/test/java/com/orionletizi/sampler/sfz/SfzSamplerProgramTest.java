package com.orionletizi.sampler.sfz;

import com.sun.org.apache.bcel.internal.util.ClassLoader;
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
    program = new SfzSamplerProgram(programResource, programRoot);
  }

  @Test
  public void test() throws Exception {
    parser.addObserver(program);
    parser.parse(programResource);

    final File file = new File(ClassLoader.getSystemResource("sfz/mellotron/A2.wav").getFile());

    assertEquals(file, program.getSampleFileForNoteName("A2", (byte) 127));
    assertEquals(file, program.getSampleFileForNote(new Note("A2").getValue(), (byte) 127));
    final Sample expected = new Sample(file.getAbsolutePath());
    final Sample actual = program.getSampleForNote(new Note("A2").getValue(), (byte) 127);
    assertEquals(expected.getFileName(), actual.getFileName());
  }

  @Test
  public void testNoteAndVelocity() throws Exception {
    programRoot = new File(ClassLoader.getSystemResource("sfz/ibanezbass/").getFile());
    programResource = ClassLoader.getSystemResource("sfz/ibanezbass/ibanez-bass.sfz");
    program = new SfzSamplerProgram(programResource, programRoot);
    parser.addObserver(program);
    parser.parse(programResource);

    File file = new File(programRoot, "E_1.wav");
    testSampleFile((byte) 52, file, 111, 127);

    file = new File(programRoot, "E_2.wav");
    testSampleFile((byte) 52, file, 86, 110);

    file = new File(programRoot, "E_3.wav");
    testSampleFile((byte) 52, file, 71, 85);

    file = new File(programRoot, "E_4.wav");
    testSampleFile((byte) 52, file, 0, 70);
  }

  private void testSampleFile(byte note, File file, int lovel, int hivel) {
    for (int b = (byte) lovel; b <= hivel; b++) {
      assertEquals(file, program.getSampleFileForNote(note, (byte) b));
    }
  }
}