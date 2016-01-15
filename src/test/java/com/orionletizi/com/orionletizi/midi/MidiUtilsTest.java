package com.orionletizi.com.orionletizi.midi;

import com.orionletizi.music.theory.Tempo;
import com.orionletizi.music.theory.TimeSignature;
import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
import org.junit.Before;
import org.junit.Test;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;

import static com.orionletizi.com.orionletizi.midi.MidiUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MidiUtilsTest {
  private static final Logger logger = LoggerImpl.forClass(MidiUtilsTest.class);

  private Sequence drumSequence;
  private Sequence copy;
  private Sequence gt1Sequence;
  private Sequence gt2Sequence;
  private Sequence gt3Sequence;

  @Before
  public void before() throws Exception {
    drumSequence = MidiSystem.getSequence(ClassLoader.getSystemResource("midi/drum_pattern.mid"));
    gt1Sequence = MidiSystem.getSequence(ClassLoader.getSystemResource("midi/GT1.mid"));
    gt2Sequence = MidiSystem.getSequence(ClassLoader.getSystemResource("midi/GT2.mid"));
    gt3Sequence = MidiSystem.getSequence(ClassLoader.getSystemResource("midi/GT3.mid"));
    copy = copy(drumSequence);
  }

  @Test
  public void testCopy() throws Exception {
    assertFalse(drumSequence == copy);
    assertEquals(drumSequence.getDivisionType(), copy.getDivisionType(), 0);
    assertEquals(drumSequence.getDivisionType(), copy.getDivisionType(), 0);
    assertEquals(drumSequence.getTickLength(), copy.getTickLength());
    assertEquals(drumSequence.getTracks().length, copy.getTracks().length);
  }

  @Test
  public void testMerge() throws Exception {
    final Sequence merged = merge(drumSequence, copy);
    assertEquals(drumSequence.getTickLength(), merged.getTickLength());
    assertEquals(drumSequence.getTracks().length * 2, merged.getTracks().length);
  }

  @Test
  public void testAppend() throws Exception {
    final Sequence appended = append(drumSequence, drumSequence);
    assertEquals(drumSequence.getTickLength() * 2, appended.getTickLength());
  }

  @Test
  public void testGetTickLength() throws Exception {
    LoggerImpl.turnOff(BasicQuantizationContext.class);
    final MidiContext midiContext = new MidiContext(1, drumSequence.getResolution(), Tempo.newTempoFromBPM(120), new TimeSignature(4, 4));
    final QuantizationContext quantizer = new BasicQuantizationContext(midiContext, 4);
    info("drumSequence reported length:  " + drumSequence.getTickLength());
    info("drumSequence inspected length: " + MidiUtils.getTickLength(drumSequence));
    info("drumSequence reported beats:   " + midiContext.ticksToBeats(drumSequence.getTickLength()));
    info("drumSequence quantized length: " + quantizer.getNearestTick(drumSequence.getTickLength()));
    info("drumSequence quantized beats:  " + midiContext.ticksToBeats(quantizer.getNearestTick(drumSequence.getTickLength())));
    info("drumSequence quantized bars:   " + midiContext.ticksToBars(quantizer.getNearestTick(drumSequence.getTickLength())));
    info("");
    info("gt1Sequence reported length:   " + gt1Sequence.getTickLength());
    info("gt1Seqeunce inspected length:  " + MidiUtils.getTickLength(gt1Sequence));
    info("gt1Sequence quantized length:  " + quantizer.getNearestTick(gt1Sequence.getTickLength()));
    info("gt1Sequence quantized beats:   " + midiContext.ticksToBeats(quantizer.getNearestTick(gt1Sequence.getTickLength())));
    info("gt1Sequence quantized bars:    " + midiContext.ticksToBars(quantizer.getNearestTick(gt1Sequence.getTickLength())));
    info("");
    info("gt2Sequence reported length:   " + gt2Sequence.getTickLength());
    info("gt2Sequence inspected length:  " + MidiUtils.getTickLength(gt2Sequence));
    info("gt2Sequence quantized length:  " + quantizer.getNearestTick(gt2Sequence.getTickLength()));
    info("gt2Sequence quantized beats:   " + midiContext.ticksToBeats(quantizer.getNearestTick(gt2Sequence.getTickLength())));
    info("gt2Sequence quantized bars:    " + midiContext.ticksToBars(quantizer.getNearestTick(gt2Sequence.getTickLength())));
    info("");
    info("gt3Sequence reported length:   " + gt3Sequence.getTickLength());
    info("gt3Sequence inspected length:  " + MidiUtils.getTickLength(gt3Sequence));
    info("gt3Sequence quantized length:  " + quantizer.getNearestTick(gt3Sequence.getTickLength()));
    info("gt3Sequence quantized beats:   " + midiContext.ticksToBeats(quantizer.getNearestTick(gt3Sequence.getTickLength())));
    info("gt3Sequence quantized bars:    " + midiContext.ticksToBars(quantizer.getNearestTick(gt3Sequence.getTickLength())));
  }

  private void info(String s) {
    logger.info(s);
  }
}