package com.orionletizi.com.orionletizi.midi.device;

import com.orionletizi.sampler.sfz.SfzSampler;

import javax.sound.midi.*;
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
    if (info != null) {
      final MidiDevice device = MidiSystem.getMidiDevice(info);
      device.open();
      final MidiAdapter adapter = new MidiAdapter(device);
      adapter.addReceiver(new SfzSampler());
    }
    final Object lock = new Object();
    synchronized (lock) {
      lock.wait();
    }
  }

  private static void info(String s) {
    System.out.println(MidiAdapter.class.getSimpleName() + ": " + s);
  }

  @Override
  public void send(MidiMessage message, long timeStamp) {
    for (Receiver receiver : receivers) {
      receiver.send(message, timeStamp);
    }
  }

  @Override
  public void close() {

  }
}