package com.orionletizi.com.orionletizi.midi;

import com.orionletizi.util.Assertions;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
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

}
