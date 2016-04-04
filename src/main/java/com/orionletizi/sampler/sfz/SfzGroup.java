package com.orionletizi.sampler.sfz;

import com.orionletizi.sampler.Group;
import com.orionletizi.sampler.Region;
import org.jfugue.theory.Note;

import java.util.HashSet;
import java.util.Set;

public class SfzGroup implements com.orionletizi.sampler.Group {
  private Note hikey;
  private Note lokey;
  private Set<Note> keys = new HashSet<>();
  private String groupId;
  private String loopMode;
  private Set<String> offByGroups = new HashSet<>();
  private Set<Group> offGroups = new HashSet<>();
  private Set<Region> regions = new HashSet<>();

  @Override
  public Note getHikey() {
    return hikey;
  }

  @Override
  public void setHikey(Note hikey) {
    this.hikey = hikey;
  }

  @Override
  public Note getLokey() {
    return lokey;
  }

  @Override
  public void setLokey(Note lokey) {
    this.lokey = lokey;
  }

  @Override
  public void addKey(int key) {
    addKey(new Note(key));
  }

  @Override
  public void addKey(Note key) {
    keys.add(key);
  }

  @Override
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
//    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
//        .append("loopMode", loopMode)
//        .append("keys", keys)
//        .append("hikey", hikey)
//        .append("lokey", lokey)
//        .append("groupId", groupId)
//        .append("offGroups", offGroups)
//        .append("offByGroups", offByGroups)
//        .append("regions", regions)
//        .toString();
    final StringBuilder offBy = new StringBuilder();
    for (String offByGroup : offByGroups) {
      offBy.append("off_by=" + offByGroup);
    }

    return "<group>\n"
        + ((groupId != null) ? "id=" + groupId + "\n" : "")
        + ((getLoopMode() != null) ? "loop_mode=" + getLoopMode() + "\n" : "")
        + (offByGroups.isEmpty() ? "" : offBy + "\n")
        + (getLokey() != null ? "lokey=" + getLokey() + "\n" : "")
        + (getHikey() != null ? "hikey=" + getHikey() + "\n" : "");
  }
}
