package io.jaegertracing.dsl.gremlin.ui;

import java.io.Serializable;

/**
 * @author Pavol Loffay
 */
public class KeyValue implements Serializable {
  private static final long serialVersionUID = 0L;

  private String key;
  private String valueType;

  // TODO there are more types: double, long, binary, not needed at the moment
  private String valueString;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValueString() {
    return valueString;
  }

  public void setValueString(String valueString) {
    this.valueString = valueString;
  }

  public String getValueType() {
    return valueType;
  }

  public void setValueType(String valueType) {
    this.valueType = valueType;
  }
}
