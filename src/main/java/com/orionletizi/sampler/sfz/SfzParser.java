package com.orionletizi.sampler.sfz;

import org.jfugue.theory.Note;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SfzParser {

  public static final String SAMPLE_FILENAME_TERMINATOR = ".wav";

  private enum Scope {
    global,
    declaration,
    group,
    region
  }

  private final CompositeObserver composite = new CompositeObserver();
  private final Map<String, String> variables = new HashMap<>();
  private int currentLine;
  private Scope scope = Scope.global;

  public SfzParser addObserver(SfzParserObserver o) {
    composite.addObserver(o);
    return this;
  }

  public void parse(final File sfzFile) throws IOException, SfzParserException {
    parse(new FileInputStream(sfzFile));
  }

  public void parse(final URL sfzResource) throws IOException, SfzParserException {
    assert sfzResource != null;
    info("opening stream for url: " + sfzResource);
    parse(sfzResource.openStream());
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

  public void parse(final InputStream inputStream) throws IOException, SfzParserException {
    info("parsing input stream...");
    final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    currentLine = 0;
    while ((line = in.readLine()) != null) {
      currentLine++;
      line = stripLeadingWhitespace(line);
      if (line.startsWith("//")) {
        continue;
      } else if (line.startsWith("#define")) {
        scope = Scope.declaration;
        line = shift(line);
        final String declaration = nextToken(line);
        line = shift(line);
        final String value = nextToken(line);
        line = shift(line);
        variables.put(declaration, value);
        scope = Scope.global;
      } else if (line.startsWith("<group>")) {
        scope = Scope.group;
        composite.notifyGroup();
        line = shift("<group>", line);
      } else if (line.startsWith("<region>")) {
        scope = Scope.region;
        composite.notifyRegion();
        line = shift("<region>", line);
      }

      String previousLine = null;
      while (!"".equals(line)) {
        if (line.equals(previousLine)) {
          throw new SfzParserException("Error at line " + currentLine + ": " + line);
        }
        previousLine = line;
        if (line.startsWith("sample=")) {
          line = shift("sample=", line);
          final String sample = line.substring(0, line.indexOf(SAMPLE_FILENAME_TERMINATOR) + SAMPLE_FILENAME_TERMINATOR.length());
          composite.notifySample(sample);
          line = shift(sample, line);
        } else if (line.startsWith("key=")) {
          line = line.substring("key=".length());
          final String key = nextToken(line);
          composite.notifyKey(Integer.valueOf(key));
          line = shift(key, line);
        } else if (line.startsWith("lokey=")) {
          line = line.substring("lokey=".length());
          final String lokey = nextToken(line);
          composite.notifyLokey(getNoteFor(lokey));
          line = shift(lokey, line);
        } else if (line.startsWith("hikey=")) {
          line = line.substring("hikey=".length());
          final String hikey = nextToken(line);
          composite.notifyHikey(getNoteFor(hikey));
          line = shift(hikey, line);
        } else if (line.startsWith("pitch_keycenter=")) {
          line = line.substring("pitch_keycenter=".length());
          final String keycenter = nextToken(line);
          composite.notifyPitchKeycenter(getNoteFor(keycenter));
          line = shift(keycenter, line);
        } else if (line.startsWith("hivel=")) {
          line = line.substring("hivel=".length());
          final String hivel = nextToken(line);
          composite.notifyHivel(Integer.valueOf(hivel));
          line = shift(hivel, line);
        } else if (line.startsWith("lovel=")) {
          line = line.substring("lovel=".length());
          final String lovel = nextToken(line);
          composite.notifyLovel(Integer.valueOf(lovel));
          line = shift(lovel, line);
        } else if (line.startsWith("group=")) {
          line = line.substring("group=".length());
          final String groupNumber = nextToken(line);
          composite.notifyGroupId(groupNumber);
          line = shift(groupNumber, line);
        } else if (line.startsWith("loop_mode")) {
          line = line.substring("loop_mode=".length());
          final String loopMode = nextToken(line);
          composite.notifyLoopMode(loopMode);
          line = shift(loopMode, line);
        } else if (line.startsWith("off_by")) {
          line = line.substring("off_by=".length());
          final String offBy = nextToken(line);
          composite.notifyOffBy(offBy);
          line = shift(offBy, line);
        } else if (line.startsWith("pitch_keytrack")) {
          line = line.substring("pitch_keytrack=".length());
          final String keytrack = nextToken(line);
          composite.notifyPitchKeytrack(Integer.parseInt(keytrack));
          line = shift(keytrack, line);
        } else {
          line = shift(line);
        }
      }
    }
  }

  private Note getNoteFor(String noteString) {
    try {
      final int value = Integer.valueOf(noteString);
      return new Note(value);
    } catch (NumberFormatException e) {
      // not an int. We'll try it as a note symbol.
    }
    return new Note(noteString);
  }


  private String nextToken(String line) throws SfzParserException {
    final int nextSpace = line.indexOf(' ');
    String rv = nextSpace != -1 ? line.substring(0, line.indexOf(' ')) : line;
    if (rv.startsWith("$") && !scope.equals(Scope.declaration)) {
      final String variable = rv;
      rv = variables.get(rv);
      if (rv == null) {
        throw new SfzParserException("Undeclared variable: " + variable + " at line " + currentLine);
      }
    }
    return rv;
  }

  // removes everything from the beginning of the line through the next contiguous whitespace
  private String shift(String line) throws SfzParserException {
    return shift(nextToken(line), line);
  }

  // removes the token (if it's at the beginning of the line) through the next contiguous whitespace
  private String shift(String token, String line) throws SfzParserException {
    if (line.startsWith(token)) {
      line = line.substring(line.indexOf(token) + token.length());
    } else if (line.startsWith("$")) {
      // this was an interpolated variable--the token passed in will be the interpolated value; shift to next whitespace
      // XXX: This recursion could result in stack overflow.
      line = line.replaceFirst("\\$", "");
      line = shift(line);
    }
    return stripLeadingWhitespace(line);
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
    public void notifyLokey(Note lokey) {
      for (SfzParserObserver observer : observers) {
        observer.notifyLokey(lokey);
      }
    }

    @Override
    public void notifyHikey(Note hikey) {
      for (SfzParserObserver observer : observers) {
        observer.notifyHikey(hikey);
      }
    }

    @Override
    public void notifyPitchKeycenter(Note pitchKeycenter) {
      for (SfzParserObserver observer : observers) {
        observer.notifyPitchKeycenter(pitchKeycenter);
      }
    }

    @Override
    public void notifyPitchKeytrack(int keytrack) {
      for (SfzParserObserver observer : observers) {
        observer.notifyPitchKeytrack(keytrack);
      }

    }

    @Override
    public void notifyKey(int key) {
      for (SfzParserObserver observer : observers) {
        observer.notifyKey(key);
      }
    }

    @Override
    public void notifyHivel(int hivel) {
      for (SfzParserObserver observer : observers) {
        observer.notifyHivel(hivel);
      }
    }

    @Override
    public void notifyLovel(int lovel) {
      for (SfzParserObserver observer : observers) {
        observer.notifyLovel(lovel);
      }
    }

    @Override
    public void notifyGroupId(String groupNumber) {
      for (SfzParserObserver observer : observers) {
        observer.notifyGroupId(groupNumber);
      }

    }

    @Override
    public void notifyLoopMode(String loopMode) {
      for (SfzParserObserver observer : observers) {
        observer.notifyLoopMode(loopMode);
      }
    }

    @Override
    public void notifyOffBy(String offBy) {
      for (SfzParserObserver observer : observers) {
        observer.notifyOffBy(offBy);
      }
    }
  }
}
