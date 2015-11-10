package main;

import com.orionletizi.sequencer.theory.TIntervals;
import com.orionletizi.sequencer.theory.TNote;
import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SampleSetCreator {

  private static final Logger logger = LoggerImpl.forClass(SampleSetCreator.class);


  private final File directory;
  private final TNote startingNote;

  public SampleSetCreator(File directory, String startingNote) {
    this.directory = directory;
    this.startingNote = new TNote(startingNote);
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
    logger.info("Starting note:" + startingNote);

    TNote baseNote = this.startingNote;
    final TIntervals chromatic = TIntervals.chromatic();
    chromatic.setRoot(baseNote);
    List<TNote> notes = chromatic.getNotes();
    final String[] filenames = directory.list(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(".wav");
      }
    });

    Arrays.sort(filenames, new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return parseFileNumber(o1) - parseFileNumber(o2);
      }
    });


    for (String filename : filenames) {
      if (i > 0 && i % 12 == 0) {
        baseNote = new TNote((byte) (baseNote.getValue() + 12));
        chromatic.setRoot(baseNote);
        notes.clear();
        notes.addAll(chromatic.getNotes());
        logger.info("next octave. i: " + i + ", new base note: " + baseNote + ", first note in list: " + notes.get(0));
      }
      logger.info("Matching filename: " + filename);
      logger.info("i=" + i + ", notes index: " + i % 12);
      final TNote note = notes.get(i % 12);
      logger.info("Note: " + note);
      final String basename = filename.substring(0, filename.indexOf('.'));
      final File originalFile = new File(directory, filename);
      final File destinationFile = new File(directory, basename + "-" + note + ".wav");
      logger.info("original file: " + originalFile + ", dest file: " + destinationFile);
      try {
        FileUtils.moveFile(originalFile, destinationFile);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      i++;
    }
  }

  private int parseFileNumber(String filename) {
    return Integer.parseInt(filename.substring(filename.indexOf('.') + 1, filename.lastIndexOf('.')));
  }
}
