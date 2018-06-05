<?php
require 'vendor/autoload.php';
include_once('vendor/getid3/getid3.php');

const medium = 480;
const low = 360;

function file_download($file){
    if(file_exists($file)){
        header('Content-Description: File Transfer');
        header('Content-Type: application/octet-stream');
        header('Content-Disposition: attachment; filename="'.basename($file).'"');
        header('Expires: 0');
        header('Cache-Control: must-revalidate');
        header('Pragma: public');
        header('Content-Length: ' . filesize($file));
        header("Content-Transfer-Encoding: binary");
        flush(); // Flush system output buffer
        readfile($file);
    }
    //echo "127.0.0.1:8000/".$file;
}

$battery = $_POST['battery'];
$connection = $_POST['connection'];
$url = $_POST['url'];
$getID3 = new getId3;
$file = fopen($url,'rb');
if(!$file){
    echo "gagal";                
}
else{
    file_put_contents('temp',$file);
}
$filename = 'temp';
$fileinfo = $getID3->analyze($filename);


if($battery >= 50 && $connection == 'wifi'){
    $x = $fileinfo['video']['resolution_x'];
}
else if($battery >50 && $connection == 'data'){
    if($fileinfo['video']['resolution_x'] < medium)$x = $fileinfo['video']['resolution_x'];
    else $x = 480;
}
else{
    if($fileinfo['video']['resolution_x'] < low)$x = $fileinfo['video']['resolution_x'];
    else $x = 360; 
}

exec("ffmpeg -i temp -vcodec libx264 -acodec aac -filter:v scale=".$x.":-2 -crf 24 -strict -2 output.mp4");
unlink('temp');
file_download('output.mp4');
//unlink('output.mp4');

die();

?>