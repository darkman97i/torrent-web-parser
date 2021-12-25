package info.llort.torrent;

import info.llort.torrent.util.Console;
import info.llort.torrent.util.WebParser;
import org.apache.commons.cli.*;
import org.fusesource.jansi.AnsiConsole;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.fusesource.jansi.Ansi.Color.*;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		AnsiConsole.systemInstall();
		Console.setLogger(Main.class);

		Options options = new Options();
		options.addOption("u", "url", true, "Web URL");
		options.addOption("g", "geckoDriverPath", true, "Gecko driver path");
		options.addOption("f", "filters", true, "URL filter values separated by comma");
		options.addOption("h", "help", false, "Show help message");

		try {
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args);
			Console.setColorful(!cmd.hasOption("n"));

			if (cmd.hasOption("h")) {
				printHelp(options);
			} else if (!cmd.hasOption("f")) {
				Console.printlnLog("Filters are mandatory", RED);
				printHelp(options);
			}else {
				String urlWebToParse = Config.URL_WEB_TO_PARSE;
				String geckoDriverPath = Config.FIREFOX_DRIVER_PATH;
				List<String> filters = new ArrayList<>();
				if (cmd.hasOption("u")) {
					urlWebToParse = cmd.getOptionValue("u");
					Console.printlnLog("Overridden URL: " + urlWebToParse, YELLOW);
				}
				if (cmd.hasOption("u")) {
					geckoDriverPath = cmd.getOptionValue("g");
					Console.printlnLog("Overridden Gecko driver path: " + geckoDriverPath, YELLOW);
				}

				if (cmd.hasOption("f")) {
					String filtersValue =  cmd.getOptionValue("f");
					filters =  Arrays.asList(filtersValue.split(","));
				}

				// Creating the driver
				System.setProperty("webdriver.gecko.driver", geckoDriverPath);
				WebDriver driver = new FirefoxDriver();

				Set<String> mainPageLinks = WebParser.findMainPageLinks(urlWebToParse, geckoDriverPath, filters, driver);
				for (String link : mainPageLinks) {
					Console.println("Main page link: " + link, WHITE);
				}

				Set<String> torrentPageLinks = WebParser.findPageTorrentLinks(mainPageLinks, driver);
				for (String link : torrentPageLinks) {
					Console.println("Torrent page link: " + link, WHITE);
				}

				Set<String> downloadTorrentLinks = WebParser.downloadTorrentLinks(torrentPageLinks, driver);
				for (String link : downloadTorrentLinks) {
					Console.println("Download link: " + link, WHITE);
				}
			}
		} catch (ParseException e) {
			Console.println("Parse exception: " + e.getMessage(), RED);
			Console.println();
			Console.reset();
			printHelp(options);
		} catch (Exception e) {
			Console.println("Unexpected exception: " + e.getMessage(), RED);
			Console.reset();
		} finally {
			AnsiConsole.systemUninstall();
		}
	}

	/**
	 * Print help message
	 */
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("torrent-web-parser.jar <options>", options);
	}
}
