package com.orionletizi.sequencer;

import com.orionletizi.sampler.Sampler;
import com.orionletizi.sampler.sfz.SfzParser;
import com.orionletizi.sampler.sfz.SfzSamplerProgram;
import com.orionletizi.util.logging.LoggerImpl;
import com.sun.org.apache.bcel.internal.util.ClassLoader;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.RecordToFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sound.midi.Receiver;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static com.orionletizi.util.Assertions.assertTrue;
import static org.junit.Assert.assertEquals;

public class SequencerTest {

  private AudioContext ac;
  private RecordToFile recorder;
  private File outfile;

  @Before
  public void before() throws Exception {
    LoggerImpl.turnOff(SfzSamplerProgram.class);

    final AudioIO io = new NonrealtimeIO();
    //final AudioIO io = new JavaSoundAudioIO();
    ac = new AudioContext(io);

    outfile = new File("/tmp/sampler-" + System.currentTimeMillis() + ".wav");
    recorder = new RecordToFile(ac, 2, outfile);
    ac.out.addDependent(recorder);
    recorder.addInput(ac.out);
  }

  @After
  public void after() throws Exception {
    ac.stop();
    System.out.println("Stopping recorder.");
    recorder.kill();
    System.out.println("Outfile: " + outfile);
    assertTrue(outfile.isFile());
    // test the output file
    final Sample sample = new Sample(outfile.getAbsolutePath());
    assert (sample.getLength() > 0);
    assertEquals(2, sample.getNumChannels());
  }

  @Test
  public void testPlayBigMono() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/drum_pattern.mid"),
        new File(ClassLoader.getSystemResource("sfz/bigmono/sfz/Big Mono ndk.sfz").toURI()));
  }

  @Test
  public void testPlayGuitar() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/michael-guitar.mid"), new File(ClassLoader.getSystemResource("sfz/guitar/guitar-fixed.sfz").getFile())
    );
  }

  @Test
  public void testPlayIbanezBass() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/bass3.mid"), new File(ClassLoader.getSystemResource("sfz/ibanezbass/ibanez-bass.sfz").getFile())
    );
  }

  @Test
  public void testPlayMellotron() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/wurli5.mid"), new File(ClassLoader.getSystemResource("sfz/mellotron/mk2flute.sfz").getFile())
    );
  }

  @Test
  public void testPlayProspector() throws Exception {
    testPlay(ClassLoader.getSystemResource("midi/prospector1.mid"), new File("/Users/orion/audiogen-data/data/songs/prospector/prospector.sfz")
    );
  }

  private void testPlay(final URL midiSource, final File programFile) throws Exception {

    final CountDownLatch latch = new CountDownLatch(1);

    final SfzSamplerProgram program = new SfzSamplerProgram(new SfzParser(), programFile);

    final List<Receiver> instruments = new ArrayList<>();
    final Sampler sampler = new Sampler(ac, program);
    instruments.add(sampler);

    Sequencer sequencer = new Sequencer(ac, instruments, midiSource);
    final Bead endListener = new Bead() {
      @Override
      protected void messageReceived(Bead message) {
        super.messageReceived(message);
        if (message == sequencer) {
          info("Recieved completion message: " + message);
          ac.stop();
          latch.countDown();
          info("Done counting down the latch.");
        }
      }
    };
    sequencer.addEndListener(endListener);
    ac.out.addDependent(sequencer);

    ac.start();
    latch.await();
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

}