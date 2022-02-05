package info.jllort.torrent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {
	private static final String bodySample = "function openTorrent(u)\n" +
			"{\n" +
			"\n" +
			"\t \n" +
			"\n" +
			"\tvar link = \"https://www.linkonclick.com/jump/next.php?r=5302219\";\n" +
			"\n" +
			"\twindow.open(link);\n" +
			"\n" +
			"\t\n" +
			"\twindow.location.href = u;\t\n" +
			"\n" +
			"}\t\n" +
			"\n" +
			"\n" +
			"var tid = parseInt(\"167641\");\n" +
			"\n" +
			"var btn = document.getElementById(\"btntor\");\n" +
			"btn.addEventListener(\"click\", function() ";

	public static void main(String[] args) {
		String linkRegex = "([\"]https://www.linkonclick.com/jump/next.*[\"])";
		Pattern pattern = Pattern.compile(linkRegex);
		Matcher matcher = pattern.matcher(bodySample);
		if (matcher.find()) {
			System.out.println(matcher.group().replaceAll("\"",""));
		}
	}
}
