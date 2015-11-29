package com.orionletizi.sampler;

import net.beadsproject.beads.analysis.featureextractors.FFT;
import net.beadsproject.beads.analysis.featureextractors.PeakDetector;
import net.beadsproject.beads.analysis.featureextractors.PowerSpectrum;
import net.beadsproject.beads.analysis.featureextractors.SpectralDifference;
import net.beadsproject.beads.analysis.segmenters.ShortFrameSegmenter;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.AudioIO;
import net.beadsproject.beads.core.Bead;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.BiquadFilter;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.RecordToFile;
import net.beadsproject.beads.ugens.SamplePlayer;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class BeadsAnalysisTest {

  private AudioContext ac;
  private Gain gain;
  private PeakDetector od;


  @Before
  public void before() throws Exception {
    AudioIO io = new JavaSoundAudioIO();
    ac = new AudioContext(io);
    gain = new Gain(ac, 1);
    ac.out.addInput(gain);

  }

  @Test
  public void test() throws Exception {

  /*
   * Set up the context and load a sample.
   */
    String audioFileName = ClassLoader.getSystemResource("audio/HH.wav").getFile();
    Sample sample = SampleManager.sample(audioFileName);
    SamplePlayer player = new SamplePlayer(ac, sample);

    BiquadFilter filter = new BiquadFilter(ac, 2, BiquadFilter.Type.HP);
    filter.setFrequency(4000f);
    filter.addInput(player);
    gain.addInput(filter);
    ac.out.addInput(gain);

  /*
   * To analyse a signal, build an analysis chain.
   * We also manually set parameters of the sfs.
   */
    ShortFrameSegmenter sfs = new ShortFrameSegmenter(ac);
    sfs.setChunkSize(2048);
    sfs.setHopSize(441);
    sfs.addInput(ac.out);
    FFT fft = new FFT();
    PowerSpectrum ps = new PowerSpectrum();
    sfs.addListener(fft);
    fft.addListener(ps);

  /*
   * Given the power spectrum we can now detect changes in spectral energy.
   */
    SpectralDifference sd = new SpectralDifference(ac.getSampleRate());
    ps.addListener(sd);
    od = new PeakDetector();
    sd.addListener(od);
  /*
   * These parameters will need to be adjusted based on the
   * type of music. This demo uses the mouse position to adjust
   * them dynamically.
   * mouse.x controls Threshold, mouse.y controls Alpha
   */
    od.setThreshold(0.009f);
    od.setAlpha(.9f);

  /*
   * OnsetDetector sends messages whenever it detects an onset.
   */
    final Chunker chunker = new Chunker();
    od.addMessageListener(chunker);

    ac.out.addDependent(sfs);

    chunker.start();
    //and begin
    ac.start();
    Thread.sleep(20 * 1000);
    chunker.kill();
    ac.stop();
  }

  private void info(String s) {
    System.out.println(s);
  }

  private class Chunker extends Bead {
    private RecordToFile recorder;

    public Chunker() {
      super();
      try {
        swap();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void start() {
      super.start();
      recorder.start();
    }

    private void swap() throws IOException {
      if (recorder != null) {
        recorder.kill();
      }
      recorder = new RecordToFile(ac, 1, new File("/tmp/htt-" + System.currentTimeMillis() + ".wav"));
      recorder.addInput(ac.out);
      ac.out.addDependent(recorder);
    }

    protected void messageReceived(Bead b) {
      info("Message receive!");
      try {
        swap();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void kill() {
      super.kill();
      recorder.kill();
    }
  }
}
