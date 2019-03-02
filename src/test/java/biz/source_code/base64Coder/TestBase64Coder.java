/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2007-2015 Broad Institute
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
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package biz.source_code.base64Coder;

// Tests for the Base64Coder class.

import org.junit.Test;

import static org.junit.Assert.fail;

public class TestBase64Coder {

    // Test Base64Coder with constant strings.
    @Test
    public void test1() {
        check("Aladdin:open sesame", "QWxhZGRpbjpvcGVuIHNlc2FtZQ==");  // example from RFC 2617
        check("", "");
        check("1", "MQ==");
        check("22", "MjI=");
        check("333", "MzMz");
        check("4444", "NDQ0NA==");
        check("55555", "NTU1NTU=");
        check("abc:def", "YWJjOmRlZg==");
    }

    private static void check(String plainText, String base64Text) {
        String s1 = Base64Coder.encodeString(plainText);
        String s2 = Base64Coder.decodeString(base64Text);
        if (!s1.equals(base64Text) || !s2.equals(plainText))
            fail("Check failed for \"" + plainText + "\" / \"" + base64Text + "\".");
    }


// THE TESTS BELOW ARE COMMENTED OUT BECAUSE NOT ALL PLATFORMS HAVE THE SUN PACKAGE.

// Test Base64Coder against sun.misc.BASE64Encoder/Decoder with random data.
// Line length below 76.
//    @Test
//    public void test2() throws Exception {
//        final int maxLineLen = 76 - 1;                          // the Sun encoder adds a CR/LF when a line is longer
//        final int maxDataBlockLen = (maxLineLen * 3) / 4;
//        sun.misc.BASE64Encoder sunEncoder = new sun.misc.BASE64Encoder();
//        sun.misc.BASE64Decoder sunDecoder = new sun.misc.BASE64Decoder();
//        Random rnd = new Random(0x538afb92);
//        for (int i = 0; i < 50000; i++) {
//            int len = rnd.nextInt(maxDataBlockLen + 1);
//            byte[] b0 = new byte[len];
//            rnd.nextBytes(b0);
//            String e1 = new String(Base64Coder.encode(b0));
//            String e2 = sunEncoder.encode(b0);
//            assertEquals(e2, e1);
//            byte[] b1 = Base64Coder.decode(e1);
//            byte[] b2 = sunDecoder.decodeBuffer(e2);
//            assertArrayEquals(b0, b1);
//            assertArrayEquals(b0, b2);
//        }
//    }
//
//    // Test Base64Coder line encoding/decoding against sun.misc.BASE64Encoder/Decoder
//// with random data.
//    @Test
//    public void test3() throws Exception {
//        final int maxDataBlockLen = 512;
//        sun.misc.BASE64Encoder sunEncoder = new sun.misc.BASE64Encoder();
//        sun.misc.BASE64Decoder sunDecoder = new sun.misc.BASE64Decoder();
//        Random rnd = new Random(0x39ac7d6e);
//        for (int i = 0; i < 10000; i++) {
//            int len = rnd.nextInt(maxDataBlockLen + 1);
//            byte[] b0 = new byte[len];
//            rnd.nextBytes(b0);
//            String e1 = new String(Base64Coder.encodeLines(b0));
//            String e2 = sunEncoder.encodeBuffer(b0);
//            assertEquals(e2, e1);
//            byte[] b1 = Base64Coder.decodeLines(e1);
//            byte[] b2 = sunDecoder.decodeBuffer(e2);
//            assertArrayEquals(b0, b1);
//            assertArrayEquals(b0, b2);
//        }
//    }
//
//
//   @Test
//    public void test4() throws Exception {
//        final int maxDataBlockLen = 512;
//        sun.misc.BASE64Encoder sunEncoder = new sun.misc.BASE64Encoder();
//        sun.misc.BASE64Decoder sunDecoder = new sun.misc.BASE64Decoder();
//        Random rnd = new Random(0x39ac7d6e);
//        for (int i = 0; i < 10000; i++) {
//            int len = rnd.nextInt(maxDataBlockLen + 1);
//            byte[] b0 = new byte[len];
//            rnd.nextBytes(b0);
//            String e1 = (new String(Base64Coder.encodeLines(b0))).trim();
//            String e2 = sunEncoder.encode(b0).trim();
//            assertEquals(e2, e1);
//            byte[] b1 = Base64Coder.decodeLines(e1);
//            byte[] b2 = sunDecoder.decodeBuffer(e2);
//            assertArrayEquals(b0, b1);
//            assertArrayEquals(b0, b2);
//        }
//    }

} // end class TestBase64Coder
