<?php
require_once('hb-runtime-version.php');

$version = getHBRuntimeFilename();



if (isset($_REQUEST['version']))
{
	echo $version;
}
else
{
	header("Location: http://www.happybrackets.net/downloads/$version");
}

?>
