package com.orionletizi.com.orionletizi.midi;

import com.orionletizi.util.Assertions;
import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
import org.jfugue.theory.Note;

import javax.sound.midi.*;
import java.util.*;

public class MidiUtils {

  private static final Logger logger = LoggerImpl.forClass(MidiUtils.class);

  public static Sequence merge(Sequence s1, Sequence s2) throws InvalidMidiDataException {
    Assertions.assertTrue(s1.getResolution() == s2.getResolution());
    Assertions.assertTrue(s1.getDivisionType() == s2.getDivisionType());
    final List<Track> allTracks = new ArrayList<>();
    allTracks.addAll(Arrays.asList(s1.getTracks()));
    allTracks.addAll(Arrays.asList(s2.getTracks()));
    return merge(s1.getDivisionType(), s1.getResolution(), allTracks);
  }

  public static Sequence merge(final float divisionType, final int resolution, final List<Track> tracks) throws InvalidMidiDataException {
    final Sequence out = new Sequence(divisionType, resolution);
    for (Track track : tracks) {
      Track newTrack = out.createTrack();
      for (int i = 0; i < track.size(); i++) {
        newTrack.add(track.get(i));
      }
    }
    return out;
  }

  public static Sequence merge(List<Sequence> sequences) throws InvalidMidiDataException {
    // XXX: I'm sure there's a more efficient way to do this
    Sequence out = null;
    if (sequences != null && !sequences.isEmpty()) {
      for (Sequence sequence : sequences) {
        if (out == null) {
          out = new Sequence(sequence.getDivisionType(), sequence.getResolution());
        }
        out = merge(out, sequence);
      }
    }
    return out;
  }

  public static Sequence append(final Sequence s1, final Sequence s2) throws InvalidMidiDataException {
    return append(new NullQuantizationContext(), s1, s2);
  }

  public static Sequence append(final QuantizationContext ctxt, final Sequence s1, final Sequence s2) throws InvalidMidiDataException {
    Sequence out = null;
    if (s1 != null) {
      out = copy(s1);
      final Track[] tracks1 = out.getTracks();
      final Track[] tracks2 = s2.getTracks();
      final long tickOffset = ctxt.getNearestTick(out.getTickLength());
      for (int i = 0; i < tracks1.length && i < tracks2.length; i++) {
        Track start = tracks1[i];
        Track end = tracks2[i];
        append(tickOffset, start, end);
      }
      if (tracks1.length < tracks2.length) {
        for (int i = tracks1.length; i < tracks2.length; i++) {
          Track start = out.createTrack();
          Track end = tracks2[i];
          append(tickOffset, start, end);
        }
      }
    }
    return out;
  }

  private static Track append(long tickOffset, Track firstTrack, Track appendedTrack) {
    Track out = null;
    if (firstTrack != null) {
      out = firstTrack;
      for (int i = 0; i < appendedTrack.size(); i++) {
        final MidiEvent midiEvent = appendedTrack.get(i);
        final MidiMessage newMessage = (MidiMessage) midiEvent.getMessage().clone();
        out.add(new MidiEvent(newMessage, midiEvent.getTick() + tickOffset));
      }
    }
    return out;
  }

  public static Sequence copy(final Sequence in) throws InvalidMidiDataException {
    Sequence out = null;
    if (in != null) {
      out = new Sequence(in.getDivisionType(), in.getResolution());
      for (Track track : in.getTracks()) {
        final Track newTrack = out.createTrack();
        for (int i = 0; i < track.size(); i++) {
          newTrack.add(track.get(i));
        }
      }
    }
    return out;
  }

  public static long getTickLength(final Sequence sequence) {
    long maxTick = 0;
    MidiEvent maxMidiEvent = null;
    final List<MidiEvent> onNotes = new ArrayList<>();
    final List<MidiEvent> offNotes = new ArrayList<>();
    final Map<Integer, MidiEvent> onNotesByKey = new HashMap<>();
    for (Track track : sequence.getTracks()) {
      for (int i = 0; i < track.size(); i++) {
        final MidiEvent midiEvent = track.get(i);
        final MidiMessage message = midiEvent.getMessage();
        if (message instanceof ShortMessage) {
          final ShortMessage shortMessage = (ShortMessage) message;
          final int key = shortMessage.getData1();
          switch (shortMessage.getCommand()) {
            case ShortMessage.NOTE_ON:
              onNotes.add(midiEvent);
              onNotesByKey.put(key, midiEvent);
              break;
            case ShortMessage.NOTE_OFF:
              offNotes.add(midiEvent);
              onNotesByKey.remove(key);
              break;
          }
          maxTick = Math.max(maxTick, midiEvent.getTick());
          if (maxTick == midiEvent.getTick()) {
            maxMidiEvent = midiEvent;
          }
        }
      }
    }
    info("    maxMidiEvent: " + toString(maxMidiEvent));
    info("    on notes    : " + onNotes.size());
    info("    off notes   : " + offNotes.size());
    info("    onNotesByKey: " + onNotesByKey);
    return maxTick;
  }

  public static String toString(final MidiEvent event) {
    return "MidiEvent{ tick: " + event.getTick() + ", " + toString(event.getMessage()) + "}";
  }

  public static String toString(final MidiMessage message) {
    final StringBuilder buf = new StringBuilder("MidiMessage{");
    buf.append("type: " + message.getClass().getName());
    if (message instanceof ShortMessage) {
      final ShortMessage shortMessage = (ShortMessage) message;
      switch (shortMessage.getCommand()) {
        case ShortMessage.NOTE_ON:
          final Note noteOn = new Note(shortMessage.getData1());
          buf.append(", NOTE_ON").append(", key: " + noteOn.getValue());
          buf.append(", note: " + noteOn);
          break;
        case ShortMessage.NOTE_OFF:
          final Note noteOff = new Note(shortMessage.getData1());
          buf.append(", NOTE_OFF").append(", key: " + noteOff.getValue());
          buf.append(", note: " + noteOff);
          break;
        // TODO: There are many more cases to handle, but we'll get to that later--if ever.
      }
    }
    buf.append("}");
    return buf.toString();
  }

  private static void info(String s) {
    logger.info(s);
  }
}
