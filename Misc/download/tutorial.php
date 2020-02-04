<?php

 // environment setup http://www.happybrackets.net/downloads/tutorial.php?view=setup   
 //fundamental compositions http://www.happybrackets.net/downloads/tutorial.php?view=fundamental
 //advanced configuration http://www.happybrackets.net/downloads/tutorial.php?view=config
 //artworks and performances www.happybrackets.net/downloads/tutorial.php?view=artworks 

$tutorials = array(
    "setup"=> "https://www.youtube.com/watch?v=PrlNEZerKlQ&list=PL5cqHd0DFNFl_EiM3QY08dKNBMjDXnRng",
    "fundamental" => "https://www.youtube.com/watch?v=esj-QLKQ1fw&list=PL5cqHd0DFNFl81V0NagOAsPFx-LnLDOyd",
    "config" => "https://www.youtube.com/watch?v=_rmLC76mixI&list=PL5cqHd0DFNFkNVP-L_kbeK7DJGy5gAQ8A",
    "artworks" => "https://www.youtube.com/watch?v=_wZrVDUnoVE&list=PL5cqHd0DFNFm9x9JGaLDLUwLZZ1H0V8aK",

);


if (isset($_REQUEST['view']))
{
    $selected = $_REQUEST['view'];

    if (isset($tutorials[$selected]))
    {
        $url = $tutorials[$selected]; 
	//echo $url;
        header("Location: $url");
    }
    else
    {
        echo "Tutorial $selected not found";
    }
}
else
{
	echo "No Tutorial selected";
}

?>
