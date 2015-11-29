package com.orionletizi.sampler.sfz;

import com.sun.org.apache.bcel.internal.util.ClassLoader;
import net.beadsproject.beads.data.Sample;
import org.jfugue.theory.Note;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SfzSamplerProgramTest {

  private URL programResource;
  private SfzParser parser;
  private File programRoot;
  private SfzSamplerProgram program;

  private void before(String programRootPath, String programPath) throws Exception {
    programRoot = new File(ClassLoader.getSystemResource(programRootPath).getFile());//new File(ClassLoader.getSystemResource("sfz/mellotron/").getFile());
    programResource = ClassLoader.getSystemResource(programPath);//ClassLoader.getSystemResource("sfz/mellotron/mk2flute.sfz");

    parser = new SfzParser();
    program = new SfzSamplerProgram(parser, programResource, programRoot);
    parser.addObserver(program);
  }

  @Test
  public void testDrums() throws Exception {
    before("program/drums/", "program/drums/program.sfz");

    Set<Byte> offNotes = program.getOffNotesForNoteOn((byte) 42, (byte) 100);
    info("off notes: " + offNotes);
    assertEquals(2, offNotes.size());
    assertTrue(offNotes.contains((byte) 44));
    assertTrue(offNotes.contains((byte) 46));

    offNotes = program.getNotesForNoteOff((byte) 32, (byte) 100, (byte) 100);
    // except for the hi hats, all note offs should be ignored
    assertTrue(offNotes.isEmpty());

    // hihats: 42, 44, 46
    offNotes = program.getNotesForNoteOff((byte) 42, (byte) 100, (byte) 100);
    info("off notes: " + offNotes);
    assertEquals(1, offNotes.size());
    assertTrue(offNotes.contains((byte) 42));
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

  @Test
  public void test() throws Exception {
    before("sfz/mellotron/", "sfz/mellotron/mk2flute.sfz");
    final File file = new File(ClassLoader.getSystemResource("sfz/mellotron/A2.wav").getFile());

    assertEquals(file, program.getSampleFileForNoteName("A2", (byte) 127));
    final byte note = new Note("A2").getValue();
    assertEquals(file, program.getSampleFileForNote(note, (byte) 127));
    final Sample expected = new Sample(file.getAbsolutePath());
    final Sample actual = program.getSampleForNote(note, (byte) 127);
    assertEquals(expected.getFileName(), actual.getFileName());

    final Set<Byte> offNotes = program.getNotesForNoteOff(note, (byte) 100, (byte) 100);
    assertEquals(1, offNotes.size());
    assertTrue(offNotes.contains(note));
  }

  @Test
  public void testNoteAndVelocity() throws Exception {
    before("sfz/ibanezbass/", "sfz/ibanezbass/ibanez-bass.sfz");

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