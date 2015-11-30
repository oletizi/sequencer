package com.orionletizi.sampler;

import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.ugens.SamplePlayer;
import org.jfugue.theory.Note;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class Sampler implements Receiver {

  private final AudioContext ac;
  private final SamplerProgram program;
  private final Map<Sample, SamplePlayer> playerCache = new HashMap<>();
  private final Map<Byte, Set<SamplePlayer>> notePlayerCache = new HashMap<>();
  private final Map<Byte, Note> onNotes = new HashMap<>();


  public Sampler(final AudioContext ac, final SamplerProgram program) {
    this.ac = ac;
    this.program = program;
  }

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

  private void handleNoteOn(ShortMessage message) {
    final byte noteValue = message.getMessage()[1];
    final byte noteOnVelocity = message.getMessage()[2];
    final Note note = new Note(noteValue);
    note.setOnVelocity(noteOnVelocity);
    onNotes.put(noteValue, note);
    noteOn(note);
  }

  public void noteOn(Note note) {
    info("Note on: " + note + ", velocity: " + note.getOnVelocity());
    final Sample sample = program.getSampleForNote(note.getValue(), note.getOnVelocity());
    SamplePlayer player = playerCache.get(sample);
    if (player == null) {
      player = new SamplePlayer(ac, sample);
      player.setKillOnEnd(false);
      ac.out.addInput(player);
      playerCache.put(sample, player);
    }

    Set<SamplePlayer> players = this.notePlayerCache.get(note.getValue());
    if (players == null) {
      players = new HashSet<>();
      this.notePlayerCache.put(note.getValue(), players);
    }
    players.add(player);
    info("starting player: " + player + ", sample: " + player.getSample());
    info("note on: " + note + ", play sample " + sample);
    final boolean paused = player.isPaused();
    player.pause(paused);
    player.setPosition(0);
    player.start();
  }

  private void handleNoteOff(ShortMessage message) {
    final byte noteValue = message.getMessage()[1];
    final byte noteOffVelocity = message.getMessage()[2];
    final Note note = onNotes.remove(noteValue);
    if (note != null) {
      note.setOffVelocity(noteOffVelocity);
      noteOff(note);
    }
  }
  private void noteOff(Note note) {
    info("Note off: " + note);
    final Set<Byte> notesForNoteOff = program.getOffNotesForNoteOff(note.getValue(), note.getOnVelocity());
    info("notes for note off: " + notesForNoteOff);
    for (Byte key : notesForNoteOff) {
      final Set<SamplePlayer> players = notePlayerCache.get(key);
      for (SamplePlayer player : players) {
        info("Pausing player: " + player);
        player.pause(true);
        player.setToLoopStart();
      }
    }
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

  @Override
  public void close() {

  }
}
