package com.orionletizi.sequencer;

import com.orionletizi.sampler.Sampler;
import com.orionletizi.sampler.sfz.SfzParser;
import com.orionletizi.sampler.sfz.SfzSamplerProgram;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.io.NonrealtimeIO;
import net.beadsproject.beads.ugens.RecordToFile;
import org.junit.Test;

import javax.sound.midi.Receiver;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SamplerSequencerTest {

  @Test
  public void test() throws Exception {
    URL midi = ClassLoader.getSystemResource("midi/drum_pattern_tempo_change.mid");
    URL programResource = ClassLoader.getSystemResource("program/drums/program.sfz");
    File programFile = new File(programResource.getPath());
    final SfzSamplerProgram program = new SfzSamplerProgram(new SfzParser(), programFile);
    //final AudioIO io = new JavaSoundAudioIO();
    final AudioIO io = new NonrealtimeIO();
    final AudioContext ac = new AudioContext(io);
    final Sampler sampler = new Sampler(ac, program);
    final List<Receiver> instruments = new ArrayList<>();
    instruments.add(sampler);
    final SamplerSequencer sequencer = new SamplerSequencer(ac, instruments, midi);
    final CountDownLatch latch = new CountDownLatch(1);
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

    final RecordToFile recorder = new RecordToFile(ac, 2, new File("/tmp/samplersequencer-" + System.currentTimeMillis() + ".wav"));
    recorder.addInput(ac.out);
    ac.out.addDependent(recorder);
    ac.start();
    latch.await();

    recorder.kill();
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }
}