package com.orionletizi.com.orionletizi.midi;

public class NullQuantizationContext implements QuantizationContext {
  @Override
  public long getNearestTick(long actualTick) {
    return actualTick;
  }
}
