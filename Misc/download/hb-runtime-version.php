<?php

function getHBVersion()
{
	return file_get_contents ("hb-runtime-version.txt");
}



function getHappyBracketsRuntimePrefix()
{
	return "HappyBracketsDeviceRuntime-";
}

function getHBRuntimeFilename()
{
	return getHappyBracketsRuntimePrefix() . getHBVersion() . ".zip";
}

?>
