package com.orionletizi.sequencer.sfz;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class SfzParserTest {

  private URL sfzResource;

  @Before
  public void before() throws Exception {
    sfzResource = ClassLoader.getSystemResource("sfz/mellotron/mk2flute.sfz");
    assertNotNull(sfzResource);
  }

  @Test
  public void testParse() throws Exception {
    final SfzParser parser = new SfzParser();
    final SfzParserObserver observer = mock(SfzParserObserver.class);
    parser.addObserver(observer);
    parser.parse(sfzResource);

    verify(observer, times(1)).notifyGroup();
    verify(observer, times(35)).notifyRegion();
    verify(observer, times(1)).notifySample("G2.wav");
    verify(observer, times(1)).notifyLokey("g2");
    verify(observer, times(1)).notifyHikey("g2");
    verify(observer, times(1)).notifyPitchKeycenter("g2");
  }

}