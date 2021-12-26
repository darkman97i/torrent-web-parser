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
wget https://github.com/darkman97i/torrent-web-parser/releases/download/v.1.0/torrent-web-parser-1.0.zip
unzip torrent-web-parser-1.0.zip
java -jar torrent-web-parser -g /utils/torrent-web-parser-1.0/geckodriver -d /utils/torrent-web-parser-1.0 -f mar,navidad
```

## Additional information
* [Selenium webdriver information](https://www.javatpoint.com/selenium-webdriver)
* [Selenium firefox browser test](https://www.javatpoint.com/selenium-webdriver-running-test-on-firefox-browser-gecko-driver)
* [Gecko releases](https://github.com/mozilla/geckodriver/releases)
