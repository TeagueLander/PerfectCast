<?php
 
/*
 * Podcast URL Extractor
 * implemented at http://itunes.so-nik.com
 *
 * All code has been re-written by lasavior. 
 * Original code & inspiration from Michael Sitarzewski, zerologic.com
 *
 * Ex: http://ax.phobos.apple.com.edgesuite.net/WebObjects/MZStore.woa/wa/viewPodcast?id=269238657
 * Ex: http://itunes.apple.com/us/podcast/hombre-potato/id319810190?uo=4
 *
 *   Usage: itunes.php?http://itunes.apple.com/us/podcast/the-morning-stream/id414564832?uo=4
 *   Returns: http://myextralife.com/ftp/radio/morningstream.xml
 
CHANGE LOG:::
EDIT 5/10/11: Removed flat-file database storing. Reformatted for SQLite (requires PHP 5 and above). Script will create database & table if they dont exist.
 
EDIT 6/2/11: Added safety catch for iTunes-U URL's. (Currently Apple does not offer a way to subscribe to these outside of iTunes [as far as i've seen]). Also touched up a little bit of code here & there.
 
EDIT 6/21/11: Added another safety catch for iTunes-U pages. Now instead of just checking the URL, it will also check the page content (as there are links that do not include the tag 'itunes-u' that was previously checked for).
 
EDIT 6/28/11: Previous iTunes-U update was flawed and resulted in a lot of false-positives. Fixed in new update.
 
EDIT 8/29/11: Added catch for material not available in the US store.
 
EDIT 9/3/11: Removed POST option, changed to GET only. (if you wish to re-enable it, set $url = $_REQUEST['terms'];)
 
EDIT 10/16/12: Changed the checkFEED function to look only for "http" instead of "http://" as it would fail on https:// links.
EDIT 10/16/12: Script now checks a URL twice before determining that it doesn't work. Sometimes iTunes just doesn't want to cooperate the first time around.
 
EDIT 10/23/12: Lots of little changes. Control structure changes (i used alt syntax improperly), plus adding in new error finding.
 
EIDT 10/25/12: Minor fixes and restructuring to make the code flow better. Also fixed cache displaying error.
 */
 
 
//-----------------------------------------
// a couple of settings
$_SETT = array(
	'hashalgo' => 'md5',
	'contactemail' => 'admin@example.com',
	'dbtable' => 'feedurls',
	'dbfilename' => 'itnfedb.sqdb'
	);
 
//-----------------------------------------
// the base URL
$url = ($_GET['terms'] != null) ? urldecode(urldecode($_GET['terms'])) : urldecode(urldecode($_SERVER['QUERY_STRING']));
$urlhash = hash($_SETT['hashalgo'], $url);
 
//-----------------------------------------
//Functions
function curlfeed($churl) {
 
	$ch = curl_init();
	curl_setopt_array ($ch, array(
		CURLOPT_RETURNTRANSFER => TRUE,
		CURLOPT_FOLLOWLOCATION => TRUE,
		CURLOPT_MAXREDIRS => 10,
		CURLOPT_CONNECTTIMEOUT => 5,
		CURLOPT_URL => $churl,
		CURLOPT_USERAGENT => 'iTunes/9.1.1'
		));
	$chresult = curl_exec($ch);
	curl_close($ch);
 
	return $chresult;
	} //END function: curlfeed
 
function subURL($urltosub, $string1 = 'feed-url="', $string2 ='"') {
 
	$str1 = strpos($urltosub, $string1) + strlen($string1);
	$str2 = strpos($urltosub, $string2, $str1);
	$subbedstring = substr($urltosub, $str1, ($str2 - $str1));
 
	return $subbedstring;
	} //END function: subURL
 
function checkFEED($thefeed, $mainURL, $isCached = NULL) {
 
	global $feedURL, $invalidURL, $_SETT;
 
	//For your reference, the following is called a HEREDOC
	$badURL = <<<BADURL
    <div style="font-size:15px;white-space:normal;max-width:400px">iTunes failed to return the feed-url. 
    <br>
    <br>This may be due to different reasons: 
    <br>&nbsp;&nbsp;1) the podcast has been deleted,
    <br>&nbsp;&nbsp;2) the podcast has no items,
    <br>&nbsp;&nbsp;3) the podcast may be outside the USA,
    <br>and many other reasons im not aware of. Unfortunately iTunes isnt perfect (hence why your here).
    <br>
    <br>Im sorry my script was of no help to you. If you feel this was in error, you can <a href="outfeed.php?$mainURL" style="text-decoration:underline" target="_blank">view the source,</a><br> or you can always <a href="mailto:{$_SETT['contactemail']}?subject=IFE Error&body=URL i tried: $mainURL" style="display:inline;text-decoration:underline">email me</a> the URL and i will see if its fixable.</div>
BADURL;
 
	if (substr($thefeed, 0, 4) != "http") {
 
		if (is_null($isCached)) {
 
			$feedURL = $badURL;
			} //END if: isCached
		$invalidURL = "The cake is a lie.";
		} //END if: http
 
	} //END function: checkFEED
 
//-----------------------------------------
//Here we initiate the sqlite database and setup the cached variables
if ($database = sqlite_open($_SETT['dbfilename'], 0666, $sqlerror)) {
 
	$check_cache_query = "SELECT url FROM {$_SETT['dbtable']} WHERE uniqueid='$urlhash'";
	$cache_file = @sqlite_single_query($database, $check_cache_query, true);
	if(sqlite_last_error($database)) {
 
		$create_table_query = "CREATE TABLE {$_SETT['dbtable']}(uniqueid TEXT, url TEXT, date TEXT)";
		@sqlite_exec($database, $create_table_query);
		$cache_file = NULL;
		} //END if: sqlite last error
	} //END if: sqlite open
else {
 
	$failed_to_initialize_sqlite = $sqlerror;
	} //END if: sqlite open
 
//-----------------------------------------
//For caching files, this determines if the cached file already exists
if ($cache_file == NULL || isset($failed_to_initialize_sqlite)) {
 
	//-----------------------------------------
	//Here we identify the podcast url scheme
	$idstyles = array(
		'?id=',
		'&id=',
		'/id'
		);
 
	for ($counter = 0; $counter < count($idstyles); $counter++) {
 
		if (strpos($url, $idstyles[$counter])) {
 
			$idstyle = $idstyles[$counter];
			$validID = "So, how are you holding up? Because I'm a potato!";
			break;
			} //END if: idstyles
		} //END for: counter
 
	//-----------------------------------------
	//Since iTunes-U uses the same identifier symbols,
	//this is where we rule them out until it is supported
	//Note: more checking for itunes-u content is done farther below
	if (strpos($url, '/itunes-u/')) {
 
		unset($validID);
		$invalidID = "itunes-u";
		} //END if: itunes-u
 
	//-----------------------------------------
	// extract feed-id, get page from itunes & find feed-url
	if (isset($validID)) {
 
		for ($loopcount = 1; $loopcount < 3; $loopcount++) {
 
			preg_match("/[0-9]+/", $url, $podid, 0, strpos($url, $idstyle)); // here we extract the feed ID
			$curled1 = curlfeed("http://itunes.apple.com/podcast/id".$podid[0]);
			if (strpos($curled1, "<key>kind</key><string>Goto</string>") > 1) {
 
				$newURL = subURL($curled1, "<key>url</key><string>", "</string>");
				$curled1 = curlfeed($newURL);
				} //END if:goto
 
			if (strpos($curled1, 'not currently available in the U.S. store')) {
 
				$feedURL = <<<ITUNESFOREIGNSTORE
        <div style="font-size:15px;white-space:normal;max-width:400px">This item is not available in the US store.
        <br>
        <br>(This may also be a problem with the podcast not existing in the iTunes store at all. Unfortunately iTunes returns the same error for both so they are indistinguishable from each other.) Apple currently restricts access to some content based on geographic locations. As I reside in the United States, I cannot retrieve your podcast link.
        <br>
        <br>If you have any PHP knowledge you can try translating the public source code to retrieve the URL on your own: 
        <br>http://snipplr.com/view/52465</div>
ITUNESFOREIGNSTORE;
				$invalidID = 'itunes_foreign';
				} //END if: us store
			elseif (strpos($curled1, '<span class="track-count">0 Items</span>')) {
 
				$emptyPodcastText = <<<EMPTYPODCAST
        <div style="font-size:15px;white-space:normal;max-width:400px"><b>Empty podcast.</b>
        <br>
        <br>Your podcast contains no episodes at the time this script last checked. Unfortunately Apple does not provide the feed URL for empty podcasts. Please check back in as little as 12 hours or once the podcast has active items.</div>
EMPTYPODCAST;
				$feedURL = $emptyPodcastText . "\n" . (time() + 60 * 60 * 12);
				$invalidID = 'empty_podcast';
				} //END if: 0 items
			elseif (strpos($curled1, ' <key>message</key><string>Your request is temporarily unable to be processed.</string>') || strlen($curled1) < 20) {
 
				if ($loopcount == 2) {
 
					$feedURL = <<<CANTPROCESS
        <div style="font-size:15px;white-space:normal;max-width:400px">Temporarily unable to process.
        <br>
        <br>iTunes returned the following error: "Your request is temporarily unable to be processed." Please try again later.</div>
CANTPROCESS;
					$invalidID = $doNotCacheResults = TRUE;
					} //END if: loopcount
				} //END if: unable to process
			elseif (strpos($curled1, 'iTunes U')) { //Leave this as the last elseif as it may catch non-iTunesU material
 
				$itunesU_title = subURL($curled1, '<title>', '</title>');
				$itunesU_title_char = urlencode($itunesU_title);
				$itunesU_crumbs = subURL($curled1, 'start breadcrumbs', 'end breadcrumbs');
				$itunesU_li = subURL($itunesU_crumbs, '<li>', '</li>');
				if (strpos($itunesU_li, 'iTunes U')) {
 
					$feedURL = <<<ITUNESU
        <div style="font-size:15px;white-space:normal;max-width:400px">iTunes-U links not supported.
        <br>
        <br>Currently Apple does not offer a way to subscribe to iTunes-U material outside of iTunes (that i can find). A temporary solution is to search for a similar title as a podcast in hopes that the content providers also posted it to the iTunes Podcast Directory (do no expect this for password protected content). Try searching for: 
        <br>"$itunesU_title"
        <br>
        <br>You can <a href='http://itunes.so-nik.com/index.php?terms=$itunesU_title_char' style="display:inline;text-decoration:underline;color:blue">click here</a> to try that now.</div>
ITUNESU;
					$invalidID = 'itunes-u';
					} //END if: itunesu_li
				} //END if: iTunes U
			if (!isset($invalidID)) {
 
				$feedURL = subURL($curled1);
				checkFEED($feedURL, $url);
				if (isset($invalidURL) && $loopcount == 1) {
 
					unset($invalidURL, $podid);
					sleep(2);
					} // END if: loopcount
				else {
 
					break;
					} //END if: loopcount
				} //END if: invalidID
			else {
 
				break;
				} //END if: invalidID
			} //END for: loopcount
 
		if (!isset($failed_to_initialize_sqlite) && !isset($doNotCacheResults)) {
 
			$newfeedURL = sqlite_escape_string($feedURL);
			$cache_query_put_content = "INSERT INTO {$_SETT['dbtable']} (uniqueid,url,date) VALUES ('$urlhash', '$newfeedURL', '".date("r")."'";
			@sqlite_exec($database, $cache_query_put_content);
			} //END if: failed sqlite
		} //END if: validID
	else {
 
		if ($invalidID == "itunes-u") {
 
			//Example URL: http://itunes.apple.com/itunes-u/the-civil-war-reconstruction/id341650730
			$itu_label = subURL($url, '/itunes-u/', '/');
			$itu_label_white = trim(ucwords(str_replace('-', ' ', $itu_label)));
			$itu_label_char = str_replace('-', '%20', $itu_label);
			$feedURL = <<<FEEDURL
        <div style="font-size:15px;white-space:normal;max-width:400px">iTunes-U links not supported.
        <br>
        <br>Currently Apple does not offer a way to subscribe to iTunes-U material outside of iTunes (that i can find). A temporary solution is to search for a similar title as a podcast in hopes that the content providers also posted it to the iTunes Podcast Directory (do no expect this for password protected content). Try searching for: 
        <br>"$itu_label_white"
        <br>
        <br>You can <a href='http://itunes.so-nik.com/index.php?terms=$itu_label_char' style="display:inline;text-decoration:underline;color:blue">click here</a> to try that now.</div>
FEEDURL;
			} //END if: itunes-u
		else {
 
			$feedURL = "URL not supported. <br><br>Please contact <a href='mailto:{$_SETT['contactemail']}?subject=Feed-Error&body=Error on URL:$url' style=\"display:inline;text-decoration:underline\">{$_SETT['contactemail']}</a> <br>and notify me of the URL you are trying.";
			$invalidID = "I was doing fine. Noone was trying to murder me, or put me in a potato, or feed me to birds.";
			} //END if: itunes-u
		} //END if: validID
	} //END if: cache_file
else {
 
	$feedURL = $cache_file;
	if (strpos($feedURL, '<b>Empty podcast.</b>')) {
 
		$invalidID = TRUE;
		$feedURL_explode = explode("\n", $feedURL);
		if (time() > (int)trim(end($feedURL_explode))) {
 
			$delete_table_query = "DELETE FROM {$_SETT['dbtable']} WHERE uniqueid='$urlhash'";
			@sqlite_exec($database, $delete_table_query);
			header("Location: " . $_SERVER["SCRIPT_URI"] . "?" . $_SERVER["QUERY_STRING"]);
			exit;
			} //END if: time
		else {
 
			$feedURL = implode("\n", explode("\n", $feedURL, -1));
			} //END if: time
		} //END if: empty podcast
	elseif (stripos($feedURL, 'itunes-u')) {
 
		$invalidID = 'itunes-u';
		} //END elseif: itunes-u
	else {
 
		checkFEED($feedURL, $url, TRUE);
		} //END if: empty podcast
	} //END if: cache_file
 
//-----------------------------------------
// html output to browser
$podcastURL = (isset($invalidURL) || isset($invalidID)) ? ($invalidID == 'empty_podcast' ? $emptyPodcastText : $feedURL) : "<a href=\"$feedURL\">$feedURL</a>";
 
echo <<<OUTTEXT
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
         "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd"> 
 
<html xmlns="http://www.w3.org/1999/xhtml" style="height: 100%;"> 
  <head> 
    <title>Podcast URL</title> 
    <meta name="viewport" content="width=320; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;"/> 
    <meta name="robots" content="NOINDEX, NOFOLLOW, NOARCHIVE, NOSNIPPET"> 
    <style type="text/css" media="screen">@import "iphonenav.css";</style>
    <link rel="apple-touch-icon" href="./icon.png" />
    <link rel="shortcut icon" href="./favicon.ico">
  </head> 
  
  <!-- GUI, iTunes Store searching & feed extraction credited to lasavior, so-nik.com --> 
  <!-- Original idea & inspiration credited to Michael Sitarzewski, zerologic.com --> 
  <!-- For the PHP code to extract the podcast feed yourself, visit http://snipplr.com/view/52465 --> 
   
  <body style="height: 100%;"> 
     
    <h1>Podcast URL:</h1> 
    <ul> 
        <li style="height: 100%; padding:10px 10px 10px 10px; font-size:20px; word-wrap:break-word">$podcastURL</li> 
        <li style="height: 100%; padding:10px 10px 10px 10px; font-size:10px; border-bottom: 0px solid;"><div style="color:LightGray;font-size:12px;"><a href="mailto:ife@so-nik.com" style="color:LightGray; font-size:12px; text-decoration:none;">ife@so-nik.com</a>&nbsp;&nbsp;&nbsp;/&nbsp;&nbsp;&nbsp;<a href="http://podcasturlextractor.blogspot.com/" style="color:LightGray; font-size:12px; text-decoration:none;" target="_blank">News</a>&nbsp;&nbsp;&nbsp;/&nbsp;&nbsp;&nbsp;<a href="http://podcasturlextractor.blogspot.com/p/what-it-is-and-why-its-here.html" style="color:LightGray; font-size:12px; text-decoration:none;" target="_blank">About</a></div></li>
    </ul> 
  </body> 
</html>
OUTTEXT;
 
?>