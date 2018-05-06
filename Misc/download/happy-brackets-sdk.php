<?php
require_once('hb-sdk-version.php');

$version = getHBImageilename();



if (isset($_REQUEST['version']))
{
	echo $version;
}
else
{
	header("Location: http://www.happybrackets.net/downloads/$version");
}

?>
