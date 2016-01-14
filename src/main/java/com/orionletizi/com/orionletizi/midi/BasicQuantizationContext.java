package com.orionletizi.com.orionletizi.midi;

import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;

import java.math.BigDecimal;

public class BasicQuantizationContext implements QuantizationContext {
  private final static Logger logger = LoggerImpl.forClass(BasicQuantizationContext.class);
  private final MidiContext midiContext;
  private final double resolution;

  /**
   * @param midiContext            Describes the ticksPerBeat resolution, sample rate, tempo, time signature, etc.
   * @param quantizationResolution Describes the quantization resolution in beats. In common time, a value of 1 means
   *                               quarter notes will be quantized; 0.5 means eighth notes will be quantized; 4 means
   *                               whole notes will be quantized, etc.
   */
  public BasicQuantizationContext(final MidiContext midiContext, final double quantizationResolution) {
    this.midiContext = midiContext;
    this.resolution = quantizationResolution;
    assert resolution > 0;
  }

  @Override
  public long getNearestTick(long actualTick) {
    // x (actual tick) rounded to the nearest y (resolution):
    //
    //    y[x/y]
    //
    // where [z] (i.e., [x/y]) is the nearest integer to z
    final double beatAtTick = midiContext.ticksToBeats(actualTick);

    final BigDecimal quotient = new BigDecimal(beatAtTick).divide(new BigDecimal(resolution), BigDecimal.ROUND_HALF_UP);
    final long integerQuotient = Math.round(quotient.doubleValue());
    final double quantumBeat = integerQuotient * resolution;
    info("actual tick: " + actualTick + ", beatAtTick: " + beatAtTick + ", quotient: " + quotient + ", integerQuotient: " + integerQuotient + ", quantumBeat: " + quantumBeat);

    // now that we know the quantum beat, convert it back to a tick
    return midiContext.beatsToTick(quantumBeat);
  }

  private void info(String s) {
    logger.info(s);
  }
}
