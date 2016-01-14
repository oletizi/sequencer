package com.orionletizi.com.orionletizi.midi;

public interface QuantizationContext {
  /**
   * @param actualTick The actual tick of the event in question
   * @return The nearest quantized tick.
   */
  long getNearestTick(long actualTick);
}
