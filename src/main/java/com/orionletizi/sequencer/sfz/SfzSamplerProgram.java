package com.orionletizi.sequencer.sfz;

import com.orionletizi.sequencer.SamplerProgram;
import net.beadsproject.beads.data.Sample;
import org.jfugue.theory.Note;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

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
    info("getSampleFileForNote(" + i + "), regions: " + Arrays.deepToString(regions));
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

      info("lokey: " + lokey + ", hikey: " + hikey);
      for (int i = lokey; i <= hikey; i++) {
        info("placing region in " + i + ": " + currentRegion);
        regions[i] = currentRegion;
      }
    }

    // start a new Region.
    currentRegion = new Region();
  }

  @Override
  public void notifySample(String sample) {
    try {
      final File sampleFile = new File(sampleBase, sample);
      currentRegion.setSample(new Sample(sampleFile.getAbsolutePath()));
      info("Notify sample: " + sampleFile + ", currentRegion: " + currentRegion);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void info(String s) {
    System.out.println("INFO " + s);
  }

  @Override
  public void notifyLokey(Note lokey) {
    currentRegion.setLokey(lokey);
    info("Notify lokey: " + lokey + ", currentRegion: " + currentRegion);
  }

  @Override
  public void notifyHikey(Note hikey) {
    currentRegion.setHikey(hikey);
    info("Notify hikey: " + hikey + ", currentRegion: " + currentRegion);
  }

  @Override
  public void notifyPitchKeycenter(Note pitchKeycenter) {
    // TODO: handle regions that span more than one note
  }

  @Override
  public void notifyKey(byte key) {
    currentRegion.setKey(key);
    info("Notify key: " + key + ", currentRegion: " + currentRegion);
  }

  @Override
  public void notifyHivel(byte hivel) {
    currentRegion.setHivel(hivel);
  }

  @Override
  public void notifyLovel(byte lovel) {
    currentRegion.setLovel(lovel);
  }

  private class Region {
    private Note hikey;
    private Note lokey;
    private Sample sample;
    private byte key;
    private byte hivel;
    private byte lovel;

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

    @Override
    public String toString() {
      byte lokeyVal = lokey == null ? -1 : lokey.getValue();
      byte hikeyVal = hikey == null ? -1 : hikey.getValue();
      return "[Region: lokey: " + lokey + " (val: " + lokeyVal + "), hikey: " + hikey + " (val: " + hikeyVal + "), sample: " + sample + "]";
    }

    public void setKey(byte key) {
      this.key = key;
    }

    public void setHivel(byte hivel) {
      this.hivel = hivel;
    }

    public void setLovel(byte lovel) {
      this.lovel = lovel;
    }
  }
}
