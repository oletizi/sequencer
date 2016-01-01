package com.orionletizi.sampler;

import com.orionletizi.com.orionletizi.midi.Transform;
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
  private final Transform transform;
  private final Map<Sample, SamplePlayer> playerCache = new HashMap<>();
  private final Map<Byte, Set<SamplePlayer>> notePlayerCache = new HashMap<>();
  private final Map<Byte, Note> onNotes = new HashMap<>();


  public Sampler(final AudioContext ac, final SamplerProgram program) {
    this(ac, program, note -> note);
  }

  public Sampler(final AudioContext ac, final SamplerProgram program, final Transform transform) {
    this.ac = ac;
    this.program = program;
    this.transform = transform;
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
    note = transform.transform(note);
    info("Note on: " + note + ", velocity: " + note.getOnVelocity());
    final Sample sample = program.getSampleForNote(note.getValue(), note.getOnVelocity());
    if (sample != null) {
      SamplePlayer player = playerCache.get(sample);
      if (player == null) {
        player = new SamplePlayer(ac, sample);
        player.setKillOnEnd(false);
        ac.out.addInput(player);
        ac.out.addDependent(player);
        playerCache.put(sample, player);
      }

      Set<SamplePlayer> players = this.notePlayerCache.get(note.getValue());
      if (players == null) {
        players = new HashSet<>();
        this.notePlayerCache.put(note.getValue(), players);
      }
      players.add(player);
      //info("note on: " + note + ", play sample " + sample);
      final boolean paused = player.isPaused();
      player.pause(paused);
      player.setPosition(0);
      player.start();
    } else {
      info("No sample for note: " + note + " (" + note.getValue() + ")");
    }

    // find all players than need to be turned off by this note on
    final Set<Integer> offNotes = program.getOffNotesForNoteOn(note.getValue());
    for (Integer offNote : offNotes) {
      final Set<SamplePlayer> offPlayers = this.notePlayerCache.get(offNote);
      if (offPlayers != null) {
        for (SamplePlayer offPlayer : offPlayers) {
          offPlayer.pause(true);
        }
      }
    }
  }

  private void handleNoteOff(ShortMessage message) {
    final byte noteValue = message.getMessage()[1];
    final byte noteOffVelocity = message.getMessage()[2];


    final Note onNote = onNotes.get(noteValue);
    onNote.setOffVelocity(noteOffVelocity);
    final Set<Integer> offNotes = program.getOffNotesForNoteOff(onNote.getValue(), onNote.getOnVelocity());
    info("OFF NOTES: " + offNotes);
    for (Integer offNote : offNotes) {
      onNotes.remove(offNote);
      noteOff(new Note(offNote));
    }
  }

  private void noteOff(Note note) {
    info("Note off: " + note);
    note = transform.transform(note);
    final Set<Integer> notesForNoteOff = program.getOffNotesForNoteOff(note.getValue(), note.getOnVelocity());
    for (Integer key : notesForNoteOff) {
      final Set<SamplePlayer> players = notePlayerCache.get(key);
      for (SamplePlayer player : players) {
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
