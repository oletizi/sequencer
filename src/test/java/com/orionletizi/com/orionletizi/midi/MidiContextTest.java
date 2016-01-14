package com.orionletizi.com.orionletizi.midi;

import com.orionletizi.music.theory.Tempo;
import com.orionletizi.music.theory.TimeSignature;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MidiContextTest {

  private MidiContext ctxt;

  @Before
  public void before() throws Exception {
    double sampleRate = 1;
    int ticksPerBeat = 1;
    //double tempo = 1;
    Tempo tempo = Tempo.newTempoFromBPM(1);
    TimeSignature timeSignature = new TimeSignature(4, 4);
    ctxt = new MidiContext(sampleRate, ticksPerBeat, tempo, timeSignature);
  }

  @Test
  public void testTicksToBeats() throws Exception {
    assertEquals(1, ctxt.ticksToBeats(1), 0);
  }

  @Test
  public void testTicksToMilliseconds() throws Exception {
    // 1 tick = 1 beat = 1 min
    assertEquals(60 * 1000, ctxt.ticksToMillisecond(1), 0);
  }

}