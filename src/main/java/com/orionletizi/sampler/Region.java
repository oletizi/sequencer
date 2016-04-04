package com.orionletizi.sampler;

import net.beadsproject.beads.data.Sample;

public interface Region extends Group {
  Sample getSample();

  void setSample(Sample sample);

  void setHivel(int hivel);

  void setLovel(int lovel);

  int getHivel();

  int getLovel();
}
