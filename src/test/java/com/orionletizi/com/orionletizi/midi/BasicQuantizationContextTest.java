package com.orionletizi.com.orionletizi.midi;

import com.orionletizi.music.theory.Tempo;
import com.orionletizi.music.theory.TimeSignature;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class BasicQuantizationContextTest {

  private MidiContext midiContext;
  private double resolution;

  @Before
  public void before() throws Exception {
    TimeSignature timeSignature = mock(TimeSignature.class);
    double sampleRate = 1;
    int ticksPerBeat = 4;
    Tempo tempo = mock(Tempo.class);
    midiContext = new MidiContext(sampleRate, ticksPerBeat, tempo, timeSignature);
  }

  @Test
  public void testWholeNoteResolution() throws Exception {
    // resolution at 4 beats
    resolution = 4;
    QuantizationContext context = new BasicQuantizationContext(midiContext, resolution);
    assertEquals(0, context.getNearestTick(0));
    assertEquals(0, context.getNearestTick(7));
    assertEquals(16, context.getNearestTick(8));
    assertEquals(16, context.getNearestTick(16));
    assertEquals(16, context.getNearestTick(23));
    assertEquals(32, context.getNearestTick(24));
  }

  @Test
  public void testHalfNoteResolution() throws Exception {
    resolution = 2;
    QuantizationContext context = new BasicQuantizationContext(midiContext, resolution);
    assertEquals(0, context.getNearestTick(0));
    assertEquals(0, context.getNearestTick(3));
    assertEquals(8, context.getNearestTick(4));
    assertEquals(8, context.getNearestTick(5));
    assertEquals(8, context.getNearestTick(8));
    assertEquals(8, context.getNearestTick(9));
    assertEquals(8, context.getNearestTick(11));
    assertEquals(16, context.getNearestTick(12));
    assertEquals(16, context.getNearestTick(16));
  }

  @Test
  public void testQuarterNoteResolution() throws Exception {
    // test resolution at 1 beat
    resolution = 1;
    QuantizationContext context = new BasicQuantizationContext(midiContext, resolution);
    long actualTick = 0;
    long quantizedTick = context.getNearestTick(actualTick);
    assertEquals(0, quantizedTick);

    actualTick = 2;
    assertEquals(4, context.getNearestTick(actualTick));

    // At four ticks per beat, tick three should snap to the next beat, which is on tick 5
    actualTick = 3;
    assertEquals(4, context.getNearestTick(actualTick));
  }

  @Test
  public void testEighthNoteResolution() throws Exception {
    // test resolution at 0.5 beats
    resolution = 0.5;

    QuantizationContext context = new BasicQuantizationContext(midiContext, resolution);

    assertEquals(0, context.getNearestTick(0));

    assertEquals(2, context.getNearestTick(1));

    assertEquals(2, context.getNearestTick(2));

    assertEquals(4, context.getNearestTick(3));

    assertEquals(4, context.getNearestTick(4));

    assertEquals(6, context.getNearestTick(5));

    assertEquals(6, context.getNearestTick(6));
  }
}