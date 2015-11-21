package com.orionletizi.sequencer.sfz;

import com.orionletizi.sequencer.SamplerProgram;
import net.beadsproject.beads.data.Sample;
import org.jfugue.theory.Note;

import java.io.File;
import java.io.IOException;

public class SfzSamplerProgram implements SamplerProgram, SfzParserObserver {

  private final Region[][] regions = new Region[128][128];
  private final File sampleBase;
  private Region currentRegion;

  public SfzSamplerProgram(final File sampleBase) {
    this.sampleBase = sampleBase;
    final Region nullRegion = new Region();
    for (int i = 0; i < regions.length; i++) {
      for (int j = 0; j > regions[i].length; j++) {
        regions[i][j] = nullRegion;
      }
    }
  }

  @Override
  public File getSampleFileForNoteName(String noteString, byte velocity) {
    final Note note = new Note(noteString);
    return getSampleFileForNote(note.getValue(), velocity);
  }

  @Override
  public File getSampleFileForNote(byte i, byte velocity) {
    info("getSampleFileForNote(note: " + i + ", velocity: " + velocity + ")");
    final Sample sample = getSampleForNote(i, velocity);
    return sample == null ? null : new File(sample.getFileName());
  }

  @Override
  public Sample getSampleForNote(byte i, byte velocity) {
    info("getSampleForNote: note: " + i + ", velocity: " + velocity);
    final Region region = regions[i][velocity];
    return region == null ? null : region.getSample();
  }

  @Override
  public void notifyGroup() {
    // TODO: Support multiple groups.
  }

  @Override
  public void notifyRegion() {
    // we're done with the previous region. Plop it in the regions array across it's range.
    if (currentRegion != null) {
      byte lokey = 127;
      byte hikey = 0;
      Note key = currentRegion.getKey();
      byte hivel = currentRegion.getHivel();
      byte lovel = currentRegion.getLovel();
      if (currentRegion.getLokey() != null) {
        lokey = currentRegion.getLokey().getValue();
      }
      if (currentRegion.getHikey() != null) {
        hikey = currentRegion.getHikey().getValue();
      }

      info("key: " + key + ", lokey: " + lokey + ", hikey: " + hikey + ", lovel: " + lovel + ", hivel: " + hivel);
      if (key != null) {
        final byte value = key.getValue();
        //while (velocity <= hivel) {
        for (int velocity = lovel; velocity <= hivel; velocity++) {
          regions[value][velocity] = currentRegion;
        }
      } else {
        for (int i = lokey; i <= hikey; i++) {
          for (int velocity = lovel; velocity <= hivel; velocity++) {
            regions[i][velocity] = currentRegion;
          }
        }
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
    private Note key;

    private Sample sample;
    private byte hivel = 127;
    private byte lovel = 0;

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
      this.key = new Note(key);
    }

    public void setHivel(byte hivel) {
      this.hivel = hivel;
    }

    public void setLovel(byte lovel) {
      this.lovel = lovel;
    }

    public byte getHivel() {
      return hivel;
    }

    public byte getLovel() {
      return lovel;
    }

    public Note getKey() {
      return key;
    }
  }
}
