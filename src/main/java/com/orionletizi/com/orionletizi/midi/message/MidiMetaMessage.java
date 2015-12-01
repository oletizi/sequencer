package com.orionletizi.com.orionletizi.midi.message;

import com.orionletizi.music.theory.Tempo;
import com.orionletizi.music.theory.TimeSignature;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.sound.midi.MetaMessage;
import java.math.BigInteger;

public class MidiMetaMessage {

  private final MetaMessage meta;

  private enum Type {
    trackName(0x03),
    instrumentName(0x04),
    setTempo(0x51),
    timeSignature(0x58);

    private final int type;

    Type(int type) {
      this.type = type;
    }

    public int getType() {
      return type;
    }
  }

  public MidiMetaMessage(MetaMessage meta) {
    this.meta = meta;
  }

  public boolean isTrackName() {
    return isType(Type.trackName);
  }

  public boolean isInstrumentName() {
    return isType(Type.instrumentName);
  }

  public boolean isSetTempo() {
    return isType(Type.setTempo);
  }

  public boolean isTimeSignature() {
    return isType(Type.timeSignature);
  }

  private boolean isType(Type type) {
    return meta.getType() == type.getType();
  }

  public String getText() {
    assert isTrackName() || isInstrumentName();
    return new String(getData());
  }

  public byte[] getData() {
    return meta.getData();
  }

  public Tempo getTempo() {
    assert isSetTempo();
    return Tempo.newTempoFromMicroseconds(new BigInteger(getData()).doubleValue());
  }

  public TimeSignature getTimeSignature() {
    assert isTimeSignature();
    return new TimeSignature(getData()[0], (int) Math.pow(2, getData()[1]));
  }

  public int length() {
    return meta.getLength();
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("type", String.format("%02X ", meta.getType()))
        .append("length", meta.getLength())
        .append("message", meta.getMessage())
        .append("data", getData())
        .toString();
  }
}
