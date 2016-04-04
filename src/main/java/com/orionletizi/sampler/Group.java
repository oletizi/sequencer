package com.orionletizi.sampler;

import org.jfugue.theory.Note;

import java.util.Set;

public interface Group {
  Note getHikey();

  void setHikey(Note hikey);

  Note getLokey();

  void setLokey(Note lokey);

  void addKey(int key);

  void addKey(Note key);

  Set<Note> getKeys();
}
