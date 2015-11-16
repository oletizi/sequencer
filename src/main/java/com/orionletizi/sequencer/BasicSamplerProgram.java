package com.orionletizi.sequencer;

import com.orionletizi.sequencer.theory.TNote;
import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.NullLogger;
import net.beadsproject.beads.data.Sample;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class BasicSamplerProgram implements SamplerProgram {

  static final Logger logger = new NullLogger();//LoggerImpl.forClass(BasicSamplerProgram.class);

  final File[] noteFiles = new File[128];
  final Sample[] samples = new Sample[128];

  public BasicSamplerProgram(File directory) throws IOException {
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
      final File sampleFile = new File(directory, filename);
      noteFiles[note.getValue()] = sampleFile;
      samples[note.getValue()] = new Sample(sampleFile.getAbsolutePath());
    }
  }

  @Override
  public File getSampleFileForNoteName(String noteString) {
    return noteFiles[new TNote(noteString).getValue()];
  }

  @Override
  public File getSampleFileForNote(byte i) {
    return noteFiles[i];
  }

  @Override
  public Sample getSampleForNote(byte i) {
    return samples[i];
  }
}
