package org.judovana.linux;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/*
 Based on https://gist.github.com/shmert/3859200 with follwoing copiright.
 However modified to much.
 Intetnionaly one static class.

 Copyright (c) 2011 Aravind Rao

 Modifications by Sam Barnum, 360Works 2012

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons
 * to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
public final class ConsoleImageViewer {

    private static void printHelp() {
        System.out.println("Utility to render  images to console");
        System.out.println("to work best use 'export COLUMNS LINES' otherwise standard terminal of 80x25 will be used ");
        System.out.println("-h --help - print help and exits");
        System.out.println("Necessary parameter is any <FILE-NAME> follwoing optional switches:");
        System.out.println("-best - overwrite everything and set best settngs");
        System.out.println("-bestnchar - same but sets nochar to true");
        System.out.println("-fg - use linux ansi escape color for character");
        System.out.println("-bg - use linux ansi escape color for background");
        System.out.println("-nochar - disable shadowing by characters");
        System.out.println("-ratio - forece to keep image ratio");
        System.out.println("-rect - will force rendering assuming that console char is not square, but rectangle of 2w~=3h");
        System.out.println("-negChar - invert chars");
        System.out.println("-negColor - invert colors");
        System.out.println("-wX- force width");
        System.out.println("-hX- force height");
    }

    private static String getFile(String[] args) {
        for (String arg : args) {
            if (!arg.startsWith("-")) {
                return arg;
            }
        }
        throw new RuntimeException("No file found!");
    }

    private static boolean haveParam(String param, String[] args) {
        return haveParam(param, args, false);

    }

    private static boolean haveParam(String param, String[] args, boolean defaults) {
        for (String arg : args) {
            if (arg.startsWith("-")) {
                String s = arg.replaceAll("-", "");
                if (s.equalsIgnoreCase(param)) {
                    return true;
                }
            }
        }
        return defaults;
    }

    private static int getParam(String w, String[] args, int i) {
        for (String arg : args) {
            if (arg.startsWith("-")) {
                String s = arg.replaceAll("-", "");
                if (s.startsWith(w)) {
                    return new Integer(s.substring(1));
                }
            }
        }
        return i;
    }

    boolean negative;

    public static String convert(final BufferedImage image, boolean fg, boolean bg, boolean drawChar, boolean negChar, boolean negColor) {
        int imgSize = (image.getWidth() + 1) * image.getHeight();
        int colorSize = 0;
        if (fg) {
            colorSize += 18;
        }
        if (bg) {
            colorSize += 18;
        }
        StringBuilder sb = new StringBuilder(imgSize + imgSize * colorSize);
        for (int y = 0; y < image.getHeight(); y++) {
            if (fg || bg) {
                sb.append(ansiColorToEscapedString(15, true));
                sb.append(ansiColorToEscapedString(0, false));
            }
            if (sb.length() != 0) {
                sb.append("\n");
            }
            for (int x = 0; x < image.getWidth(); x++) {
                Color pixelColor = new Color(image.getRGB(x, y));
                if (fg || bg) {
                    int ansi = colorToAnsiColor(pixelColor, negColor);
                    if (fg) {
                        sb.append(ansiColorToEscapedString(ansi, true));
                    }
                    if (bg) {
                        sb.append(ansiColorToEscapedString(ansi, false));
                    }
                }
                if (drawChar) {
                    double gValue = (double) pixelColor.getRed() * 0.2989 + (double) pixelColor.getBlue() * 0.5870 + (double) pixelColor.getGreen() * 0.1140;
                    final char s = negChar ? returnStrNeg(gValue) : returnStrPos(gValue);
                    sb.append(s);
                } else {
                    sb.append(" "); //nocahr, just color
                }
                //best is with char, and bg only
            }
        }
        return sb.toString();
    }

    private static String ansiColorToEscapedString(int ansi, boolean fg) {
        if (fg) {
            return "\033[38;5;" + ansi + "m"; //fg
        } else {
            return "\033[48;5;" + ansi + "m"; //bg
        }

    }

    /**
     * r=3; g=0; b=2; let number=16+36*r+6*g+b; COLOR='\033[38;5;'$number'm' ;
     * NC='\033[0m' ; echo -e "I ${COLOR}love${NC} Stack"
     * http://stackoverflow.com/questions/15682537/ansi-color-specific-rgb-sequence-bash
     *
     * @param pixelColor
     * @return
     */
    private static int colorToAnsiColor(Color pixelColor, boolean negative) {
        int n = 0;
        if (negative) {
            n = 255;
        }
        int ansiR = Math.abs(n - pixelColor.getRed()) / 50;
        int ansiG = Math.abs(n - pixelColor.getGreen()) / 50;
        int ansiB = Math.abs(n - pixelColor.getBlue()) / 50;
        int ansi = 16 + 36 * ansiR + 6 * ansiG + ansiB;
        return ansi;
    }

    /**
     * Create a new string and assign to it a string based on the grayscale
     * value. If the grayscale value is very high, the pixel is very bright and
     * assign characters such as . and , that do not appear very dark. If the
     * grayscale value is very lowm the pixel is very dark, assign characters
     * such as # and @ which appear very dark.
     *
     * @param g grayscale
     * @return char
     */
    private static char returnStrPos(double g)//takes the grayscale value as parameter
    {
        final char str;

        if (g >= 230.0) {
            str = ' ';
        } else if (g >= 200.0) {
            str = '.';
        } else if (g >= 180.0) {
            str = '*';
        } else if (g >= 160.0) {
            str = ':';
        } else if (g >= 130.0) {
            str = 'o';
        } else if (g >= 100.0) {
            str = '&';
        } else if (g >= 70.0) {
            str = '8';
        } else if (g >= 50.0) {
            str = '#';
        } else {
            str = '@';
        }
        return str; // return the character

    }

    /**
     * Same method as above, except it reverses the darkness of the pixel. A
     * dark pixel is given a light character and vice versa.
     *
     * @param g grayscale
     * @return char
     */
    private static char returnStrNeg(double g) {
        final char str;

        if (g >= 230.0) {
            str = '@';
        } else if (g >= 200.0) {
            str = '#';
        } else if (g >= 180.0) {
            str = '8';
        } else if (g >= 160.0) {
            str = '&';
        } else if (g >= 130.0) {
            str = 'o';
        } else if (g >= 100.0) {
            str = ':';
        } else if (g >= 70.0) {
            str = '*';
        } else if (g >= 50.0) {
            str = '.';
        } else {
            str = ' ';
        }
        return str;

    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH, boolean ratio) {
        if (ratio) {
            double wR = (double) newW / (double) img.getWidth();
            double hR = (double) newH / (double) img.getHeight();
            if (wR > hR) {
                newW = (int) (hR * (double) img.getWidth());
            } else {
                newH = (int) (wR * (double) img.getHeight());
            }
        }
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            tui(args);
        } else {
            printHelp();
        }
    }

    public static void tui(String[] args) throws IOException {
        if (haveParam("help", args) || haveParam("h", args)) {
            printHelp();
            System.exit(0);
        }
        boolean ratio = haveParam("ratio", args);
        boolean rect = haveParam("rect", args);
        boolean bg = haveParam("bg", args);
        boolean fg = haveParam("fg", args);
        boolean noChar = haveParam("nochar", args);
        boolean negChar = haveParam("negChar", args);
        boolean negCol = haveParam("negColor", args);
        if (haveParam("best", args) || haveParam("bestnchar", args)) {
            ratio = true;
            rect = true;
            bg = true;
            fg = false;
            if (haveParam("best", args)) {
                noChar = false;
            }
            if (haveParam("bestnchar", args)) {
                noChar = true;
            }
            negChar = true;
            negCol = false;
        }
        File f = new File(getFile(args));
        BufferedImage image = ImageIO.read(f);
        if (image == null) {
            throw new IllegalArgumentException(f + " is not a valid image.");
        }
//use exported! export COLUMNS LINES to find size
//force ratio? think about char in consoel is not square, but rectangle 2w=h
        String systemWidth = System.getenv("COLUMNS");
        String systemHeight = System.getenv("LINES");
        int w = getParam("w", args, 80);
        int h = getParam("h", args, 25);
        if (systemWidth != null && w == 80) {
            w = Integer.valueOf(systemWidth);
        } else {
            if (w == 80) {
                System.err.println("Warning COLUMNS varibale not exported nor -w specified! Standart " + w + " used");
            }
        }
        if (systemHeight != null && h == 25) {
            h = Integer.valueOf(systemHeight);
        } else {
            if (h == 25) {
                System.err.println("Warning LINES varibale not exported nor -h specified! Standart " + h + " used");
            }
        }
        String ascii = doAll(rect, image, w, h, ratio, fg, bg, noChar, negChar, negCol);
        System.out.print(ascii);
        System.out.println();

    }

    private static String doAll(boolean rect, BufferedImage image, int w, int h, boolean ratio, boolean fg, boolean bg, boolean noChar, boolean negChar, boolean negCol) {
        if (rect) {
//HxW of terminal char
//78x56
//39*28
            double rectMod = 56d / 78d;
            //surprisingly still bad
            rectMod = 56d / (1.7d * 78d);
            int nh = (int) ((double) image.getHeight() * rectMod);
            image = resize(image, image.getWidth(), nh, false);

        }
        image = resize(image, w, h, ratio);
        final String ascii = convert(image, fg, bg, !noChar, negChar, negCol);
        return ascii;
    }

}
