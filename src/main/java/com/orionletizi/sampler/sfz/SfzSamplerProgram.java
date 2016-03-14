package com.orionletizi.sampler.sfz;

import com.orionletizi.sampler.SamplerProgram;
import com.orionletizi.sampler.SamplerProgramParserException;
import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
import net.beadsproject.beads.data.Sample;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jfugue.theory.Note;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

// TODO: Implement random sample selection
// TODO: Figure out why loop mode isn't working
public class SfzSamplerProgram implements SamplerProgram, SfzParserObserver {

  private static final Logger logger = LoggerImpl.forClass(SfzSamplerProgram.class);

  private URL programResource;


  private enum Scope {
    global,
    group,
    region
  }

  //  private final File sampleBase;
  private final Set<Group> allGroups = new HashSet<>();
  private final Map<Integer, Group> groupByNote = new HashMap<>();
  private final Map<String, Set<Group>> groupsById = new HashMap<>();
  private final Region[][] regions = new Region[128][128];
  private final Map<Integer, Set<Region>> regionsByKey = new HashMap<>();
  private String globalLoopMode;
  private Group currentGroup;
  private Region currentRegion;
  private Scope scope = Scope.global;

  public SfzSamplerProgram(final SfzParser parser, final File programFile) throws IOException, SamplerProgramParserException {
    this(parser, programFile.toURI().toURL());
  }

  public SfzSamplerProgram(final SfzParser parser, final URL programResource) throws IOException, SamplerProgramParserException {
    if (!programResource.getProtocol().startsWith("file")) {
      throw new IllegalArgumentException("I don't support non-file URLs yet: " + programResource);
    }
    this.programResource = programResource;
    parser.addObserver(this);
    parser.parse(programResource);
    commitGroup();
    commitRegion();
    prepareGroups();
    prepareRegions();
  }

  public URL getProgramResource() {
    return programResource;
  }

  @Override
  public SamplerProgram copyTo(final File destDir) throws IOException {
    FileUtils.forceMkdir(destDir);
    if (!destDir.isDirectory()) {
      throw new IOException("Couldn't create destDir: " + destDir);
    }

    // load the program file
    String programSource = IOUtils.toString(programResource.openStream());

    // copy the samples
    final File sampleDir = new File(destDir, "samples");
    FileUtils.forceMkdir(sampleDir);
    if (!sampleDir.isDirectory()) {
      throw new IOException("Failed to create sample directory: " + sampleDir);
    }

    // collect a set of unique source sample files
    final Set<String> samplePaths = new HashSet<>();
    final SfzParser parser = new SfzParser();
    parser.addObserver(new SfzParserObserverAdapter() {
      @Override
      public void notifySample(String sample) {
        samplePaths.add(sample);
      }
    });
    try {
      parser.parse(programResource);
    } catch (SamplerProgramParserException e) {
      throw new IOException(e);
    }

    // copy all the source sample files to the samples directory
    for (String sourcePath : samplePaths) {
      final URL sourceResource = new URL(programResource, URLEncoder.encode(FilenameUtils.separatorsToUnix(sourcePath), "UTF-8"));
      final String sampleName = URLDecoder.decode(FilenameUtils.getName(sourceResource.getFile()), "UTF-8");
      info("Source path: " + sourcePath + ", sample name: " + sampleName);

      final String destPath = FilenameUtils.separatorsToSystem(sampleDir.getName() + File.separatorChar + sampleName);
      final File dest = new File(sampleDir, sampleName);
      assert programSource.contains(sourcePath);
      programSource = programSource.replace(sourcePath, destPath);
      final File sourceFile = new File(URLDecoder.decode(sourceResource.getFile(), "UTF-8"));
      FileUtils.copyFile(sourceFile, dest);
      if (!dest.isFile()) {
        throw new IOException("Failed to copy sample file: source: " + sourceResource + ", dest: " + dest);
      }
    }

    // write the program source to the new location
    final File destFile = new File(destDir, destDir.getName() + ".sfz");
    FileUtils.writeStringToFile(destFile, programSource);
    if (!destFile.isFile()) {
      throw new IOException("Failed to write program file: " + destFile);
    }

    try {
      return new SfzSamplerProgram(new SfzParser(), destFile);
    } catch (SamplerProgramParserException e) {
      throw new IOException(e);
    }
  }

  public Region[][] getRegions() {
    final Region[][] rv = new Region[regions.length][];
    for (int i = 0; i < regions.length; i++) {
      rv[i] = ArrayUtils.clone(regions[i]);
    }
    return rv;
  }

  @SuppressWarnings("unused")
  public Set<Region> getRegionsByKey(final int key) {
    return regionsByKey.get(key);
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
        groupByNote.put((int) key.getValue(), group);
      }

      // setup the offBy stuff
      for (String groupId : group.getOffByGroups()) {
        final Set<Group> offByGroups = groupsById.get(groupId);
        if (offByGroups != null) {
          for (Group offByGroup : offByGroups) {
            offByGroup.addOffGroup(group);
          }
        }
      }
    }
  }

  private void prepareRegions() throws SamplerProgramParserException {
    for (Map.Entry<Integer, Set<Region>> entry : regionsByKey.entrySet()) {
      final ArrayList<Region> regions = new ArrayList<>(entry.getValue());
      // sort by hivel, ascending...
      // XXX: this doesn't account for only setting lovel
      Collections.sort(regions, (r1, r2) -> r1.getHivel() - r2.getHivel());

      // map the low and high velocity of the regions for this key
      for (int i = 0; i < regions.size(); i++) {
        final Region region = regions.get(i);
        if (i == 0 && i + 1 < regions.size()) {
          // handle the first region
          final Region nextRegion = regions.get(i + 1);
          if (region.getHivel() == 127) {
            // the first region has the max hivel; set its hivel to the lovel - 1 of the next region
            if (nextRegion.getLovel() == 0) {
              // the velocities aren't set right
              throw new SamplerProgramParserException("Region velocity ranges overlap: " + region + ", " + nextRegion);
            }
            region.setHivel((byte) (nextRegion.getLovel() - 1));
          } else if (nextRegion.getLovel() == 0) {
            // the next region has the default lovel; set its lovel to this region's hivel + 1
            final int nextLovel = region.getHivel() + 1;
            nextRegion.setLovel(nextLovel);
          }
        } else if (i > 0 && i + 1 < regions.size()) {
          // handle the middle regions; the lovel of the first region should be set properly by the code above
          final Region nextRegion = regions.get(i + 1);
          final int nextLovel = region.getHivel() + 1;
          nextRegion.setLovel(nextLovel);
        } // nothing to do for the last region
      }

      // now fill in the regions matrix
      for (Region region : regions) {
        for (Note note : region.getKeys()) {
          final byte key = note.getValue();
          final int minVelocity = region.getLovel();
          final int maxVelocity = region.getHivel();

          for (int i = minVelocity; i <= maxVelocity; i++) {
            this.regions[key][i] = region;
          }
        }
      }

    }

  }

  @Override
  public Sample getSampleForNote(int note, int velocity) {
    final Region region = regions[note][velocity];
    info("region for note on: velocity: " + velocity + ", region: " + region);
    return region == null ? null : region.getSample();
  }

  @Override
  public Set<Integer> getOffNotesForNoteOn(int note) {
    final Set<Integer> rv = new HashSet<>();
    // Find all the groups that should be turned off by this on note
    // XXX: This is probably wrong. It's probably allowed to have more than one group per note
    final Group group = groupByNote.get(note);
    if (group != null) {
      final Set<Group> offGroups = group.getOffGroups();
      for (Group offGroup : offGroups) {
        // RoboVM gets freaked out by streams, so I'm not using them
        for (Note key : offGroup.getKeys()) {
          rv.add((int) key.getValue());
        }
      }
    }
    rv.remove(note);
    return rv;
  }

  @Override
  public Set<Integer> getOffNotesForNoteOff(int note, int onVelocity) {
    final Set<Integer> rv = new HashSet<>();

    final Region region = regions[note][onVelocity];
    //info("region for notes off: onVelocity: " + onVelocity + ", region: " + region);
    String loopMode = null;
    final Group group = groupByNote.get(note);
    if (group != null) {
      loopMode = group.getLoopMode();
    }

    if (region != null) {
      if (region.getLoopMode() != null) {
        loopMode = region.getLoopMode();
      }
    }

    info("LOOP MODE: " + loopMode);
    if (!"one_shot".equals(loopMode)) {
      rv.add(note);
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

  private void commitRegion() {
    if (currentRegion != null) {
      if (currentRegion.getLokey() != null && currentRegion.getHikey() != null) {
        final byte min = currentRegion.getLokey().getValue();
        final byte max = currentRegion.getHikey().getValue();
        for (int i = min; i <= max; i++) {
          currentRegion.addKey((byte) i);
        }
      }
      for (Note note : currentRegion.getKeys()) {
        final int key = note.getValue();
        Set<Region> regions = regionsByKey.get(key);
        if (regions == null) {
          regions = new HashSet<>();
          regionsByKey.put(key, regions);
        }
        regions.add(currentRegion);
      }
    }
  }

  @Override
  public void notifyRegion() {
    changeScope(Scope.region);
    if (currentGroup == null) {
      currentGroup = new Group();
    }
    commitRegion();
    currentRegion = new Region(currentGroup);
  }

  private void changeScope(final Scope scope) {
    this.scope = scope;
  }

  @Override
  public void notifySample(String sample) throws IOException {
    info("Notify sample: " + sample + ", currentRegion: " + currentRegion);
    final String unixifiedSample = FilenameUtils.separatorsToUnix(sample);
    info("Unixified: " + unixifiedSample);
    final URL sampleResource = new URL(programResource, URLEncoder.encode(unixifiedSample, "UTF-8"));
    currentRegion.setSample(new Sample(URLDecoder.decode(sampleResource.getFile(), "UTF-8")));
  }

  @SuppressWarnings("unused")
  private void info(String s) {
    //System.out.println(getClass().getSimpleName() + ": " + s);
    logger.info(s);
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
  public void notifyPitchKeytrack(int keytrack) {
    currentRegion.setPitchKeytrack(keytrack);
  }

  @Override
  public void notifyKey(int key) {
    switch (scope) {
      case group:
        currentGroup.addKey(key);
        break;
      case region:
        currentRegion.addKey(key);
        break;
    }
  }

  @Override
  public void notifyHivel(int hivel) {
    assert hivel >= 0 && hivel <= Byte.MAX_VALUE;
    currentRegion.setHivel(hivel);
  }

  @Override
  public void notifyLovel(int lovel) {
    assert lovel >= 0 && lovel <= Byte.MAX_VALUE;
    currentRegion.setLovel(lovel);
  }

  @Override
  public void notifyGroupId(String groupId) {
    currentGroup.setGroupId(groupId);

    Set<Group> groups = groupsById.get(groupId);
    if (groups == null) {
      groups = new HashSet<>();
      groupsById.put(groupId, groups);
    }
    groups.add(currentGroup);
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


}
