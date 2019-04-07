<?php

function GetHBVersion()
{
	return file_get_contents ("hb-hifi-image-version.txt");
}


function GetHappyBracketsImagePrefix()
{
	return "happy-brackets-";
}


function getHBImageFilename()
{
	return GetHappyBracketsImagePrefix() . getHBVersion() . "-hifi.zip";
}

?>
