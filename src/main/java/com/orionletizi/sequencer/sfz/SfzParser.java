package com.orionletizi.sequencer.sfz;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class SfzParser {

  private final CompositeObserver observer = new CompositeObserver();
  private States state;

  private enum States {
    group, region
  }


  public SfzParser addObserver(SfzParserObserver o) {
    observer.addObserver(o);
    return this;
  }

  public void parse(final File sfzFile) throws IOException {
    parse(new FileInputStream(sfzFile));
  }

  public void parse(final URL sfzResource) throws IOException {
    parse(sfzResource.openStream());
  }

  public void parse(final InputStream inputStream) throws IOException {
    final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
    String line = null;
    // TODO: This parser isn't very flexible. It should handle whitespace and comments and probably other stuff
    while ((line = in.readLine()) != null) {
      if (line.startsWith("<group>")) {
        state = States.group;
        observer.notifyGroup();
        line = line.substring("<group>".length());
      } else if (line.startsWith("<region>")) {
        state = States.region;
        observer.notifyRegion();
        line = line.substring("<region>".length());
      }

      if (line.startsWith("sample=")) {
        line = line.substring("sample=".length());
        observer.notifySample(line);
      } else if (line.startsWith("lokey=")) {
        line = line.substring("lokey=".length());
        observer.notifyLokey(line);
      } else if (line.startsWith("hikey=")) {
        line = line.substring("hikey=".length());
        observer.notifyHikey(line);
      } else if (line.startsWith("pitch_keycenter=")) {
        line = line.substring("pitch_keycenter=".length());
        observer.notifyPitchKeycenter(line);
      }
    }
  }


  private class CompositeObserver implements SfzParserObserver {

    private final Set<SfzParserObserver> observers = new HashSet<>();

    public void addObserver(SfzParserObserver observer) {
      observers.add(observer);
    }

    @Override
    public void notifyGroup() {
      for (SfzParserObserver observer : observers) {
        observer.notifyGroup();
      }
    }

    @Override
    public void notifyRegion() {
      for (SfzParserObserver observer : observers) {
        observer.notifyRegion();
      }
    }

    @Override
    public void notifySample(String sample) {
      for (SfzParserObserver observer : observers) {
        observer.notifySample(sample);
      }
    }

    @Override
    public void notifyLokey(String lokey) {
      for (SfzParserObserver observer : observers) {
        observer.notifyLokey(lokey);
      }
    }

    @Override
    public void notifyHikey(String hikey) {
      for (SfzParserObserver observer : observers) {
        observer.notifyHikey(hikey);
      }
    }

    @Override
    public void notifyPitchKeycenter(String pitchKeycenter) {
      for (SfzParserObserver observer : observers) {
        observer.notifyPitchKeycenter(pitchKeycenter);
      }
    }
  }
}
