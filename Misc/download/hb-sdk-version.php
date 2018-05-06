<?php

function getHBVersion()
{
	return file_get_contents ("hb-sdk-version.txt");
}



function getHappyBracketsSDKPrefix()
{
	return "HappyBracketsDeveloperKit-";
}

function getHBSDKFilename()
{
	return getHappyBracketsSDKPrefix() . getHBVersion() . ".zip";
}

?>
