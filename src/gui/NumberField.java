package gui;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

class NumberField extends TextField {
  private static final TextFieldFilter FILTER =
      new TextFieldFilter.DigitsOnlyFilter();

  private int value;
  private int min = Integer.MIN_VALUE;
  private int max = Integer.MAX_VALUE;

  NumberField (int defaultValue, Skin skin) {
    super("", skin);
    setValue(defaultValue);
    setTextFieldFilter(FILTER);
    setTextFieldListener((textField, c) -> {
      String text1 = getText();

      if (!text1.isEmpty()) {
        try {
          int newValue = Integer.parseInt(text1);
          setValue(newValue);

          if (newValue != getValue()) {
            setCursorPosition(getText().length());
          }
        } catch (NumberFormatException nfe) {
          setText("" + value);
          setCursorPosition(getText().length());
        }
      } else {
        value = min;
      }
    });
  }

  public int getValue () {
    return value;
  }

  public void setValue (int value) {
    this.value = MathUtils.clamp(value, min, max);
    setText("" + this.value);
  }

  public void setMin (int min) {
    this.min = min;
    if (value < min) {
      setValue(min);
    }
  }

  public void setMax (int max) {
    this.max = max;
    if (value > max) {
      setValue(max);
    }
  }
}