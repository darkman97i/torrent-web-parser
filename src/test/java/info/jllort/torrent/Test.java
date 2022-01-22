package info.jllort.torrent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	public static void main(String[] args) {
		String value = "xxxxxx window.open(\"//atomtt.com/download/164730_-1640076623-Atrapada-en-la-Navidad--2021---BluRayRip-AC3-5-1/\");   xxxxxx";
		String regex = "window.open\\(\"(.*?)\"\\)";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(value);
		while (matcher.find()) {
			String value2 = matcher.group(1);
			System.out.println(value2);
		}
	}
}
