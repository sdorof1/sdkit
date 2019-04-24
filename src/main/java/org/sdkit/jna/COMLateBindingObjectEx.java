package org.sdkit.jna;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.OaIdl.SAFEARRAY;
import com.sun.jna.platform.win32.OleAuto;
import com.sun.jna.platform.win32.Variant;
import com.sun.jna.platform.win32.Variant.VARIANT;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.COMLateBindingObject;

public class COMLateBindingObjectEx extends COMLateBindingObject {

  protected COMLateBindingObjectEx(String progId, boolean useActiveInstance) throws COMException {
    super(progId, useActiveInstance);
  }

  protected long getLongProperty(String propertyName) {
    VARIANT.ByReference result = new VARIANT.ByReference();
    this.oleMethod(OleAuto.DISPATCH_PROPERTYGET, result, propertyName);
    return ((Number) result.getValue()).longValue();
  }

  protected double getDoubleProperty(String propertyName) {
    VARIANT.ByReference result = new VARIANT.ByReference();
    this.oleMethod(OleAuto.DISPATCH_PROPERTYGET, result, propertyName);
    return ((Number) result.getValue()).doubleValue();
  }

  protected double[] doubleArrayValue(SAFEARRAY value) {
    if (value == null) {
      return null;
    }

    Pointer dataPointer = value.accessData();
    int size = value.getUBound(0) + 1;
    double[] result = new double[size];
    if (size > 0) {
      VARIANT[] variantData = (VARIANT[]) new VARIANT(dataPointer).toArray(size);
      for (int i = 0; i < size; ++i) {
        result[i] = (double) variantData[i].getValue();
      }
    }
    value.unaccessData();
    value.destroy();
    return result;
  }

  protected String[] stringArrayValue(SAFEARRAY value) {
    if (value == null) {
      return null;
    }

    Pointer dataPointer = value.accessData();
    int size = value.getUBound(0) + 1;
    String[] result = new String[size];
    if (size > 0) {
      VARIANT[] variantData = (VARIANT[]) new VARIANT(dataPointer).toArray(size);
      for (int i = 0; i < size; ++i) {
        result[i] = variantData[i].getValue().toString();
      }
    }
    value.unaccessData();
    value.destroy();
    return result;
  }

  // https://github.com/java-native-access/jna/blob/master/contrib/platform/test/com/sun/jna/platform/win32/SAFEARRAYTest.java
  protected String[][] stringArrayValue2D(SAFEARRAY varArray) {
    if (varArray == null) {
      return null;
    }

    if (varArray.getDimensionCount() != 2) {
      throw new IllegalArgumentException();
    }

    int rowCount = varArray.getUBound(0) + 1;
    int colCount = varArray.getUBound(1) + 1;

    String[][] result = new String[rowCount][colCount];

    for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
      for (int colIdx = 0; colIdx < colCount; colIdx++) {
        VARIANT element = (VARIANT) varArray.getElement(rowIdx, colIdx);
        result[rowIdx][colIdx] = element.stringValue();
        OleAuto.INSTANCE.VariantClear(element);
      }
    }
    varArray.destroy();
    return result;
  }

  protected SAFEARRAY createSafeArray(double[] array) {
    SAFEARRAY sarr = SAFEARRAY.createSafeArray(array.length);
    VARIANT var = new VARIANT();
    for (int i = 0; i < array.length; ++i) {
      var.setValue(Variant.VT_R8, array[i]);
      sarr.putElement(var, i);
    }
    return sarr;
  }

  protected SAFEARRAY createSafeArray(String[] array) {
    SAFEARRAY sarr = SAFEARRAY.createSafeArray(new WTypes.VARTYPE(Variant.VT_BSTR), array.length);
    for (int i = 0; i < array.length; ++i) {
      sarr.putElement(array[i], i);
    }
    return sarr;
  }

  protected class ValueHolder<T> {
    private VARIANT variant;
    private T value;

    public VARIANT getVariant() {
      return variant;
    }

    public T getValue() {
      return value;
    }

    public ValueHolder(int vt, T value) {
      this.value = value;
      variant = new VARIANT();
      variant.setValue(vt, value);
    }
  }

  protected class ArrayHolder extends ValueHolder<SAFEARRAY> {

    public ArrayHolder(double[] array) {
      super(Variant.VT_ARRAY | Variant.VT_VARIANT, createSafeArray(array));
    }

    public ArrayHolder(String[] array) {
      super(Variant.VT_ARRAY | Variant.VT_BSTR, createSafeArray(array));
    }

    public void destroy() {
      getValue().destroy();
    }
  }
}
