package com.orionletizi.sampler.sfz;

import com.orionletizi.com.orionletizi.midi.MidiContext;
import net.beadsproject.beads.data.Sample;
import org.jfugue.theory.Note;

import javax.sound.midi.*;
import java.util.Set;

public class SfzResampler {
  private final Sequence sequence;
  private final long ticksBetweenNotes;
  private long currentTick;

  public SfzResampler(final MidiContext midiContext, final SfzSamplerProgram program, final long ticksBetweenNotes) throws InvalidMidiDataException {
    this.ticksBetweenNotes = ticksBetweenNotes;

    sequence = new Sequence(Sequence.PPQ, midiContext.getTicksPerBeat());
    final Track track = sequence.createTrack();

    //final MidiContext midiContext = new MidiContext(sampleRate, ticksPerBeat, tempo);
    final Region[][] regions = program.getRegions();

    for (int i = 0; i < regions.length; i++) {
      final Region[] regionsForKey = regions[i];
      Region previousRegion = null;
      for (Region region : regionsForKey) {
        if (region == null || region == previousRegion) {
          continue;
        }
        final Sample sample = region.getSample();
        final long frameCount = sample.getNumFrames();
        final long tickCount = midiContext.frameToTick(frameCount);
        final int channel = 0;
        final int velocity = region.getHivel();
        final Set<Note> keys = region.getKeys();
        assert !keys.isEmpty();

        // add note on event
        ShortMessage message = new ShortMessage();
        final byte key = keys.iterator().next().getValue();
        message.setMessage(ShortMessage.NOTE_ON, channel, key, velocity);

        MidiEvent event = new MidiEvent(message, currentTick);
        track.add(event);

        // add note off event
        message = new ShortMessage();
        message.setMessage(ShortMessage.NOTE_OFF, channel, key, 0);
        event = new MidiEvent(message, currentTick + tickCount);
        track.add(event);

        currentTick += tickCount + ticksBetweenNotes;
        previousRegion = region;
      }
    }
  }

  public Sequence getSequence() {
    return sequence;
  }
}
