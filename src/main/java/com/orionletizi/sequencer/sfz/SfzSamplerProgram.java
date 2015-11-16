package com.orionletizi.sequencer.sfz;

import com.orionletizi.sequencer.SamplerProgram;
import net.beadsproject.beads.data.Sample;
import org.jfugue.theory.Note;

import java.io.File;
import java.io.IOException;

public class SfzSamplerProgram implements SamplerProgram, SfzParserObserver {

  private final Region[] regions = new Region[128];
  private final File sampleBase;
  private Region currentRegion;

  public SfzSamplerProgram(final File sampleBase) {
    this.sampleBase = sampleBase;
    final Region nullRegion = new Region();
    for (int i = 0; i < regions.length; i++) {
      regions[i] = nullRegion;
    }
  }

  @Override
  public File getSampleFileForNoteName(String noteString) {
    final Note note = new Note(noteString);
    return getSampleFileForNote(note.getValue());
  }

  @Override
  public File getSampleFileForNote(byte i) {
    final Region region = regions[i];
    final Sample sample = region.getSample();
    return sample == null ? null : new File(sample.getFileName());
  }

  @Override
  public Sample getSampleForNote(byte i) {
    return regions[i].getSample();
  }

  @Override
  public void notifyGroup() {
    // TODO: Support multiple groups.
  }

  @Override
  public void notifyRegion() {
    // we're done with the previous region. Plop it in the regions array across it's range.
    if (currentRegion != null) {
      int lokey = 127;
      int hikey = 0;
      if (currentRegion.getLokey() != null) {
        lokey = currentRegion.getLokey().getValue();
      }
      if (currentRegion.getHikey() != null) {
        hikey = currentRegion.getHikey().getValue();
      }

      for (int i = lokey; i <= hikey; i++) {
        regions[i] = currentRegion;
      }
    }

    // start a new Region.
    currentRegion = new Region();
  }

  @Override
  public void notifySample(String sample) {
    try {
      currentRegion.setSample(new Sample(new File(sampleBase, sample).getAbsolutePath()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void notifyLokey(String lokey) {
    currentRegion.setLokey(new Note(lokey));
  }

  @Override
  public void notifyHikey(String hikey) {
    currentRegion.setHikey(new Note(hikey));
  }

  @Override
  public void notifyPitchKeycenter(String pitchKeycenter) {
    // TODO: handle regions that span more than one note
  }

  private class Region {
    private Note hikey;
    private Note lokey;
    private Sample sample;

    public Note getHikey() {
      return hikey;
    }

    public void setHikey(Note hikey) {
      this.hikey = hikey;
    }

    public Note getLokey() {
      return lokey;
    }

    public void setLokey(Note lokey) {
      this.lokey = lokey;
    }

    public Sample getSample() {
      return sample;
    }

    public void setSample(Sample sample) {
      this.sample = sample;
    }
  }
}
