package com.orionletizi.sampler.sfz;

import net.beadsproject.beads.data.Sample;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jfugue.theory.Note;

import java.util.Set;

public class Region extends Group {

  private final Group group;
  private Sample sample;
  private byte hivel = 127;
  private byte lovel = 0;

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

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("hikey", getHikey())
        .append("lokey", getLokey())
        .append("keys", getKeys())
        .append("hivel", this.hivel)
        .append("lovel", this.lovel)
        .append("sample", this.sample).toString();
  }
}