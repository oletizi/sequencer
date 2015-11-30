package com.orionletizi.sampler.sfz;

import com.orionletizi.sampler.SamplerProgram;
import net.beadsproject.beads.data.Sample;
import org.jfugue.theory.Note;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SfzSamplerProgram implements SamplerProgram, SfzParserObserver {


  private enum Scope {
    global,
    group,
    region
  }

  private final File sampleBase;
  private final Region[][] regions = new Region[128][128];
  private final Set<Group> allGroups = new HashSet<>();
  private final Map<Byte, Group> groupByNote = new HashMap<>();
  private final Map<String, Group> groupById = new HashMap<>();
  //private final Map<Group, Set<String>> groupOffBy = new HashMap<>();
  private String globalLoopMode;
  private Group currentGroup; //= new Group();
  private Region currentRegion; //= new Region(currentGroup);
  private Scope scope = Scope.global;

  public SfzSamplerProgram(final SfzParser parser, final URL programResource, final File sampleBase) throws IOException, SfzParserException {
    assert sampleBase != null;
    assert sampleBase.exists();
    assert sampleBase.isDirectory();
    this.sampleBase = sampleBase;
    parser.addObserver(this);
    info("Added self as an observer.");
    info("parsing program: " + programResource);
    parser.parse(programResource);
    commitGroup();
    commitRegion();
    prepareGroups();
    info("done parsing program.");
  }

  private void prepareGroups() {
    for (Group group : allGroups) {
      // set the low and high keys range to keys, if they aren't set yet.
      if (group.getHikey() != null & group.getLokey() != null) {
        final byte min = group.getLokey().getValue();
        final byte max = group.getHikey().getValue();
        for (int i = min; i <= max; i++) {
          group.addKey((byte) i);
        }
      }

      // put the group in groupByNote across its keyspan
      for (Note key : group.getKeys()) {
        groupByNote.put(key.getValue(), group);
      }

      // setup the offBy stuff
      for (String groupId : group.getOffByGroups()) {
        //info("looking for off-by group: " + groupId);
        final Group offByGroup = groupById.get(groupId);
        if (offByGroup != null) {
          //info("setting off group: offgroup: " + group.getGroupId() + ", offbygroup: " + offByGroup.getGroupId());
          offByGroup.addOffGroup(group);
        }
      }
    }
  }

  @Override
  public Sample getSampleForNote(byte i, byte velocity) {
    final Region region = regions[i][velocity];
    info("region for note on: velocity: " + velocity + ", regin: " + region);
    return region == null ? null : region.getSample();
  }

  @Override
  public Set<Byte> getOffNotesForNoteOn(byte note) {
    final Set<Byte> rv = new HashSet<>();
    // Find all the groups that should be turned off by this on note
    // XXX: This is probably wrong. It's probably allowed to have more than one group per note
    final Group group = groupByNote.get(note);
    if (group != null) {
      final Set<Group> offGroups = group.getOffGroups();
      for (Group offGroup : offGroups) {
        for (Note key : offGroup.getKeys()) {
          rv.add(key.getValue());
        }
      }
    }
    rv.remove(note);
    return rv;
  }

  @Override
  public Set<Byte> getOffNotesForNoteOff(byte note, byte onVelocity) {
    final Set<Byte> rv = new HashSet<>();

    final Region region = regions[note][onVelocity];
    info("region for notes off: onVelocity: " + onVelocity + ", region: " + region);
    if (region != null) {
      final String loopMode = region.getLoopMode();
      if (loopMode != null && !loopMode.startsWith("one_shot")) {
        rv.add(note);
      }
    }
    // XXX: This is wrong. The loop mode of the individual region should have priority over the
    // loop mode of the group
    final Group group = groupByNote.get(note);
    info("group for notes off: onVelocity: " + onVelocity + ", note: " + note + ", group: " + group);
    if (group != null) {
      final String loopMode = group.getLoopMode();
      if (!"one_shot".equals(loopMode)) {
        rv.add(note);
      }
    }
    return rv;
  }

  @Override
  public void notifyGroup() {
    changeScope(Scope.group);
    commitGroup();
    currentGroup = new Group();
    if (globalLoopMode != null) {
      currentGroup.setLoopMode(globalLoopMode);
    }
  }

  private void commitGroup() {
    if (currentGroup != null) {
      allGroups.add(currentGroup);
    }
  }

  @Override
  public void notifyRegion() {
    //scope = Scope.region;
    changeScope(Scope.region);
    commitRegion();
    //info("new region");
    if (currentGroup == null) {
      currentGroup = new Group();
    }
    currentRegion = new Region(currentGroup);
  }

  private void changeScope(final Scope scope) {
    //info("change scope from " + this.scope + " to " + scope);
    this.scope = scope;
  }

  private void commitRegion() {
    if (currentRegion != null) {

      if (currentRegion.getLokey() != null && currentRegion.getHikey() != null) {
        byte min = currentRegion.getLokey().getValue();
        byte max = currentRegion.getHikey().getValue();
        for (int i = min; i <= max; i++) {
          currentRegion.addKey((byte) i);
        }
      }

      byte hivel = currentRegion.getHivel();
      byte lovel = currentRegion.getLovel();

      //info("keys: " + key + ", lokey: " + lokey + ", hikey: " + hikey + ", lovel: " + lovel + ", hivel: " + hivel);
      for (Note key : currentRegion.getKeys()) {
        byte value = key.getValue();
        for (int velocity = lovel; velocity <= hivel; velocity++) {
          regions[value][velocity] = currentRegion;
        }
      }
    }
  }

  @Override
  public void notifySample(String sample) {
    try {
      final File sampleFile = new File(sampleBase, sample);
      currentRegion.setSample(new Sample(sampleFile.getAbsolutePath()));
      //info("Notify sample: " + sampleFile + ", currentRegion: " + currentRegion);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void info(String s) {
    System.out.println(getClass().getSimpleName() + ": " + s);
  }

  @Override
  public void notifyLokey(Note lokey) {
    switch (scope) {
      case group:
        currentGroup.setLokey(lokey);
        break;
      case region:
        currentRegion.setLokey(lokey);
        break;
    }
    info("Notify lokey: " + lokey + ", currentGroup: " + currentGroup + ", currentRegion: " + currentRegion);
  }

  @Override
  public void notifyHikey(Note hikey) {
    switch (scope) {
      case group:
        currentGroup.setHikey(hikey);
        break;
      case region:
        currentRegion.setHikey(hikey);
        break;
    }
    info("Notify hikey: " + hikey + ", currentGroup: " + currentGroup + ", currentRegion: " + currentRegion);
  }

  @Override
  public void notifyPitchKeycenter(Note pitchKeycenter) {
    // TODO: handle regions that span more than one note
  }

  @Override
  public void notifyKey(byte key) {
    switch (scope) {
      case group:
        currentGroup.addKey(key);
        break;
      case region:
        currentRegion.addKey(key);
        break;
    }
    //info("Notify key: " + key + ", currentRegion: " + currentRegion);
  }

  @Override
  public void notifyHivel(byte hivel) {
    currentRegion.setHivel(hivel);
  }

  @Override
  public void notifyLovel(byte lovel) {
    currentRegion.setLovel(lovel);
  }

  @Override
  public void notifyGroupId(String groupId) {
    final Group group = groupById.get(groupId);
    if (group != null) {
      group.merge(currentGroup);
      currentGroup = group;
    } else {
      groupById.put(groupId, currentGroup);
      currentGroup.setGroupId(groupId);
    }
  }

  @Override
  public void notifyLoopMode(String loopMode) {
    info("setting loop mode: " + loopMode);
    switch (scope) {
      case global:
        globalLoopMode = loopMode;
        break;
      case group:
        currentGroup.setLoopMode(loopMode);
        break;
      case region:
        currentRegion.setLoopMode(loopMode);
        break;
    }
  }

  @Override
  public void notifyOffBy(String offBy) {
    if (scope.equals(Scope.group)) {
      currentGroup.setOffByGroups(offBy);
    }
  }


  private class Group {
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

    @Override
    public String toString() {
      byte lokeyVal = lokey == null ? -1 : lokey.getValue();
      byte hikeyVal = hikey == null ? -1 : hikey.getValue();
      return "[" + getClass().getSimpleName() + ": loopMode: " + loopMode + ", keys: " + keys;
    }

    public void setGroupId(String groupId) {
      this.groupId = groupId;
    }

    public String getGroupId() {
      return groupId;
    }

    public void setLoopMode(String loopMode) {
      this.loopMode = loopMode;
      info("Set loop mode: " + loopMode + ", this: " + this);
    }

    public void setOffByGroups(String offByGroup) {
      this.offByGroups.add(offByGroup);
    }

    public void addRegion(Region region) {
      this.regions.add(region);
    }

    public Set<String> getOffByGroups() {
      return new HashSet<>(offByGroups);
    }

    public void merge(Group group) {
      if (group.getHikey() != null) {
        setHikey(group.getHikey());
      }
      if (group.getLokey() != null) {
        setLokey(group.getLokey());
      }
      if (group.getGroupId() != null) {
        setGroupId(group.getGroupId());
      }
      if (group.getLoopMode() != null) {
        this.loopMode = group.getLoopMode();
      }

      keys.addAll(group.keys);
      regions.addAll(group.regions);
      offGroups.addAll(group.getOffGroups());
      offByGroups.addAll(group.getOffByGroups());
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
  }

  private class Region extends Group {

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
  }
}
