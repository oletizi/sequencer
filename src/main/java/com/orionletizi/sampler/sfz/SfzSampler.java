package com.orionletizi.sampler.sfz;

import org.jfugue.theory.Note;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;


public class SfzSampler implements Receiver {

  @Override
  public void send(MidiMessage message, long timeStamp) {
    if (message instanceof ShortMessage) {
      handleShortMessage((ShortMessage) message);
    }
  }

  private void handleShortMessage(ShortMessage message) {
    switch (message.getCommand()) {
      case ShortMessage.NOTE_ON:
        handleNoteOn(message);
      case ShortMessage.NOTE_OFF:
        handleNoteOff(message);
    }
  }

  private void handleNoteOff(ShortMessage message) {
    final byte noteValue = message.getMessage()[1];
    noteOff(new Note(noteValue));
  }

  private void noteOff(Note note) {
    info("Note off: " + note);
  }

  private void handleNoteOn(ShortMessage message) {
    final byte noteValue = message.getMessage()[1];
    final byte noteOnVelocity = message.getMessage()[2];
    final Note note = new Note(noteValue);
    note.setOnVelocity(noteOnVelocity);
    noteOn(note);
  }

  public void noteOn(Note note) {
    info("Note on: " + note + ", velocity: " + note.getOnVelocity());
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

  @Override
  public void close() {

  }
}
