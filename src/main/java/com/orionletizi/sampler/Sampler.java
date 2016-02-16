package com.orionletizi.sampler;

import com.orionletizi.com.orionletizi.midi.Transform;
import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
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

  private static final Logger logger = LoggerImpl.forClass(Sampler.class);

  private final AudioContext ac;
  private final SamplerProgram program;
  private final Transform transform;
  private final Map<Sample, SamplePlayer> playerCache = new HashMap<>();
  private final Map<Integer, Set<SamplePlayer>> notePlayingCache = new HashMap<>();
  private final Map<Integer, Note> onNotes = new HashMap<>();

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
        break;
      case ShortMessage.NOTE_OFF:
        handleNoteOff(message);
        break;
    }
  }

  private void handleNoteOn(ShortMessage message) {
    final int noteValue = message.getMessage()[1];
    final int noteOnVelocity = message.getMessage()[2];
    final Note note = new Note(noteValue);
    note.setOnVelocity((byte) noteOnVelocity);
    //onNotes.put(noteValue, note);
    noteOn(note);
  }

  public void noteOn(Note note) {
    int noteValue = note.getValue();
    int noteOnVelocity = note.getOnVelocity();
    onNotes.put(noteValue, note);
    note = transform.transform(note);
    info("Note on: " + note + "(" + noteValue + "), velocity:" + note.getOnVelocity());
    final Sample sample = program.getSampleForNote(noteValue, noteOnVelocity);
    info("Note on got sample for note: " + note + ", sample: " + sample);
    if (sample != null) {
      SamplePlayer player = playerCache.get(sample);
      if (player == null) {
        player = new SamplePlayer(ac, sample);
        player.setKillOnEnd(false);
        ac.out.addInput(player);
        ac.out.addDependent(player);
        playerCache.put(sample, player);
      }

      Set<SamplePlayer> players = this.notePlayingCache.get(noteValue);
      if (players == null) {
        players = new HashSet<>();
        this.notePlayingCache.put(noteValue, players);
      }
      info("Adding player to cache: " + notePlayingCache);
      players.add(player);
      info("Done adding player to cache: " + notePlayingCache);
      //info("note on: " + note + ", play sample " + sample);
      player.setPosition(0);
      info("starting player: " + player);
      player.start();
    } else {
      info("No sample for note: " + note + " (" + note.getValue() + ")");
    }

    // find all players than need to be turned off by this note on
    final Set<Integer> offNotes = program.getOffNotesForNoteOn(note.getValue());
    for (Integer offNote : offNotes) {
      final Set<SamplePlayer> offPlayers = this.notePlayingCache.get(offNote);
      if (offPlayers != null) {
        for (SamplePlayer offPlayer : offPlayers) {
          info("stopping player: " + offPlayer);
          offPlayer.pause(true);
        }
      }
    }
  }

  private void handleNoteOff(ShortMessage message) {
    final int noteValue = message.getMessage()[1];
    final int noteOffVelocity = message.getMessage()[2];

    info("=====>handle note off: getting on note for " + noteValue + "; on notes: " + onNotes);
    final Note onNote = onNotes.get(noteValue);
    info("found: " + onNote);
    if (onNote != null) {
      onNote.setOffVelocity((byte) noteOffVelocity);
      noteOff(onNotes.remove(noteValue));
    }
  }

  private void noteOff(Note note) {
    info("===> Note off: " + note + "(" + note.getValue() + ")");
    note = transform.transform(note);
    final Set<Integer> notesForNoteOff = program.getOffNotesForNoteOff(note.getValue(), note.getOnVelocity());
    info("Turning notes off for note: note: " + note + ": off notes: " + notesForNoteOff);
    info("Note playing cache: " + notePlayingCache);
    for (Integer key : notesForNoteOff) {
      final Set<SamplePlayer> players = notePlayingCache.remove(key);
      info("got players for key: " + key + ", players: " + players);
      if (players != null) {
        for (SamplePlayer player : players) {
          player.pause(true);
          player.setToLoopStart();
        }
      }
    }
  }

  private void info(String s) {
    //System.out.println(getClass().getSimpleName() + ": " + s);
    logger.info(s);
  }


  @Override
  public void close() {

  }
}
