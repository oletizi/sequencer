package com.orionletizi.sequencer;

import com.orionletizi.util.logging.Logger;
import com.orionletizi.util.logging.LoggerImpl;
import com.sun.media.sound.StandardMidiFileReader;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import net.beadsproject.beads.ugens.RecordToFile;
import org.jfugue.midi.MidiParser;
import org.jfugue.parser.ParserListener;
import org.jfugue.theory.Chord;
import org.jfugue.theory.Note;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sound.midi.*;
import java.io.File;
import java.net.URL;

public class SequencerTest {

  private static final Logger logger = LoggerImpl.forClass(SequencerTest.class);

  private URL midiSource;
  private StandardMidiFileReader reader;
  private Sequence sequence;
  private Sequencer sequencer;
  private File sampleDirectory;
  private AudioContext ac;
  private RecordToFile recorder;

  @Before
  public void before() throws Exception {
    AudioIO io = new JavaSoundAudioIO();
    ac = new AudioContext(io);

    final File outfile = new File("/tmp/sampler-" + System.currentTimeMillis() + ".wav");
    recorder = new RecordToFile(ac, 2, outfile);
    ac.out.addDependent(recorder);
    recorder.addInput(ac.out);

    midiSource = ClassLoader.getSystemResource("midi/wurli4.mid");
    sampleDirectory = new File(ClassLoader.getSystemResource("samples/piano").getFile());
    reader = new StandardMidiFileReader();
    sequence = reader.getSequence(midiSource);
    sequencer = new Sequencer(ac, new SampleSet(sampleDirectory));
  }

  @After
  public void after() throws Exception {
    System.out.println("Stopping recorder.");
    recorder.kill();
  }

  @Test
  public void testMidParser() throws Exception {
    final MidiParser parser = new MidiParser() {
      @Override
      public void fireNotePressed(MidiEvent event, Note note) {
        super.fireNotePressed(event, note);
        logger.info("Tick: " + event.getTick() + ", Note pressed: " + note);

      }

      @Override
      public void fireNoteReleased(MidiEvent event, Note note) {
        super.fireNoteReleased(event, note);
        logger.info("Tick: " + event.getTick() + ", Note released: " + note);
      }
    };

    parser.parse(sequence);
  }

  @Test
  public void testPlay() throws Exception {
    sequencer.startParser();
    sequencer.parse(sequence);
    sequencer.startParser();
    sequencer.play();
    synchronized (this) {
      wait(60 * 1000);
    }
  }

  @Test
  public void testParser() throws Exception {
    sequencer.startParser();
    sequencer.parse(sequence);
    sequencer.stopParser();
  }

  @Test
  public void test() throws Exception {
    println("Sequence: " + sequence);
    println("division type: " + sequence.getDivisionType());
    println("microsecond length: " + sequence.getMicrosecondLength());
    println("resolution: " + sequence.getResolution());
    println("tick length: " + sequence.getTickLength());
    println("patch list...");
    for (Patch patch : sequence.getPatchList()) {
      println("patch bank: " + patch.getBank());
      println("patch program: " + patch.getProgram());
    }
    println("tracks...");
    for (Track track : sequence.getTracks()) {
      println("track size: " + track.size());
      println("track ticks: " + track.ticks());
    }


    final Track track = sequence.getTracks()[0];

    for (int i = 0; i < track.size(); i++) {
      final MidiEvent midiEvent = track.get(i);
      println("midiEvent: " + midiEvent);
      println("  tick: " + midiEvent.getTick());
      final MidiMessage message = midiEvent.getMessage();

      println("  message length: " + message.getLength());
      println("  message status: " + message.getStatus());
      for (byte b : message.getMessage()) {
        println("  message byte: " + b);
      }
    }
  }


  private void println(String msg) {
    System.out.println(msg);
  }

  private class MyParserListener implements ParserListener {
    public void beforeParsingStarts() {
      println("before parsing starts...");
    }

    public void afterParsingFinished() {
      println("after parsing finished.");
    }

    public void onTrackChanged(byte b) {
      println("onTrackChanged: " + b);
    }

    public void onLayerChanged(byte b) {
      println("onLayerChanged: " + b);
    }

    public void onInstrumentParsed(byte b) {
      println("onInstrumentParsed: " + b);
    }

    public void onTempoChanged(int i) {
      println("onTempoChanged: " + i);
    }

    public void onKeySignatureParsed(byte b, byte b1) {
      println("onKeySignatureParsed: " + b + ", b1");
    }

    public void onTimeSignatureParsed(byte b, byte b1) {
      println("onTimeSignatureParsed: " + b + ", " + b1);
    }

    public void onBarLineParsed(long l) {
      println("Bar line: " + l);
    }

    public void onTrackBeatTimeBookmarked(String s) {
      println("onTrackBeatTimeBookmarked(" + s + ")");
    }

    public void onTrackBeatTimeBookmarkRequested(String s) {
      println("onTrackBeatTimeBookmarkRequested(" + s + ")");
    }

    public void onTrackBeatTimeRequested(double v) {
      println("onTrackBeatTimeRequested(" + v + ")");
    }

    public void onPitchWheelParsed(byte b, byte b1) {
      println("onPitchWheelParsed(" + b + ", " + b1);
    }

    public void onChannelPressureParsed(byte b) {
      println("onChannelPressureParsed(" + b + ")");
    }

    public void onPolyphonicPressureParsed(byte b, byte b1) {
      println("onPolyphonicPressureParsed(" + b + ", " + b1 + ")");
    }

    public void onSystemExclusiveParsed(byte... bytes) {
      println("onSystemExclusiveParsed...");
      for (byte aByte : bytes) {
        println("    " + aByte);
      }
    }

    public void onControllerEventParsed(byte b, byte b1) {
      println("onControllerEventParsed(" + b + ", " + b1 + ")");
    }

    public void onLyricParsed(String s) {
      println("onLyricParsed(" + s + ")");
    }

    public void onMarkerParsed(String s) {
      println("onMarkerParsed(" + s + ")");
    }

    public void onFunctionParsed(String s, Object o) {
      println("onFunctionParsed(" + s + ", " + o + ")");
    }

    public void onNotePressed(Note note) {
      println("Note pressed: " + note);
    }

    public void onNoteReleased(Note note) {
      println("Note released: " + note);
    }

    public void onNoteParsed(Note note) {
      println("Note parsed: " + note);
      println("  decorator string: " + note.getDecoratorString());
      println("  duration: " + note.getDuration());
      println("  pattern: " + note.getPattern());
      println("  is rest: " + note.isRest());
      println("  is percussion note: " + note.isPercussionNote());
    }

    public void onChordParsed(Chord chord) {
      println("onChordParsed(" + chord + ")");
    }
  }

}