package com.orionletizi.sequencer;

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
      final String note = filename.substring(filename.indexOf('-') + 1, filename.indexOf(".wav"));
      final byte noteValue = Theory.parseNoteValue(note);
      logger.info("filename: " + filename + ", note: " + note + ", note value: " + noteValue);
      noteFiles[noteValue] = new File(directory, filename);
    }
  }

  public File getSampleFileForNoteName(String note) {
    return noteFiles[Theory.parseNoteValue(note)];
  }

  public File getSampleFileForNote(byte i) {
    return noteFiles[i];
  }
}
