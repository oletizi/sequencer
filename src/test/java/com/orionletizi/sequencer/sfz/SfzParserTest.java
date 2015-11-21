package com.orionletizi.sequencer.sfz;

import org.jfugue.theory.Note;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class SfzParserTest {

  private URL sfzResource;
  private SfzParser parser;
  private SfzParserObserver observer;

  @Before
  public void before() throws Exception {
    sfzResource = ClassLoader.getSystemResource("sfz/mellotron/mk2flute.sfz");
    assertNotNull(sfzResource);
    parser = new SfzParser();
    observer = mock(SfzParserObserver.class);
    parser.addObserver(observer);
  }

  @Test
  public void testWithKey() throws Exception {
    sfzResource = ClassLoader.getSystemResource("sfz/ibanezbass/ibanez-bass.sfz");
    assertNotNull(sfzResource);
    parser.parse(sfzResource);

    verify(observer, times(100)).notifyRegion();
    verify(observer, times(4)).notifyKey((byte) 52);
    verify(observer, times(25)).notifyHivel((byte) 127);
    verify(observer, times(25)).notifyLovel((byte) 111);
  }

  @Test
  public void testParse() throws Exception {
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
    sfzResource = ClassLoader.getSystemResource("sfz/prospector/prospector.sfz");
    assertNotNull(sfzResource);

    parser.parse(sfzResource);
    verify(observer, times(78)).notifyRegion();
    verify(observer, times(1)).notifySample("Acoustic_Guitar-A-section1.wav");
    verify(observer, times(1)).notifyLokey(new Note("24"));
    verify(observer, times(1)).notifyHikey(new Note("24"));
    verify(observer, times(1)).notifyPitchKeycenter(new Note("24"));
  }
}