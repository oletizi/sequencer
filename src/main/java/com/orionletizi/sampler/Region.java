package com.orionletizi.sampler;

import net.beadsproject.beads.data.Sample;

public interface Region extends Group {
  Sample getSample();

  void setSample(Sample sample);

  void setHivel(int hivel);

  void setLovel(int lovel);

  double getStart();

  void setStart(double start);

  double getEnd();

  void setEnd(double end);

  int getHivel();

  int getLovel();

  int getPitchKeytrack();

  void setPitchKeytrack(int keytrack);
}
