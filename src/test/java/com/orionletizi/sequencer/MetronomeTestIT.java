package com.orionletizi.sequencer;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import org.junit.Before;
import org.junit.Test;

public class MetronomeTestIT {

  private AudioContext ac;

  @Before
  public void before() {
    final JavaSoundAudioIO io = new JavaSoundAudioIO();
    this.ac = new AudioContext(io);
  }

  @Test
  public void test() throws Exception {
    int tempo = 120;
    final Metronome metronome = new Metronome(ac, tempo);
    ac.start();
    metronome.start();

    synchronized (this) {
      wait();
    }
  }

}