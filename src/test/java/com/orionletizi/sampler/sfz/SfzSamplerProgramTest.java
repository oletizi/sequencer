package com.orionletizi.sampler.sfz;

import com.orionletizi.sampler.SamplerProgram;
import com.sun.org.apache.bcel.internal.util.ClassLoader;
import net.beadsproject.beads.data.Sample;
import org.jfugue.theory.Note;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URL;
import java.util.Set;

import static org.junit.Assert.*;


public class SfzSamplerProgramTest {

  private File programRoot;
  private SfzSamplerProgram program;

  @Rule
  public TemporaryFolder tmp = new TemporaryFolder();

  private void before(String programRootPath, String programPath) throws Exception {
    programRoot = new File(ClassLoader.getSystemResource(programRootPath).getPath());
    URL programResource = ClassLoader.getSystemResource(programPath);

    SfzParser parser = new SfzParser();

    program = new SfzSamplerProgram(parser, new File(programResource.toURI()));
    parser.addObserver(program);
  }

  @Test
  public void testWithSpaces() throws Exception {
    before("sfz/guitar", "sfz/guitar-nki/New guitar.sfz");
  }

  @Test
  public void testCopyTo() throws Exception {
    before("sfz/guitar/", "sfz/guitar-nki/New guitar.sfz");
    final File destDir = tmp.newFolder();
    final File destProgramFile = new File(destDir, "program.sfz");
    assertFalse(destProgramFile.exists());

    final SamplerProgram copy = program.copyTo(destDir);
    assertNotNull(copy);
    assertTrue(destProgramFile.exists());
  }

  @Test
  public void testGuitar() throws Exception {
    before("sfz/guitar/", "sfz/guitar/guitar-fixed.sfz");
    final Sample sample = program.getSampleForNote((byte) 45, (byte) 127);
    final File expected = new File(programRoot, "samples/A_quarter_notes.02_02.wav");
    info("sample file: " + sample.getFileName());
    assertEquals(expected.getAbsolutePath(), sample.getFileName());
  }

  @Test
  @Ignore
  public void testBigMono() throws Exception {
    before("sfz/bigmono", "sfz/bigmono/sfz/Big Mono ndk.sfz");
  }

  @Test
  @Ignore("The note off test doesn't seem to be working right.")
  public void testDrums() throws Exception {
    before("program/drums/", "program/drums/program.sfz");

    Set<Integer> offNotes = program.getOffNotesForNoteOn(42);
    info("off notes: " + offNotes);
    assertEquals(2, offNotes.size());
    assertTrue(offNotes.contains(44));
    assertTrue(offNotes.contains(46));

    offNotes = program.getOffNotesForNoteOff(32, 100);
    // except for the hi hats, all note offs should be ignored
    assertTrue(offNotes.isEmpty());

    // hihats: 42, 44, 46
    offNotes = program.getOffNotesForNoteOff(42, 100);
    info("off notes: " + offNotes);
    assertEquals(1, offNotes.size());
    assertTrue(offNotes.contains(42));
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

  @Test
  @Ignore("The note off test doesn't seem to be working right.")
  public void test() throws Exception {
    before("sfz/mellotron/", "sfz/mellotron/mk2flute.sfz");
    final File file = new File(ClassLoader.getSystemResource("sfz/mellotron/A2.wav").getFile());

    final byte note = new Note("A2").getValue();
    assertEquals(file.getAbsolutePath(), program.getSampleForNote(note, 127).getFileName());
    final Sample actual = program.getSampleForNote(note, (byte) 127);
    assertEquals(file.getAbsolutePath(), actual.getFileName());

    final Set<Integer> offNotes = program.getOffNotesForNoteOff(note, 100);
    assertEquals(1, offNotes.size());
    assertTrue(offNotes.contains(note));
  }

  @Test
  public void testNoteAndVelocity() throws Exception {
    before("sfz/ibanezbass", "sfz/ibanezbass/ibanez-bass.sfz");

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

      final Sample sample = program.getSampleForNote(note, b);
      assertNotNull("No sample found for note: " + note + ", lovel: " + lovel + ", hivel: " + hivel, sample);
      assertEquals(file.getAbsolutePath(), sample.getFileName());
    }
  }
}