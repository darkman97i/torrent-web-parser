package info.llort.torrent.util;

import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * http://www.doublecloud.org/2013/10/writing-colorful-console-output-in-java/
 */
public class Console {
	private static Logger log = LoggerFactory.getLogger(Console.class);
	private static boolean colorful = true;

	/**
	 * Set logger
	 */
	public static void setLogger(Class clazz) {
		log = LoggerFactory.getLogger(clazz);
	}

	/**
	 * Set color mode
	 */
	public static void setColorful(boolean colorful) {
		Console.colorful = colorful;
	}

	/**
	 * Read from console
	 */
	public static String read(String def) {
		Scanner scanIn = new Scanner(System.in);
		String val = scanIn.nextLine();

		// http://stackoverflow.com/questions/15423519/issue-with-scanners-and-java-util-nosuchelementexception-no-line-found-at-jav
		// scanIn.close();

		if (val.isEmpty()) {
			return def;
		} else {
			return val;
		}
	}

	/**
	 * Print to console and log
	 */
	public static void print(String str, Ansi.Color color) {
		printImpl(str, color);
	}

	/**
	 * Print to console
	 */
	public static void println() {
		System.out.println();
	}

	/**
	 * Print to console
	 */
	public static void println(String str, Ansi.Color color) {
		printImpl(str, color);
		println();
	}

	/**
	 * Print to console and log
	 */
	public static void printLog(String str, Ansi.Color color) {
		printImpl(str, color);
		logImpl(str);
	}

	/**
	 * Print to console and log
	 */
	public static void printlnLog(String str, Ansi.Color color) {
		printImpl(str, color);
		println();
		logImpl(str);
	}

	/**
	 * Flush output
	 */
	public static void flush() {
		System.out.flush();
	}

	/**
	 * Reset console
	 */
	public static void reset() {
		Ansi.ansi().reset();
	}

	/**
	 * Private
	 */
	private static void printImpl(String str, Ansi.Color color) {
		if (colorful) {
			// Only for linux
			System.out.print(Ansi.ansi().bold().fgBright(color).a(str).reset());
		} else {
			System.out.print(Ansi.ansi().bold().a(str).reset());
		}
	}

	/**
	 * Private
	 */
	private static void logImpl(String str) {
		if (str.startsWith("- ")) {
			str = str.substring(2);
		}

		log.info(str);
	}
}
