package com.orionletizi.sequencer;

import com.orionletizi.sequencer.theory.TIntervals;
import com.orionletizi.sequencer.theory.TNote;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TheoryTest {
  @Test
  public void test() {
    final TIntervals chromatic = new TIntervals("1 b2 2 b3 3 4 b5 5 b6 6 b7 7");

    TNote root = new TNote("G4");
    chromatic.setRoot(root);
    final List<TNote> notes = chromatic.getNotes();

    System.out.println("notes length: " + notes.size() + ", notes 0: " + notes.get(0));
    assertEquals(new TNote("G4"), notes.get(0));

    chromatic.setRoot(new TNote((byte) (root.getValue() + 12)));
    notes.addAll(chromatic.getNotes());

    System.out.println("next octave: " + notes.get(12));
    assertEquals(new TNote("G5"), notes.get(12));
  }
}
