<?php
require_once('hb-sdk-version.php');

$version = getHBSDKFilename();



if (isset($_REQUEST['version']))
{
	echo $version;
}
else
{
	header("Location: http://www.happybrackets.net/downloads/$version");
}

?>
