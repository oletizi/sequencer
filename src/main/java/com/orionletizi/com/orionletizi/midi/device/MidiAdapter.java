package com.orionletizi.com.orionletizi.midi.device;

import com.orionletizi.sampler.Sampler;
import com.orionletizi.sampler.sfz.SfzParser;
import com.orionletizi.sampler.sfz.SfzSamplerProgram;
import net.beadsproject.beads.core.AudioContext;
import net.beadsproject.beads.core.io.JavaSoundAudioIO;

import javax.sound.midi.*;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;


public class MidiAdapter implements Receiver {


  private final MidiDevice device;
  private final Set<Receiver> receivers = new HashSet<>();

  public MidiAdapter(MidiDevice device) throws MidiUnavailableException {
    this.device = device;
    final Transmitter tx = device.getTransmitter();
    tx.setReceiver(this);
  }

  public void addReceiver(final Receiver receiver) {
    info("adding receiver: " + receiver);
    receivers.add(receiver);
  }

  public static void main(String args[]) throws Exception {
    final javax.sound.midi.MidiDevice.Info[] midiDeviceInfo = MidiSystem.getMidiDeviceInfo();
    javax.sound.midi.MidiDevice.Info info = null;
    for (int i = 0; i < midiDeviceInfo.length; i++) {
      info = midiDeviceInfo[i];
      System.out.println("info: " + info);
      if ("Arturia BeatStep".equals(info.getName())) {
        break;
      }
    }
    final MidiDevice device = MidiSystem.getMidiDevice(info);
    info("got midi device: " + device);
    device.open();
    info("opened midi device: " + device);

    final MidiAdapter adapter = new MidiAdapter(device);
    info("created midi adapter: " + adapter);

    final URL programResource = ClassLoader.getSystemResource("program/drums/program.sfz");
    info("Got program resource: " + programResource);

    final SfzSamplerProgram program = new SfzSamplerProgram(new SfzParser(), programResource,
        new File(programResource.getFile()).getParentFile());
    info("Got program: " + programResource);

    final Sampler sampler = new Sampler(new AudioContext(new JavaSoundAudioIO()), program);
    info("Created sampler: " + sampler);

    adapter.addReceiver(sampler);
    info("added sampler as receiver: " + sampler);

    final Object lock = new Object();
    synchronized (lock) {
      lock.wait();
    }
  }

  private static void info(String s) {
    //System.out.println(MidiAdapter.class.getSimpleName() + ": " + s);
  }

  @Override
  public void send(MidiMessage message, long timeStamp) {
    info("sending message to receivers: " + receivers);
    for (Receiver receiver : receivers) {
      info("sending message " + message + " to receiver: " + receiver);
      receiver.send(message, timeStamp);
    }
  }

  @Override
  public void close() {

  }
}