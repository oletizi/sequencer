package com.orionletizi.sequencer;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.ugens.Clock;

public class Metronome extends Bead {

  private final int tempo;
  private final Clock clock;

  public Metronome(AudioContext ac, int tempo) {
    this.tempo = tempo;
    this.clock = new Clock(ac, 1000 * 60 / tempo);
    clock.setClick(true);
    //ac.out.addInput(clock);
    ac.out.addDependent(clock);
  }

  @Override
  public void start() {
    super.start();
    clock.start();
  }
}