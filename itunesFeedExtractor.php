<?php
/**
 * itunesFeedExtractor
 *
 * This file will echo the original podcast URL as submitted to iTunes, given an iTunes
 * URL as the source.
 *
 * Ex: http://ax.phobos.apple.com.edgesuite.net/WebObjects/MZStore.woa/wa/viewPodcast?id=269238657
 *
 * @author		Michael Sitarzewski <msitarzewski@gmail.com>
 * @copyright	GNU General Public License
 * @version		1.3.1
 * @released	2011-05-11
 */

/*
	Usage: php itunesFeedExtractor.php http://itunes.apple.com/us/podcast/boulder-open-podcast/id346723827
	Returns: http://feeds.feedburner.com/BoulderOpenPodcast

	Use -v argument for more verbose output:
	Usage: php itunesFeedExtractor.php -v http://itunes.apple.com/us/podcast/boulder-open-podcast/id346723827
	Returns:
		URL Search
		Podcast ID: 346723827
		URL: http://feeds.feedburner.com/BoulderOpenPodcast
*/

// Variables
$url			=	'http://phobos.apple.com/WebObjects/MZStore.woa/wa/viewPodcast?id=169241193'; // the base URL
$feedURL		=	''; // the URL of the original feed
$castID			=	''; // Apple's id for the podcast
$flags			=	array(); // flags sent on the command line
$feed_return	=	''; // the final output

/**
* @return string
* @param string $url
* @desc Given a URL, parse the query string to find the 'id' of the podcast
*/
function getPodcastID($url = '', $flags)
{
	$querystring = '';
	$queryarray = array();
	
	// split the URL in two (hopefully. this should be reinforced)
	if(substr_count($url, '?'))
	{
		$querystring = explode('?', $url);
		if($flags['verbose'] === true) { echo "Query String Search\r\n"; }
		// set an array with the arguments from the query string
		parse_str($querystring[1], $queryarray);
		$id = $queryarray['id']; // hmm.
		if($flags['verbose'] === true) { echo "Podcast ID: {$id}\r\n"; }
	} else {
		if($flags['verbose'] === true) { echo "URL Search\r\n"; }
		$url_array = explode('/', $url);
		foreach($url_array as $key => $value)
		{
			if(substr($value, 0, 2) == 'id')
			{
				$id = substr($value, 2);
				if($flags['verbose'] === true) { echo "Podcast ID: {$id}\r\n"; }
			}
		}
	}
	return $id;
}

/**
 * @return string
 * @param int $castID
 * @param string $dest
 * @desc Given an id, retreive page contents from iTunes
 */
function getPageContents($castId, $page = 'http://itunes.apple.com/podcast/id')
{
}

/**
* @return string
* @param string $castId
* @desc Given a URL, go to the shell, curl some XML from Apple, and grab exactly one line from it that contains the word 'feedURL'
*/
function getFeedURL($castID = '', $flags)
{
	$input = '';
	$output = '';
	$urls = array(); // iTunes URLs to try.
	
	$urls = array('http://itunes.apple.com/podcast/id','http://itunes.apple.com/WebObjects/DZR.woa/wa/viewPodcast?id=');
	
	foreach($urls AS $id => $url)
	{
		$inputString = '';
		// set the input string. Pretend we're iTunes.
		$inputString = "curl -A 'iTunes/10.1 (Windows; U; Microsoft Windows XP Home Edition Service Pack 2 (Build 2600)) DPI/96' -s '{$url}{$castID}'";
		// execute the string - make the shell do the hard stuff
		$input = shell_exec($inputString);
		libxml_use_internal_errors(true); // disable errors
		$dom = new DOMDocument();
		$html = $dom->loadHTML($input);
		$dom->preserveWhiteSpace = false;
		$buttons = $dom->getElementsByTagName('button'); // find the button. Our URL is an element in a button tag.
		if(count($buttons))
		{
			$temp = '';
			foreach ($buttons as $button)   
			{
				if($temp = $button->getAttribute('feed-url')) { $output = $temp; break; } // grab the first match, exit.
			}
		} else {
			break;
		}
	}
	
	return $flags['verbose'] === true ? "URL: ".$output : $output;
}

if(defined('STDIN'))
{
	// if we're on the command line
	$flags['verbose'] = array_search('-v', $argv) ? true : false; // verbose?
	$url = $argv[count($argv)-1]; // set the base url
} else {
	// from the URL in a browser
	$flags['verbose'] = isset($_GET['verbose']) && $_GET['verbose'] == 1 ? true : false;
	$url = isset($_GET['url']) && $_GET['url'] != '' ? $_GET['url'] : false;
}

$protocol = substr($url, 5); // get the protocol
$protocols = array('itms:','feed:','itpc:'); // array of invalid protocols (curl won't get these)
if(in_array($protocol, $protocols)) { str_replace($protocol,"http:", $url); } // if it's not 'http:' fix it.

// "heavy lifting"
$castID = getPodcastID($url, $flags); // set the cast id
$feedURL = getFeedURL($castID, $flags); // get the feed URL

// return value
$feed_return = $flags['verbose'] === true ? $feedURL."\r\n" : $feedURL; // add returns
echo($feed_return); // display it.

?>
