package info.jllort.torrent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest2 {
	private static final String bodySample = "function openTorrent(u)\n" +
			"}\t\n" +
			"\n" +
			"\n" +
			"var tid = parseInt(\"167632\");\n" +
			"\n" +
			"var btn = document.getElementById(\"btntor\");\n" +
			"btn.addEventListener(\"click\", function() ";

	public static void main(String[] args) {
		String linkRegex = "(parseInt\\(\"(.*)\"\\);)";
		Pattern pattern = Pattern.compile(linkRegex);
		Matcher matcher = pattern.matcher(bodySample);
		if (matcher.find()) {
			String value = matcher.group(2);
			System.out.println(value);
		}
	}
}
