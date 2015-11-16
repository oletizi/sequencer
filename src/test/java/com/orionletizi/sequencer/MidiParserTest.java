package com.orionletizi.sequencer;

import com.leff.midi.MidiFile;
import com.leff.midi.event.NoteOn;
import com.leff.midi.util.MidiEventListener;
import com.leff.midi.util.MidiProcessor;
import com.orionletizi.sequencer.midi.MidiParser;
import com.orionletizi.sequencer.midi.MidiParserObserver;
import com.orionletizi.sequencer.theory.TNote;
import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
import com.sun.media.sound.StandardMidiFileReader;
import org.junit.Before;
import org.junit.Test;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import java.io.File;
import java.net.URL;

import static org.mockito.Mockito.*;

public class MidiParserTest {

  private static final Logger logger = LoggerImpl.forClass(MidiParserTest.class);
  private Sequence sequence;

  @Before
  public void before() throws Exception {
    final URL midiFile = ClassLoader.getSystemResource("midi/wurli2.mid");
    sequence = new StandardMidiFileReader().getSequence(midiFile);
  }

  @Test
  public void test() {
    final MidiParserObserver observer = mock(MidiParserObserver.class);
    MidiParser parser = new MidiParser(observer);
    parser.parse(sequence);

    verify(observer, times(1)).notifyResolution(480);
    verify(observer, times(1)).notifyDivisionTypePPQ();
    verify(observer, times(1)).notifyTickLength(7200);
    //verify(observer, )
  }

  @Test
  public void testAndroidRawSequence() throws Exception {
    final MidiFile midiFile = new MidiFile(new File(ClassLoader.getSystemResource("midi/wurli2.mid").getFile()));
    final MidiProcessor processor = new MidiProcessor(midiFile);
    processor.registerEventListener(new MidiEventListener() {
      @Override
      public void onStart(boolean b) {
        logger.info("On start");
      }

      @Override
      public void onEvent(com.leff.midi.event.MidiEvent midiEvent, long l) {
        final NoteOn noteOn = (NoteOn) midiEvent;
        logger.info("Note on: " + new TNote((byte) noteOn.getNoteValue()));
      }

      @Override
      public void onStop(boolean b) {
        logger.info("On stop");
      }
    }, com.leff.midi.event.NoteOn.class);

    processor.start();
    synchronized (this) {
      wait(10 * 1000);
    }
  }

  @Test
  public void testJavaSoundRawSequence() {
    final Track track = sequence.getTracks()[0];
    logger.info("resolution: " + sequence.getResolution());
    logger.info("tick length: " + sequence.getTickLength());
    logger.info("division type: " + sequence.getDivisionType());
    logger.info("microsecond length: " + sequence.getMicrosecondLength());
    for (int i = 0; i < track.size(); i++) {
      final MidiEvent midiEvent = track.get(i);
      final MidiMessage message = midiEvent.getMessage();
      final byte[] data = message.getMessage();
      final int status = message.getStatus();
      final String statusString = Integer.toBinaryString(status);
      final int messageType = status >> 4;
      final int channel = status & 00001111;
      final String channelString = Integer.toBinaryString(channel);

      final String messageTypeString = Integer.toBinaryString(messageType);
      final TNote note = new TNote(data[1]);
      logger.info("tick: " + midiEvent.getTick() + ", status: " + statusString + ", message type: " + messageTypeString
          + ", channel: " + channelString + ", data size: " + data.length
          + ", byte 2 (data byte 1 (pitch)): " + Integer.toBinaryString(data[1]) + " (" + data[1] + ") (" + note + ")"
          + ", byte 3 (data byte 2): " + Integer.toBinaryString(data[2]));
    }
  }
}
