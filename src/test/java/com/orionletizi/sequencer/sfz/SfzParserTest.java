package com.orionletizi.sequencer.sfz;

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
  public void testParse() throws Exception {
    parser.parse(sfzResource);

    verify(observer, times(1)).notifyGroup();
    verify(observer, times(35)).notifyRegion();
    verify(observer, times(1)).notifySample("G2.wav");
    verify(observer, times(1)).notifyLokey("g2");
    verify(observer, times(1)).notifyHikey("g2");
    verify(observer, times(1)).notifyPitchKeycenter("g2");
  }

  @Test
  public void testParseWithoutLinebreaks() throws Exception {
    sfzResource = ClassLoader.getSystemResource("sfz/america2/america2.sfz");
    assertNotNull(sfzResource);

    parser.parse(sfzResource);
    verify(observer, times(76)).notifyRegion();
    verify(observer, times(1)).notifySample("AcousticGuitar-A-section1.wav");
    verify(observer, times(1)).notifyLokey("24");
    verify(observer, times(1)).notifyHikey("24");
    verify(observer, times(1)).notifyPitchKeycenter("24");
  }
}