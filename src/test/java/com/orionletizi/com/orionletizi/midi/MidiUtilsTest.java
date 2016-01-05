package com.orionletizi.com.orionletizi.midi;

import org.junit.Before;
import org.junit.Test;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import static com.orionletizi.com.orionletizi.midi.MidiUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MidiUtilsTest {

  private Sequence sequence;
  private Sequence copy;

  @Before
  public void before() throws Exception {
    sequence = MidiSystem.getSequence(ClassLoader.getSystemResource("midi/drum_pattern.mid"));
    copy = copy(sequence);
  }

  @Test
  public void testCopy() throws Exception {
    assertFalse(sequence == copy);
    assertEquals(sequence.getDivisionType(), copy.getDivisionType(), 0);
    assertEquals(sequence.getDivisionType(), copy.getDivisionType(), 0);
    assertEquals(sequence.getTickLength(), copy.getTickLength());
    assertEquals(sequence.getTracks().length, copy.getTracks().length);
  }

  @Test
  public void testMerge() throws Exception {
    final Sequence merged = merge(sequence, copy);
    assertEquals(sequence.getTickLength(), merged.getTickLength());
    assertEquals(sequence.getTracks().length * 2, merged.getTracks().length);
  }

  @Test
  public void testAppend() throws Exception {
    final Sequence appended = append(sequence, sequence);
    assertEquals(sequence.getTickLength() * 2, appended.getTickLength());
  }
}