package com.orionletizi.sampler.sfz;

import net.beadsproject.beads.data.Sample;
import org.jfugue.theory.Note;

import java.util.Set;

public class Region extends Group {

  private final Group group;
  private Sample sample;
  private int hivel = 127;
  private int lovel = 0;
  // default keytrack is 100 which means 100 cents (one semitone) per key. Allowed values are -1200 - 1200
  private int pitchKeytrack = 100;

  public Region(Group group) {
    super();
    this.group = group;
    this.group.addRegion(this);
  }

  @Override
  public Note getHikey() {
    return super.getHikey() == null ? group.getHikey() : super.getHikey();
  }

  @Override
  public Note getLokey() {
    return super.getLokey() == null ? group.getLokey() : super.getLokey();
  }

  @Override
  public Set<Note> getKeys() {
    return super.getKeys().isEmpty() ? group.getKeys() : super.getKeys();
  }

  public Sample getSample() {
    return sample;
  }

  public void setSample(Sample sample) {
    this.sample = sample;
  }

  public void setHivel(int hivel) {
    assert hivel >= 0 && hivel <= Byte.MAX_VALUE;
    this.hivel = hivel;
  }

  public void setLovel(int lovel) {
    assert lovel >= 0 && hivel <= Byte.MAX_VALUE;
    this.lovel = lovel;
  }

  public int getHivel() {
    return hivel;
  }

  public int getLovel() {
    return lovel;
  }

  public void setPitchKeytrack(int pitchKeytrack) {
    this.pitchKeytrack = pitchKeytrack;
  }

  public int getPitchKeytrack() {
    return pitchKeytrack;
  }

  public String toString(final String sampleRoot) {

    String samplePath = "";
    if (getSample() != null) {
      samplePath = sampleRoot != null ? sampleRoot + "/" + this.getSample().getSimpleName() : this.getSample().getSimpleName();
    }
    return "<region>\n"
        + (getLokey() != null ? "lokey=" + getLokey().getValue() + "\n" : "")
        + (getHikey() != null ? "hikey=" + getHikey().getValue() + "\n" : "")
        + "lovel=" + getLovel() + "\n"
        + "hivel=" + getHivel() + "\n"
        + "pitch_keytrack=" + getPitchKeytrack() + "\n"
        + "sample=" + samplePath;
  }

  @Override
  public String toString() {
//    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
//        .append("hikey", getHikey())
//        .append("lokey", getLokey())
//        .append("keys", getKeys())
//        .append("hivel", this.hivel)
//        .append("lovel", this.lovel)
//        .append("sample", this.sample).toString();
    return toString(null);
  }

  public Group getGroup() {
    return group;
  }

}
