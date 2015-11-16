package com.orionletizi.sequencer.sfz;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class SfzParser {

  private final CompositeObserver observer = new CompositeObserver();

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
    String line;
    while ((line = in.readLine()) != null) {
      line = stripLeadingWhitespace(line);
      if (line.startsWith("//")) {
        continue;
      } else if (line.startsWith("<group>")) {
        observer.notifyGroup();
        line = shift("<group>", line);
      } else if (line.startsWith("<region>")) {
        observer.notifyRegion();
        line = shift("<region>", line);
      }

      while (!"".equals(line)) {
        if (line.startsWith("sample=")) {
          line = shift("sample=", line);
          final String sample = nextToken(line);
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

  private String nextToken(String line) {
    final int nextSpace = line.indexOf(' ');
    return nextSpace != -1 ? line.substring(0, line.indexOf(' ')) : line;
  }

  // removes everything from the beginning of the line through the next contiguous whitespace
  private String shift(String line) {
    return shift(nextToken(line), line);
  }

  // removes the token (if it's at the beginning of the line) through the next contiguous whitespace
  private String shift(String token, String line) {
    return stripLeadingWhitespace(line.replaceAll("^" + token, ""));
  }

  // removes all contiguous whitespace from the beginning of the line
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
