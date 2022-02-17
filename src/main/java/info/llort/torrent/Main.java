package info.llort.torrent;

import info.llort.torrent.util.Console;
import info.llort.torrent.util.PctmixWebParserV4;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.CaptureType;
import org.apache.commons.cli.*;
import org.fusesource.jansi.AnsiConsole;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.CapabilityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.Color.YELLOW;

public class Main {
	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		AnsiConsole.systemInstall();
		Console.setLogger(Main.class);

		Options options = new Options();
		options.addOption("u", "url", true, "Web URL");
		options.addOption("g", "geckoDriverPath", true, "Gecko driver path");
		options.addOption("f", "filters", true, "URL filter values separated by comma");
		options.addOption("t", "timeout", true, "Download file timeout");
		options.addOption("d", "dstPath", true, "Download File system path");
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
				long downloadTimeOut = Config.FILE_DOWNLOAD_TIMEOUT;
				String dstPath = Config.FILESYSTEM_DOWNLOAD_PATH;
				List<String> filters = new ArrayList<>();
				if (cmd.hasOption("u")) {
					urlWebToParse = cmd.getOptionValue("u");
					Console.printlnLog("Overridden URL: " + urlWebToParse, YELLOW);
				}
				if (cmd.hasOption("g")) {
					geckoDriverPath = cmd.getOptionValue("g");
					Console.printlnLog("Overridden Gecko driver path: " + geckoDriverPath, YELLOW);
				}

				if (cmd.hasOption("f")) {
					String filtersValue =  cmd.getOptionValue("f");
					filters =  Arrays.asList(filtersValue.split(","));
					Console.printlnLog("Filters: " + filtersValue, YELLOW);
				}

				if (cmd.hasOption("t")) {
					String timeoutValue =  cmd.getOptionValue("t");
					downloadTimeOut = Long.parseLong(timeoutValue);
					Console.printlnLog("Download file timeout: " + downloadTimeOut, YELLOW);
				}

				if (cmd.hasOption("d")) {
					dstPath =  cmd.getOptionValue("d");
					Console.printlnLog("Download file system path: " + dstPath, YELLOW);
				}

				// Creating proxy
				BrowserMobProxy proxy = new BrowserMobProxyServer();
				proxy.start(8080);
				Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

				String hostIp = Inet4Address.getLocalHost().getHostAddress();
				seleniumProxy.setHttpProxy(hostIp + ":" + proxy.getPort());
				seleniumProxy.setSslProxy(hostIp + ":" + proxy.getPort());
				proxy.enableHarCaptureTypes(CaptureType.REQUEST_CONTENT, CaptureType.RESPONSE_CONTENT);

				// Creating the driver
				System.setProperty("webdriver.gecko.driver", geckoDriverPath);
				// firefox profile to autosave
				FirefoxOptions firefoxOptions = new FirefoxOptions();
				FirefoxProfile fxProfile = new FirefoxProfile();
				fxProfile.setPreference("browser.download.folderList", 2);
				fxProfile.setPreference("browser.download.dir", dstPath);
				fxProfile.setPreference("browser.helperApps.neverAsk.saveToDisk","application/octet-stream");
				fxProfile.setPreference("pdfjs.enabledCache.state",false);
				firefoxOptions.setProfile(fxProfile);
				// Setting the proxy
				firefoxOptions.setCapability(CapabilityType.PROXY, seleniumProxy);
				firefoxOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

				WebDriver driver = new FirefoxDriver(firefoxOptions);

				PctmixWebParserV4.capture(urlWebToParse, geckoDriverPath, filters, downloadTimeOut, driver, proxy);

				// closing the driver
				driver.close();

				// closing proxy
				proxy.abort();
			}
		} catch (ParseException e) {
			Console.println("Parse exception: " + e.getMessage(), RED);
			e.printStackTrace();
			Console.println();
			Console.reset();
			printHelp(options);
		} catch (Exception e) {
			Console.println("Unexpected exception: " + e.getMessage(), RED);
			e.printStackTrace();
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
