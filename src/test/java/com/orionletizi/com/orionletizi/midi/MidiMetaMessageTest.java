package com.orionletizi.com.orionletizi.midi;

import com.orionletizi.com.orionletizi.midi.message.MidiMetaMessage;
import org.junit.Test;

import javax.sound.midi.*;

public class MidiMetaMessageTest {


  @Test
  public void test() throws Exception {
    final Sequence sequence = MidiSystem.getSequence(ClassLoader.getSystemResourceAsStream("midi/drum_pattern_tempo_change.mid"));
    final Track track = sequence.getTracks()[0];
    for (int i = 0; i < track.size(); i++) {
      final MidiEvent midiEvent = track.get(i);
      final MidiMessage message = midiEvent.getMessage();
      if (message instanceof MetaMessage) {
        final MetaMessage meta = (MetaMessage) message;
        final MidiMetaMessage mm = new MidiMetaMessage(meta);
        info(mm);
        if (mm.isTrackName()) {
          info("track name: " + mm.getText());
        } else if (mm.isInstrumentName()) {
          info("instrument name: " + mm.getText());
        } else if (mm.isSetTempo()) {
          info("tempo: " + mm.getTempo());
        } else if (mm.isTimeSignature()) {
          info("time signature: " + mm.getTimeSignature());
        }
      }
    }
  }

  private void info(Object s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }
}