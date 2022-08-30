/*
 * Copyright (c) 2016 Newshunt. All rights reserved.
 */
package com.newshunt.common.helper.common;

import android.graphics.Paint;
import android.text.Layout;
import android.text.TextPaint;
import android.widget.TextView;

/**
 * A Helper class to truncate the String content of a textview if the lastline or line
 * number (maxLines-1)  will overlap with the viewToCheckIfOverlapping.
 * Edge case handled.
 * 1. If viewToCheckForOverlapping width + elipsize width is greater that textview width itself,
 * then this class truncates the string upto the characters count present in the lines upto
 * maxLines -1
 * 2. If there is no overlap, there is no need of truncation, in which case, this class returns
 * truncated and untruncated string both the same
 * 3. If the line count < maxLines, there is no need of truncation, this class returns
 * truncated and untruncated string both the same
 *
 * @author santhosh.kc
 * //TODO (santhosh.kc) to merge this with {@link TextTruncator} once we extend this concept to
 * Gallery
 */
public class TextTruncator2 {

  private final String description;
  private final int maxLines;
  private final TextView textView;
  private int truncateAt = Integer.MIN_VALUE;
  private boolean shouldAddElipsize;
  private String overlapText;
  private int overLapTextWidth;

  /**
   * Constructor
   *
   * @param description - string content to be truncated
   * @param maxLines    - maxLines of the text view to fit without overlapping
   * @param textView    - textView to have non overlapping text
   * @param overLapText - view with which to check if given text view with string
   *                    content will overlap
   * @throws IllegalArgumentException if any arguments are illegal
   *                                  eg. 1. MaxLines should be >= 1
   *                                  2. if any parameters to this constructor is null
   */
  public TextTruncator2(String description, int maxLines, TextView textView, String overLapText)
      throws IllegalArgumentException {
    if (description == null || maxLines <= 0 || textView == null) {
      throw new IllegalArgumentException("illegal arguments");
    }
    this.description = description;
    this.maxLines = maxLines;
    this.textView = textView;
    overlapText = overLapText;
    Paint paint = textView.getPaint();
    overLapTextWidth = (int) paint.measureText(overLapText);
  }

  public String getUnTruncatedString() {
    return description;
  }

  public String getTruncatedString() {
    if (truncateAt == Integer.MIN_VALUE) {
      findTruncateAtPosition();
    }

    String truncatedString = Constants.EMPTY_STRING;
    if (!description.equals(Constants.EMPTY_STRING) && truncateAt >= 0 && truncateAt <
        description.length()) {
      truncatedString = description.substring(0,
          truncateAt + 1) + (shouldAddElipsize ? Constants.ELLIPSIZE_END : Constants
          .EMPTY_STRING) + overlapText;
    }
    return truncatedString;
  }

  public int getTruncatedPosition() {
    if (!truncatesText()) {
      return description.length() - 1;
    }
    String truncatedString = getTruncatedString();
    return truncatedString.lastIndexOf(overlapText);
  }

  public boolean truncatesText() {
    if (truncateAt == Integer.MIN_VALUE) {
      findTruncateAtPosition();
    }
    return truncateAt >= 0 && truncateAt < description.length() - 1;
  }

  private void findTruncateAtPosition() {
    Layout textLayout = textView.getLayout();
    if (textLayout == null || textLayout.getLineCount() == 0) {
      return;
    }
    if (textLayout.getLineCount() < maxLines) {
      truncateAt = description.length() - 1;
      return;
    }
    int prevLineEndingCharacterCount = textLayout.getLineStart(maxLines - 1) - 1;
    truncateAt = truncateTextViewToLines(textView.getText().toString(), textView, maxLines);
    shouldAddElipsize = (truncateAt < description.length() - 1) && truncateAt >
        prevLineEndingCharacterCount;
  }

  /*
   * An utility function to truncate the string to fit in (maxLines-1) line number along with
   * elipsizes o that it does not overlap with viewToCheckForOverlapping.
   * <p/>
   * Edge case:
   * 1. If viewToCheckForOverlapping width + elipsize width is greater that textview width itself,
   * then this function returns ending character count of line number (maxLines - 2)
   * 2. If there is no overlap, there is no need of truncation, in which case, this function
   * returns textContent.length() - 1
   * 3. If the line count < maxLines, there is no need of truncation, this function returns
   * textContent.length() - 1
   *
   * @param textContent               - text content to be truncated to fit in the text view
   * @param textView                  - text view to show truncated text if overlapping
   * @param viewToCheckForOverlapping -  view to check if the given textview will not overlap or not
   * @param maxLines                  - maxLines count to fit in textview given without overlapping
   * @return - returns integer which indicates where to truncate the string textContent, on any
   * illegal arguments this function returns -1
   */
  private int truncateTextViewToLines(String textContent, TextView textView, int maxLines) {
    if (textContent == null || textView == null || textView
        .getLayout() == null || maxLines <= 0) {
      return -1;
    }

    int truncateAt = textContent.length() - 1;
    Layout textLayout = textView.getLayout();
    int lineCount = textLayout.getLineCount();

    if (lineCount >= maxLines) {
      TextPaint textPaint = textView.getPaint();
      boolean truncate = lineCount > maxLines || AndroidUtils.overlapsWithLine
          (textView, overLapTextWidth, maxLines - 1);
      if (truncate) {
        int start = textLayout.getLineStart(maxLines - 1), end = textLayout.getLineEnd(maxLines - 1);

        if (end < start) {
          return -1;
        }

        for (int i = 0 ; i < textLayout.getLineCount() ; i++) {
          Logger.d("TRUNCATOR","line count : " + (i+1) + ": " + textLayout.getLineStart(i) + "," +
              ""+textLayout.getLineEnd(i));
        }
        TextPaint paint = textView.getPaint();
        int elipsizeWidth = (int) Math.ceil(paint.measureText(Constants.ELLIPSIZE_END));
        int leftWidth =
            textView.getWidth() - overLapTextWidth - elipsizeWidth - textView.getPaddingLeft() -
                textView.getPaddingRight();
        if (leftWidth < 0) {
          return start - 1;
        }

        start = Math.max(0, start);
        end = Math.min(textContent.length() - 1, end);
        if ((end - start) < 0) {
          end = start;
        }

        if ((start | end | (textContent.length() - end)) < 0) {
          return -1;
        }

        int fitCharCount = textPaint.breakText(textContent, start, end
            , true, leftWidth, null);
        truncateAt = start + fitCharCount - 1;
      }
    }
    return truncateAt;
  }
}
