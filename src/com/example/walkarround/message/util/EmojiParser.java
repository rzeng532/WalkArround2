package com.example.walkarround.message.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import com.example.walkarround.R;
import com.example.walkarround.base.view.AlignCenterImageSpan;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表情资源管理
 */
public class EmojiParser {

    // Singleton stuff
    /*一个表情相当于4个字符*/
    private static final int EMOJI_INSTEAD_TEXT_COUNT = 4;
    private static EmojiParser sInstance;

    public static EmojiParser getInstance(Context context) {
        if (sInstance == null) {
            synchronized (EmojiParser.class) {
                if(sInstance == null) {
                    sInstance = new EmojiParser(context);
                }
            }
        }
        return sInstance;
    }

    private final Context mContext;
    private final String[] mSmileyTexts;
    public final String[] mEncodedSmileyTexts;
    private final Pattern mPattern;
    private final HashMap<String, Integer> mSmileyToRes;
    private final HashMap<String, String> mSmileyToText;

    /**
     * Adds ImageSpans to a CharSequence that replace textual emoticons such as :-) with a graphical version.
     * 
     * @param text
     *            A CharSequence possibly containing emoticons
     * @return A CharSequence annotated with ImageSpans covering any recognized emoticons.
     */
    public CharSequence addSmileySpans(CharSequence text) {
        return addSmileySpans(text, 0.5f);
    }

    /**
     * 转化为图片并设置图片为原高度的emojiHeight倍
     * @param text
     * @param emojiHeight
     * @return
     */
    public CharSequence addSmileySpans(CharSequence text, float emojiHeight) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int resId = mSmileyToRes.get(matcher.group());
            Drawable drawable = mContext.getResources().getDrawable(resId);
            float height = drawable.getIntrinsicHeight() * emojiHeight;
            drawable.setBounds(0, 0, (int) height, (int) height);
            builder.setSpan(new AlignCenterImageSpan(drawable), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return builder;
    }

    public CharSequence getSmileyText(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        List<String> smileyList = new ArrayList<String>();
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            smileyList.add(matcher.group());
        }
        String builder = text.toString();
        for (String replaceStr : smileyList) {
            builder = builder.replace(replaceStr, mSmileyToText.get(replaceStr));
        }
        return builder;
    }

    /**
     * 获取指定长度的字符（一个表情算一个字）
     *
     * @param text   输入的字符
     * @param length 指定的长度
     * @return
     */
    public CharSequence getSmileySpans(CharSequence text, int length) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        if (text.length() * EMOJI_INSTEAD_TEXT_COUNT <= length) {
            return addSmileySpans(text);
        }
        int realPos = 0;
        int curLen = 0;
        int lastCurPos = 0;
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int midLength = matcher.start() - lastCurPos;
            if (curLen + midLength >= length) {
                realPos = matcher.start() + length - curLen;
                curLen = length;
                break;
            }
            curLen += midLength;
            lastCurPos = matcher.end();
            curLen += EMOJI_INSTEAD_TEXT_COUNT;
            if (curLen > length) {
                realPos = matcher.start();
                curLen = length;
                break;
            } else if (curLen == length) {
                realPos = matcher.end();
                break;
            }
        }
        if (curLen < length) {
            realPos = lastCurPos + (length - curLen);
        }
        return addSmileySpans(text.subSequence(0, realPos));
    }

    /**
     * 获取指定长度的字符（一个表情算一个字）
     *
     * @param text   输入的字符
     * @param length 指定的长度
     * @return
     */
    public CharSequence getSmileySpans2(CharSequence text, int length) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        if (text.length() * EMOJI_INSTEAD_TEXT_COUNT <= length) {
            return addSmileySpans(text);
        }
        int start;
        int end;
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            if (length <= start) {
                break;
            }
            length += end -start-1;
        }
        return addSmileySpans(text.subSequence(0, length));
    }

    public boolean hasSmileySpans(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        Matcher matcher = mPattern.matcher(text);
        boolean hasMatcher = false;
        while (matcher.find()) {
            hasMatcher = true;
            break;
        }
        return hasMatcher;
    }

    /**
     * 获取字符长度（一个表情算4个字）
     *
     * @param text
     * @return
     */
    public int getSmileySpansLength(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return 0;
        }
        int length = text.length();
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int smileyLength = matcher.end() - matcher.start();
            if (smileyLength > EMOJI_INSTEAD_TEXT_COUNT) {
                // 一个表情算4个字符
                length -= (smileyLength - EMOJI_INSTEAD_TEXT_COUNT);
            } else if (smileyLength < EMOJI_INSTEAD_TEXT_COUNT) {
                length += (EMOJI_INSTEAD_TEXT_COUNT - smileyLength);
            }
        }
        return length;
    }

    /**
     * 获取字符长度（一个表情算1个字）
     *
     * @param text
     * @return
     */
    public int getSmileySpansLength2(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return 0;
        }
        int length = text.length();
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int smileyLength = matcher.end() - matcher.start();
            if (smileyLength > 0)
                length += (1 - smileyLength);
        }
        return length;
    }

    /**
     * 若字符串末尾是表情字符，则返回最后一个表情的起始位置，否则返回-1
     *
     * @param text
     * @return
     */
    public int getLastSmileyPosition(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return -1;
        }
        int length = text.length();
        int lastSmilyPosition = -1;
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if(end== length)
                lastSmilyPosition = start;
        }
        return lastSmilyPosition;
    }

    public CharSequence addSmileySpans(CharSequence text, String highLightTxt, int color) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        Matcher matcher = mPattern.matcher(text);
        while (matcher.find()) {
            int resId = mSmileyToRes.get(matcher.group());
            Drawable drawable = mContext.getResources().getDrawable(resId);
            float height = drawable.getIntrinsicHeight() * 0.5f;
            drawable.setBounds(0, 0, (int) height, (int) height);
            builder.setSpan(new ImageSpan(drawable), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (!TextUtils.isEmpty(highLightTxt)) {
            int start = text.toString().indexOf(highLightTxt);
            builder.setSpan(new ForegroundColorSpan(color), start, start + highLightTxt.length(),
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        return builder;
    }

    /**
     * 根据表情code码获取对应的图片资源ID
     * @return
     */
    public int getSmileyDrawableId(String text) {
        return mSmileyToRes.get(text);
    }

    /**
     * 获取所有表情code码
     * @return
     */
    public String[] getEncodedSmilyTextArray() {
        return mEncodedSmileyTexts;
    }

    private EmojiParser(Context context) {
        mContext = context;
        mSmileyTexts = mContext.getResources().getStringArray(R.array.emoji_smiley_texts);

        mEncodedSmileyTexts = new String[mSmileyTexts.length];

        final int emojiLength = mSmileyTexts.length;
        for (int i = 0; i < emojiLength; i++) {
            mEncodedSmileyTexts[i] = convertUnicode(mSmileyTexts[i]);
        }

        mSmileyToRes = buildSmileyToRes();
        mPattern = buildPattern();
        mSmileyToText = new HashMap<String, String>();
        String[] smileyTexts = mContext.getResources().getStringArray(R.array.emoji_texts);
        for (int i = 0; i < mEncodedSmileyTexts.length; i++) {
            mSmileyToText.put(mEncodedSmileyTexts[i], smileyTexts[i]);
        }
    }

    /**
     * Builds the hashtable we use for mapping the string version of a smiley (e.g. ":-)") to a resource ID for the icon
     * version.
     */
    private HashMap<String, Integer> buildSmileyToRes() {
        HashMap<String, Integer> smileyToRes = new HashMap<String, Integer>(mSmileyTexts.length);
        for (int i = 0; i < mSmileyTexts.length; i++) {
            smileyToRes.put(mEncodedSmileyTexts[i], getSmileyResources(mSmileyTexts[i]));
        }
        return smileyToRes;
    }

    /**
     * 根据表情code获取对应的图片资源ID
     * @param codeName
     * @return
     */
    private int getSmileyResources(String codeName) {
        int id = -1;
        String name = "emoji_" + codeName;
        Field field;
        try {
            field = R.drawable.class.getDeclaredField(name);
            if (field != null) {
                id = field.getInt(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * Builds the regular expression we use to find smileys in {@link #addSmileySpans}.
     */
    private Pattern buildPattern() {
        // Set the StringBuilder capacity with the assumption that the average
        // smiley is 3 characters long.
        StringBuilder patternString = new StringBuilder(mEncodedSmileyTexts.length * 3);
        // Build a regex that looks like (:-)|:-(|...), but escaping the smilies
        // properly so they will be interpreted literally by the regex matcher.
        patternString.append('(');
        for (String s : mEncodedSmileyTexts) {
            patternString.append(Pattern.quote(s));
            patternString.append('|');
        }
        // Replace the extra '|' with a ')'
        patternString.replace(patternString.length() - 1, patternString.length(), ")");
        return Pattern.compile(patternString.toString());
    }

    private static String convertUnicode(String emo) {
        emo = emo.substring(emo.indexOf("_") + 1);
        if (emo.length() < 6) {
            String s = new String(Character.toChars(Integer.parseInt(emo, 16)));
            return s;
        }
        String[] emos = emo.split("_");
        char[] char0 = Character.toChars(Integer.parseInt(emos[0], 16));
        char[] char1 = Character.toChars(Integer.parseInt(emos[1], 16));
        char[] emoji = new char[char0.length + char1.length];
        for (int i = 0; i < char0.length; i++) {
            emoji[i] = char0[i];
        }
        for (int i = char0.length; i < emoji.length; i++) {
            emoji[i] = char1[i - char0.length];
        }
        String s = new String(emoji);
        return s;
    }
}
