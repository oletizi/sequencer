package com.orionletizi.sampler.sfz;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jfugue.theory.Note;

import java.util.HashSet;
import java.util.Set;

public class Group {
  private Note hikey;
  private Note lokey;
  private Set<Note> keys = new HashSet<>();
  private String groupId;
  private String loopMode;
  private Set<String> offByGroups = new HashSet<>();
  private Set<Group> offGroups = new HashSet<>();
  private Set<Region> regions = new HashSet<>();

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

  public void addKey(byte key) {
    addKey(new Note(key));
  }

  public void addKey(Note key) {
    keys.add(key);
  }

  public Set<Note> getKeys() {
    return new HashSet<>(keys);
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setLoopMode(String loopMode) {
    this.loopMode = loopMode;
  }

  public void setOffByGroups(String offByGroup) {
    this.offByGroups.add(offByGroup);
  }

  public void addRegion(Region region) {
    this.regions.add(region);
  }

  public Set<Region> getRegions() {
    return new HashSet<>(regions);
  }

  public Set<String> getOffByGroups() {
    return new HashSet<>(offByGroups);
  }

  public void addOffGroup(Group group) {
    offGroups.add(group);
  }

  public Set<Group> getOffGroups() {
    return new HashSet<>(offGroups);
  }

  public String getLoopMode() {
    return loopMode;
  }


  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("loopMode", loopMode)
        .append("keys", keys)
        .append("hikey", hikey)
        .append("lokey", lokey)
        .append("groupId", groupId)
        .append("offGroups", offGroups)
        .append("offByGroups", offByGroups)
        .append("regions", regions)
        .toString();
  }
}
