/*
 * The MIT License
 *
 * Copyright (c) 2013 The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.igv.utils;

import javafx.scene.paint.Color;
import org.igv.tribble.util.RemoteURLHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * @author jrobinso
 */
public class ColorUtils {

    public static Map<Object, Color> colorCache = new WeakHashMap<Object, Color>(100);

    // HTML 4.1 color table,  + orange and magenta
    static Map<String, String> colorSymbols = new HashMap();


    static {
        colorSymbols.put("white", "FFFFFF");
        colorSymbols.put("silver", "C0C0C0");
        colorSymbols.put("gray", "808080");
        colorSymbols.put("black", "000000");
        colorSymbols.put("red", "FF0000");
        colorSymbols.put("maroon", "800000");
        colorSymbols.put("yellow", "FFFF00");
        colorSymbols.put("olive", "808000");
        colorSymbols.put("lime", "00FF00");
        colorSymbols.put("green", "008000");
        colorSymbols.put("aqua", "00FFFF");
        colorSymbols.put("teal", "008080");
        colorSymbols.put("blue", "0000FF");
        colorSymbols.put("navy", "000080");
        colorSymbols.put("fuchsia", "FF00FF");
        colorSymbols.put("purple", "800080");
        colorSymbols.put("orange", "FFA500");
        colorSymbols.put("magenta", "FF00FF");
    }

    /**
     * Convert an rgb string, hex, or symbol to a color.
     *
     * @param string
     * @return
     */
    public static Color parseColor(String string) {
        try {
            Color c = colorCache.get(string);
            if (c == null) {
                if (string.contains(",")) {
                    String[] rgb = string.split(",");
                    int red = Integer.parseInt(rgb[0]);
                    int green = Integer.parseInt(rgb[1]);
                    int blue = Integer.parseInt(rgb[2]);
                    c =  Color.rgb(red, green, blue);
                } else if (string.startsWith("#")) {
                    c = hexToColor(string.substring(1));
                } else {
                    String hexString = colorSymbols.get(string.toLowerCase());
                    if (hexString != null) {
                        c = hexToColor(hexString);
                    }
                }

                if (c == null) {
                    c = Color.BLACK;
                }
                colorCache.put(string, c);
            }
            return c;

        } catch (NumberFormatException numberFormatException) {
            //TODO Throw this exception?
            return Color.BLACK;
        }
    }


    private static Color hexToColor(String string) {
        if (string.length() == 6) {
            int red = Integer.parseInt(string.substring(0, 2), 16);
            int green = Integer.parseInt(string.substring(2, 4), 16);
            int blue = Integer.parseInt(string.substring(4, 6), 16);
            return Color.rgb(red, green, blue);
        } else {
            return null;
        }

    }


}
