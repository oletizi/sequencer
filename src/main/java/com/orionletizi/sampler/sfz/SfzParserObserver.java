package com.orionletizi.sampler.sfz;

import org.jfugue.theory.Note;

public interface SfzParserObserver {
  void notifyGroup();

  void notifyRegion();

  void notifySample(String sample);

  void notifyLokey(Note lokey);

  void notifyHikey(Note hikey);

  void notifyPitchKeycenter(Note pitchKeycenter);

  void notifyKey(byte key);

  void notifyHivel(byte hivel);

  void notifyLovel(byte lovel);

  void notifyGroupNumber(String groupNumber);

  void notifyLoopMode(String loopMode);

  void notifyOffBy(String offBy);
}