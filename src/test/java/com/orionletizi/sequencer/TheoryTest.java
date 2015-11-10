package com.orionletizi.sequencer;

import com.orionletizi.sequencer.theory.TIntervals;
import com.orionletizi.sequencer.theory.TNote;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TheoryTest {
  @Test
  public void test() {
    final TIntervals chromatic = TIntervals.chromatic();

    TNote root = new TNote("G4");
    chromatic.setRoot(root);
    final List<TNote> notes = chromatic.getNotes();

    final TNote rootNote = notes.get(0);
    System.out.println("notes length: " + notes.size() + ", notes 0: " + rootNote);
    assertEquals(new TNote("G4"), rootNote);

    chromatic.setRoot(new TNote((byte) (root.getValue() + 12)));
    notes.addAll(chromatic.getNotes());

    System.out.println("next octave: " + notes.get(12));
    assertEquals(new TNote("G5"), notes.get(12));

    final TNote nextOctave = rootNote.nextOctave();
    assertEquals(new TNote("G5"), nextOctave);
    assertEquals(rootNote, nextOctave.previousOctave());
  }
}
