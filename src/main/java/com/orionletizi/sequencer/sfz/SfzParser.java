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
    while ((line = in.readLine()) != null) {
      line = stripLeadingWhitespace(line);
      if (line.startsWith("//")) {
        continue;
      } else if (line.startsWith("<group>")) {
        state = States.group;
        observer.notifyGroup();
        line = stripLeadingWhitespace(line.substring("<group>".length()));
      } else if (line.startsWith("<region>")) {
        state = States.region;
        observer.notifyRegion();
        line = stripLeadingWhitespace(line.substring("<region>".length()));
      }

      while (!"".equals(line)) {
        info("line: " + line);
        if (line.startsWith("sample=")) {
          line = line.substring("sample=".length());
          final String sample = nextToken(line);//line.substring(0, line.indexOf(' '));
          observer.notifySample(sample);
          line = shift(sample, line);
        } else if (line.startsWith("lokey=")) {
          line = line.substring("lokey=".length());
          final String lokey = nextToken(line);
          observer.notifyLokey(lokey);
          line = shift(lokey, line);
        } else if (line.startsWith("hikey=")) {
          line = line.substring("hikey=".length());
          final String hikey = nextToken(line);
          observer.notifyHikey(hikey);
          line = shift(hikey, line);
        } else if (line.startsWith("pitch_keycenter=")) {
          line = line.substring("pitch_keycenter=".length());
          final String keycenter = nextToken(line);
          observer.notifyPitchKeycenter(keycenter);
          line = shift(keycenter, line);
        } else {
          line = shift(line);
        }
      }
    }
  }

  private void info(String s) {
    System.out.println("INFO: " + s);
  }

  private String nextToken(String line) {
    final int nextSpace = line.indexOf(' ');
    return nextSpace != -1 ? line.substring(0, line.indexOf(' ')) : line;
  }

  private String shift(String line) {
    return shift(nextToken(line), line);
  }

  private String shift(String token, String line) {
    return stripLeadingWhitespace(line.replaceAll("^" + token, ""));
  }

  private String stripLeadingWhitespace(String line) {
    return line.replaceAll("^\\s+", "");
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
