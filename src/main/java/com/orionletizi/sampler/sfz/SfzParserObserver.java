package com.orionletizi.sampler.sfz;

import org.jfugue.theory.Note;

import java.io.IOException;

public interface SfzParserObserver {
  void notifyGroup();

  void notifyRegion();

  void notifySample(String sample) throws IOException;

  void notifyLokey(Note lokey);

  void notifyHikey(Note hikey);

  void notifyPitchKeycenter(Note pitchKeycenter);

  void notifyPitchKeytrack(int keytrack);

  void notifyKey(int key);

  void notifyHivel(int hivel);

  void notifyLovel(int lovel);

  void notifyGroupId(String groupNumber);

  void notifyLoopMode(String loopMode);

  void notifyOffBy(String offBy);
}
