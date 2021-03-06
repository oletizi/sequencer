package com.orionletizi.sequencer;

import com.orionletizi.com.orionletizi.midi.Transpose;
import com.orionletizi.sampler.sfz.SfzParser;
import com.orionletizi.sampler.sfz.SfzSamplerProgram;
import com.sun.media.sound.StandardMidiFileReader;
import com.sun.org.apache.bcel.internal.util.ClassLoader;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import net.beadsproject.beads.ugens.RecordToFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sound.midi.Sequence;
import java.io.File;
import java.net.URL;

public class SequencerTestIT {

  //private URL midiSource;
  private StandardMidiFileReader reader;
  // private Sequence sequence;
  //private Sequencer sequencer;
  //private File sampleDirectory;
  private AudioContext ac;
  private RecordToFile recorder;
  //private File programFile;
  //private SfzSamplerProgram program;

  @Before
  public void before() throws Exception {
    AudioIO io = new JavaSoundAudioIO();
    ac = new AudioContext(io);

    final File outfile = new File("/tmp/sampler-" + System.currentTimeMillis() + ".wav");
    recorder = new RecordToFile(ac, 2, outfile);
    ac.out.addDependent(recorder);
    recorder.addInput(ac.out);

    reader = new StandardMidiFileReader();

  }

  @After
  public void after() throws Exception {
    System.out.println("Stopping recorder.");
    recorder.kill();
  }

  @Test
  public void testPlayGuitar() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/michael-guitar.mid"), new File(ClassLoader.getSystemResource("sfz/guitar/guitar-fixed.sfz").getFile()),
        new Transpose(2 * 12));
  }

  @Test
  public void testPlayIbanezBass() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/bass3.mid"), new File(ClassLoader.getSystemResource("sfz/ibanezbass/ibanez-bass.sfz").getFile()),
        new Transpose(0));
  }

  @Test
  public void testPlayMellotron() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/wurli5.mid"), new File(ClassLoader.getSystemResource("sfz/mellotron/mk2flute.sfz").getFile()),
        new Transpose(-12));
  }

  @Test
  public void testPlayProspector() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/prospector1.mid"), new File("/Users/orion/audiogen-data/data/songs/prospector/prospector.sfz"),
        new Transpose(-12));
  }

  private void testPlay(final URL midiSource, final File programFile, Transpose transpose) throws Exception {

    SfzSamplerProgram program = new SfzSamplerProgram(new SfzParser(), programFile);
    new SfzParser().addObserver(program).parse(programFile);

    Sequence sequence = reader.getSequence(midiSource);
    // the transposition here is because Logic Pro seems to export midi notes higher than expected.
    Sequencer sequencer = new Sequencer(ac, program, transpose);

    sequencer.startParser();
    sequencer.parse(sequence);
    sequencer.startParser();
    sequencer.play();

    synchronized (this) {
      wait(30 * 1000);
    }
  }

}