package com.orionletizi.sampler.sfz;

import com.orionletizi.com.orionletizi.midi.MidiContext;
import com.sun.org.apache.bcel.internal.util.ClassLoader;
import org.junit.Before;
import org.junit.Test;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class SfzResamplerTestIT {

  private int millisBetweenNotes;
  private MidiContext midiContext;
  private SfzResampler resampler;
  private SfzSamplerProgram program;

  @Before
  public void before() throws Exception {
    final int sampleRate = 48 * 1000;
    final int ticksPerBeat = 96;
    final int tempo = 120;
    midiContext = new MidiContext(sampleRate, ticksPerBeat, tempo);

    final URL programResource = ClassLoader.getSystemResource("sfz/guitar/guitar-fixed.sfz");
    final File sampleBase = new File(programResource.getFile()).getParentFile();
    final SfzParserObserver observer = mock(SfzParserObserver.class);
    final SfzParser parser = new SfzParser();
    parser.addObserver(observer);
    program = new SfzSamplerProgram(parser, programResource, sampleBase);
    millisBetweenNotes = 1000;
    final long ticksBetweenNotes = midiContext.millisecondsToTicks(millisBetweenNotes);
    info("Ticks between notes: " + ticksBetweenNotes);
    resampler = new SfzResampler(midiContext, program, ticksBetweenNotes);

  }

  @Test
  public void testCreateProgram() throws Exception {
    ClassLoader.getSystemResource("audio/resampled-guitar.wav");
  }

  @Test
  public void testGenerateMidi() throws Exception {

    //final Track track = resampler.getTrack();
    final Sequence sequence = resampler.getSequence();
    assertNotNull(sequence);


    final Region[][] regions = program.getRegions();
    int regionCount = 0;
    for (Region[] regionsForNote : regions) {
      regionCount += regionsForNote.length;
    }

    //assertEquals(regionCount, track.size());

    final File outfile = new File("/tmp/midi-" + System.currentTimeMillis() + ".mid");
    MidiSystem.write(sequence, MidiSystem.getMidiFileTypes(sequence)[0], outfile);
    info("Write sequence to :" + outfile);
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

}