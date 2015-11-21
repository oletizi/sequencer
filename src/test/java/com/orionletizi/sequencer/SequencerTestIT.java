package com.orionletizi.sequencer;

import com.orionletizi.sequencer.midi.Transpose;
import com.orionletizi.sequencer.sfz.SfzParser;
import com.orionletizi.sequencer.sfz.SfzSamplerProgram;
import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
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

  private static final Logger logger = LoggerImpl.forClass(SequencerTestIT.class);

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
  public void testPlayIbanezBass() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/bass1.mid"), new File(ClassLoader.getSystemResource("sfz/ibanezbass/ibanez-bass.sfz").getFile()));
  }

  @Test
  public void testPlayMellotron() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/wurli5.mid"), new File(ClassLoader.getSystemResource("sfz/mellotron/mk2flute.sfz").getFile()));
  }

  @Test
  public void testPlayProspector() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/prospector1.mid"), new File("/Users/orion/audiogen-data/data/songs/prospector/prospector.sfz"));
  }

  private void testPlay(final URL midiSource, final File programFile) throws Exception {

    final File sampleDirectory = programFile.getParentFile();
    //final File programFile = new File(sampleDirectory, "mk2flute.sfz");
    SfzSamplerProgram program = new SfzSamplerProgram(programFile.getParentFile());
    new SfzParser().addObserver(program).parse(programFile);

    Sequence sequence = reader.getSequence(midiSource);
    // the transposition here is because Logic Pro seems to export midi notes higher than expected.
    Sequencer sequencer = new Sequencer(ac, program, new Transpose(-12));

    sequencer.startParser();
    sequencer.parse(sequence);
    sequencer.startParser();
    sequencer.play();
    synchronized (this) {
      wait(10 * 1000);
    }
  }

}