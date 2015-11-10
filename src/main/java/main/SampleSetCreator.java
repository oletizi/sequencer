package main;

import com.orionletizi.sequencer.Theory;
import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
import org.apache.commons.io.FileUtils;
import org.jfugue.theory.Note;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class SampleSetCreator {

  private static final Logger logger = LoggerImpl.forClass(SampleSetCreator.class);


  private final File directory;
  private final String startingNoteName;
  private final int startingNoteNumber;
  private final Note startingNote;

  public SampleSetCreator(File directory, String startingNote) {
    this.directory = directory;
    this.startingNote = new Note(startingNote);
    startingNoteName = startingNote.substring(0, startingNote.length() - 1);
    startingNoteNumber = Integer.parseInt(startingNote.substring(startingNote.length() - 1));
  }

  public static void main(String[] args) {
    final String directoryName = args[0];
    final String startingNote = args[1];
    final File directory = new File(directoryName);
    if (!directory.isDirectory()) {
      throw new RuntimeException(directoryName + " is not a directory.");
    }
    new SampleSetCreator(directory, startingNote).run();
  }

  private void run() {
    int i = 0;
    logger.info("Starting note name: " + startingNoteName);
    logger.info("Starting note number: " + startingNoteNumber);
    for (String filename : directory.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".wav");
      }
    })) {

      logger.info("Matching filename: " + filename);
      final int noteNumber = (i / Theory.NOTE_NAMES.length) + startingNoteNumber;
      final String basename = filename.substring(0, filename.indexOf('.'));
      final String noteName = Theory.NOTE_NAMES[i % Theory.NOTE_NAMES.length];
      final File originalFile = new File(directory, filename);
      final File destinationFile = new File(directory, basename + "-" + noteName + noteNumber + ".wav");
      logger.info("original file: " + originalFile + ", dest file: " + destinationFile);
      try {
        FileUtils.moveFile(originalFile, destinationFile);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      i++;
    }
  }
}
