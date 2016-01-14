package com.orionletizi.sampler.sfz;

import com.orionletizi.com.orionletizi.midi.MidiContext;
import com.orionletizi.music.theory.Tempo;
import com.orionletizi.music.theory.TimeSignature;
import com.sun.org.apache.bcel.internal.util.ClassLoader;
import org.junit.Before;
import org.junit.Test;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.net.URL;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class SfzResamplerTestIT {

  private SfzResampler resampler;
  private SfzSamplerProgram program;

  @Before
  public void before() throws Exception {
    final int sampleRate = 48 * 1000;
    final int ticksPerBeat = 96;
    final Tempo tempo = Tempo.newTempoFromBPM(120);
    final TimeSignature timeSignature = new TimeSignature(4, 4);
    MidiContext midiContext = new MidiContext(sampleRate, ticksPerBeat, tempo, timeSignature);

    final URL programResource = ClassLoader.getSystemResource("sfz/guitar/guitar-fixed.sfz");
    final File programFile = new File(programResource.getPath());
    final SfzParserObserver observer = mock(SfzParserObserver.class);
    final SfzParser parser = new SfzParser();
    parser.addObserver(observer);
    program = new SfzSamplerProgram(parser, programFile);
    int millisBetweenNotes = 1000;
    final int ticksBetweenNotes = (int) midiContext.millisecondsToTicks(millisBetweenNotes);
    info("Ticks between notes: " + ticksBetweenNotes);
    resampler = new SfzResampler(midiContext, program, ticksBetweenNotes);

  }


  @Test
  public void test() throws Exception {

    //final Track track = resampler.getTrack();
    final Sequence sequence = resampler.getSequence();
    assertNotNull(sequence);


    final Region[][] regions = program.getRegions();
    final File outfile = new File(System.getProperty("user.home") + "/tmp/midi-" + System.currentTimeMillis() + ".mid");
    MidiSystem.write(sequence, MidiSystem.getMidiFileTypes(sequence)[0], outfile);
    info("Wrote sequence to :" + outfile);

    final URL source = ClassLoader.getSystemResource("audio/resampled-guitar.wav");
    final File dest = new File(System.getProperty("user.home") + "/tmp/resampled-" + System.currentTimeMillis());
    final File destProgramFile = new File(dest, dest.getName() + ".sfz");
    assertFalse(dest.exists());

    resampler.createNewProgram(source, dest);

    assertTrue(dest.isDirectory());
    assertTrue(destProgramFile.isFile());
    info("Wrote program to " + dest);

    // load the program we just created and check it against the source program
    final SfzSamplerProgram newProgram = new SfzSamplerProgram(
        new SfzParser(),
        destProgramFile);

    final Region[][] newRegions = newProgram.getRegions();
    assertEquals(regions.length, newRegions.length);

    for (int i = 0; i < regions.length; i++) {
      assertEquals(regions[i].length, newRegions[i].length);
      for (int j = 0; j < regions[i].length; j++) {
        final Region sourceRegion = regions[i][j];
        final Region destRegion = newRegions[i][j];
        if (sourceRegion != null) {
          assertEquals(sourceRegion.getLovel(), destRegion.getLovel());
          assertEquals(sourceRegion.getHivel(), destRegion.getHivel());
          assertEquals(sourceRegion.getLokey(), destRegion.getLokey());
          assertEquals(sourceRegion.getHikey(), destRegion.getHikey());
        }
      }
    }
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

}