package com.orionletizi.sampler.sfz;

import com.orionletizi.com.orionletizi.midi.MidiContext;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.audiofile.AudioFileType;
import org.apache.commons.io.FileUtils;
import org.jfugue.theory.Note;

import javax.sound.midi.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

public class SfzResampler {
  private final Sequence sequence;
  private MidiContext midiContext;
  private SfzSamplerProgram program;
  private final int samplesBetweenNotes;

  public SfzResampler(final MidiContext midiContext, final SfzSamplerProgram program, final int samplesBetweenNotes) throws InvalidMidiDataException {
    this.midiContext = midiContext;
    this.program = program;
    this.samplesBetweenNotes = samplesBetweenNotes;

    sequence = new Sequence(Sequence.PPQ, midiContext.getTicksPerBeat());
    final Track track = sequence.createTrack();

    //final MidiContext midiContext = new MidiContext(sampleRate, ticksPerBeat, tempo);
    final Region[][] regions = program.getRegions();

    long currentTick = 0;
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

        currentTick += tickCount + this.midiContext.frameToTick(this.samplesBetweenNotes);
        previousRegion = region;
      }
    }
  }

  public Sequence getSequence() {
    return sequence;
  }

  public void createNewProgram(URL source, File dest) throws IOException {

    assert !dest.exists();

    final File sampleDir = new File(dest, "samples");
    FileUtils.forceMkdir(sampleDir);

    final BufferedWriter out = new BufferedWriter(new FileWriter(new File(dest, dest.getName() + ".sfz")));

    assert dest.isDirectory();
    assert sampleDir.isDirectory();

    final Sample sourceSample = new Sample(source.getFile());

    if (sourceSample.getNumFrames() > Integer.MAX_VALUE) {
      throw new RuntimeException("Source sample frame count is larger than integer max value. Beads has a problem.");
    }

    int currentFrame = 0;


    for (Region[] regionsForKey : program.getRegions()) {
      Region previousRegion = null;
      Group previousGroup = null;
      for (Region region : regionsForKey) {
        if (region == null || region == previousRegion) {
          continue;
        }
        final Group group = region.getGroup();
        if (group != null && group != previousGroup) {
          out.write(group.toString());
          out.newLine();
          out.newLine();
        }

        final Sample sample = region.getSample();
        int frameCount = (int) sample.getNumFrames() + samplesBetweenNotes;

        final Sample destSample = new Sample(sourceSample.samplesToMs(frameCount), sourceSample.getNumChannels(),
            sourceSample.getSampleRate());

        float[][] buffer = new float[sourceSample.getNumChannels()][];

        for (int i = 0; i < sourceSample.getNumChannels(); i++) {
          buffer[i] = new float[frameCount];
        }

        // copy the relevant frames from the source sample to the dest sample
        sourceSample.getFrames(currentFrame, buffer);
        destSample.putFrames(0, buffer);

        // write the dest sample
        final String destSampleName = new File(sample.getFileName()).getName();
        destSample.write(new File(sampleDir, destSampleName).getAbsolutePath(), AudioFileType.WAV);

        // write the region to the program file
        out.write(region.toString("samples"));
        out.newLine();
        out.newLine();

        currentFrame += frameCount;
        previousRegion = region;
        previousGroup = region.getGroup();
      }
    }
    out.close();
  }
}
