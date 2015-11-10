package com.orionletizi.sequencer;

import com.orionletizi.sequencer.theory.TNote;
import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;

import java.io.File;
import java.io.FilenameFilter;

public class SampleSet {

  static final Logger logger = LoggerImpl.forClass(SampleSet.class);

  final File[] noteFiles = new File[128];

  public SampleSet(File directory) {
    for (String filename : directory.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".wav");
      }
    })) {
      final String noteName = filename.substring(filename.indexOf('-') + 1, filename.indexOf(".wav"));
      logger.info("Note name: " + noteName);
      //final byte noteValue = new TNTheory.parseNoteValue(note);
      final TNote note = new TNote(noteName);
      logger.info("filename: " + filename + ", note: " + note);
      noteFiles[note.getValue()] = new File(directory, filename);
    }
  }

  public File getSampleFileForNoteName(String noteString) {
    return noteFiles[new TNote(noteString).getValue()];
  }

  public File getSampleFileForNote(byte i) {
    return noteFiles[i];
  }
}
