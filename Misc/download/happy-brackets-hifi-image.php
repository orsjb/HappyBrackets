<?php
require_once('hb-hifi-image-version.php');

$version = getHBImageFilename();



if (isset($_REQUEST['version']))
{
	echo $version;
}
else
{
	header("Location: http://www.happybrackets.net/downloads/$version");
}

?>
