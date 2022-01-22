# Torrent web parser
Utility to get torrent download links from the web without advertisements:
* https://atomixhq.net/

## Prerequisites
* Install Java jdk 1.8

## Installation
* [Download the latest release](https://github.com/darkman97i/torrent-web-parser/releases/)

## Usage
```
mkdir /utils
cd /utils
wget https://github.com/darkman97i/torrent-web-parser/releases/download/v.1.2/torrent-web-parser-1.2.zip
unzip torrent-web-parser-1.2.zip
java -jar torrent-web-parser -g /utils/torrent-web-parser-1.2/geckodriver -d /utils/torrent-web-parser-1.2 -f mar,navidad
```

### Parameters
```
 -d,--dstPath <arg>           Download File system path
 -f,--filters <arg>           URL filter values separated by comma
 -g,--geckoDriverPath <arg>   Gecko driver path
 -h,--help                    Show help message
 -t,--timeout <arg>           Download file timeout
 -u,--url <arg>               Web URL
```

## Additional information
* [Selenium webdriver information](https://www.javatpoint.com/selenium-webdriver)
* [Selenium firefox browser test](https://www.javatpoint.com/selenium-webdriver-running-test-on-firefox-browser-gecko-driver)
* [Gecko releases](https://github.com/mozilla/geckodriver/releases)
* [Proxy sample with selenium](https://www.swtestacademy.com/browsermob-proxy-selenium-java/)
  * [browsermob-proxy](https://github.com/lightbody/browsermob-proxy)
  * [Modify request header in selenium](https://sqa.stackexchange.com/questions/37227/how-to-modify-http-request-header-in-selenium-webdriver-with-java)
