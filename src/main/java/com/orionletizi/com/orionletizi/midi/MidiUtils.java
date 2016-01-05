package com.orionletizi.com.orionletizi.midi;

import com.orionletizi.util.Assertions;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MidiUtils {
  
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
    Sequence out = null;
    if (s1 != null) {
      out = copy(s1);
      final Track[] tracks1 = out.getTracks();
      final Track[] tracks2 = s2.getTracks();
      final long tickOffset = out.getTickLength();
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

  private static Track append(long tickOffset, Track start, Track end) {
    Track out = null;
    if (start != null) {
      out = start;
      for (int i = 0; i < end.size(); i++) {
        final MidiEvent midiEvent = end.get(i);
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
}
