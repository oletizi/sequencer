package com.orionletizi.sequencer.sfz;

public interface SfzParserObserver {
  void notifyGroup();

  void notifyRegion();

  void notifySample(String sample);

  void notifyLokey(String lokey);

  void notifyHikey(String hikey);

  void notifyPitchKeycenter(String pitchKeycenter);
}
