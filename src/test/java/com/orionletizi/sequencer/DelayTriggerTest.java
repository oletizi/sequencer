package com.orionletizi.sequencer;

import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.DelayEvent;
import net.beadsproject.beads.ugens.SamplePlayer;
import org.junit.Test;

public class DelayTriggerTest {

  private static final Logger logger = LoggerImpl.forClass(DelayTriggerTest.class);

  @Test
  public void test() throws Exception {
    AudioIO io = new JavaSoundAudioIO();
    AudioContext ac = new AudioContext(io);

    Sample sample = new Sample(ClassLoader.getSystemResource("samples/piano/piano-A4.wav").getFile());
    SamplePlayer player = new MySamplePlayer(ac, sample);
    player.setLoopType(SamplePlayer.LoopType.LOOP_FORWARDS);
    //final RecordToFile recorder = new RecordToFile();

    DelayEvent trigger = new MyDelayEvent(ac, 1000);
    ac.out.addDependent(trigger);
    ac.out.addInput(player);

    logger.info("Starting audio context...");
    ac.start();
    logger.info("Audio context started.");

    synchronized (this) {
      wait();
    }

  }

  private class MySamplePlayer extends SamplePlayer {

    public MySamplePlayer(AudioContext context, Sample buffer) {
      super(context, buffer);
    }

    @Override
    public void calculateBuffer() {
      super.calculateBuffer();
    }
  }

  private class MyDelayEvent extends DelayEvent {

    private boolean untriggered = true;

    public MyDelayEvent(AudioContext context, double delay) {
      super(context, delay);
    }

    @Override
    public void trigger() {
      if (untriggered) {
        logger.info("I got triggered.");
        untriggered = false;
      }
    }

    @Override
    public void calculateBuffer() {
      super.calculateBuffer();
    }
  }
}
