package com.orionletizi.sampler.sfz;

import org.jfugue.theory.Note;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class SfzParserTest {

  private URL sfzResource;
  private SfzParser parser;
  private SfzParserObserver observer;

  private void setup(String resource) throws Exception {
    sfzResource = ClassLoader.getSystemResource(resource);
    assertNotNull(sfzResource);
    parser = new SfzParser();
    observer = mock(SfzParserObserver.class);
    parser.addObserver(observer);
  }

  @Test
  public void testGuitar() throws Exception {
    setup("sfz/guitar/guitar-fixed.sfz");
    parser.parse(sfzResource);
    verify(observer, times(1)).notifyGroup();
    verify(observer, times(1)).notifySample("samples/A_quarter_notes.02_02.wav");
    verify(observer, times(60)).notifyRegion();
    verify(observer, times(60)).notifyPitchKeytrack(0);
  }

  @Test
  public void testDrums() throws Exception {
    setup("program/drums/program.sfz");
    parser.parse(sfzResource);
    // make sure the hh group was parsed three times
    verify(observer, times(3)).notifyGroupId("0");
    verify(observer, times(1)).notifyLoopMode("one_shot");
    verify(observer, times(3)).notifyOffBy("0");
  }

  @Test
  public void testWithKey() throws Exception {
    setup("sfz/ibanezbass/ibanez-bass.sfz");
    parser.parse(sfzResource);

    verify(observer, times(100)).notifyRegion();
    verify(observer, times(4)).notifyKey((byte) 52);
    verify(observer, times(25)).notifyHivel((byte) 127);
    verify(observer, times(25)).notifyLovel((byte) 111);
  }

  @Test
  public void testParse() throws Exception {
    setup("sfz/mellotron/mk2flute.sfz");
    parser.parse(sfzResource);

    verify(observer, times(1)).notifyGroup();
    verify(observer, times(35)).notifyRegion();
    verify(observer, times(1)).notifySample("G2.wav");
    verify(observer, times(1)).notifyLokey(new Note("g2"));
    verify(observer, times(1)).notifyHikey(new Note("g2"));
    verify(observer, times(1)).notifyPitchKeycenter(new Note("g2"));
  }

  @Test
  public void testParseWithoutLinebreaks() throws Exception {
    setup("sfz/prospector/prospector.sfz");
    assertNotNull(sfzResource);

    parser.parse(sfzResource);
    verify(observer, times(78)).notifyRegion();
    verify(observer, times(1)).notifySample("Acoustic_Guitar-A-section1.wav");
    verify(observer, times(1)).notifyLokey(new Note("24"));
    verify(observer, times(1)).notifyHikey(new Note("24"));
    verify(observer, times(1)).notifyPitchKeycenter(new Note("24"));
  }
}