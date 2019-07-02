<?php

require("DBInfo.inc");

$query = "select following_txt from following where user_id='".$_GET['user_id']."' and following_user_id = '".$_GET['following_user_id']."'";

$result = mysqli_query($connect,$query);

if(!$result){
	die('Error cannot run query');
}
$userInfo = array();
while($row=mysqli_fetch_assoc($result)){
	$userInfo[] = $row;
	break;
}
if($userInfo){
	print("{'msg':'is subscribed','info':'". json_encode($userInfo) ."'}");
}else{
	print("{'msg':'not subscribed'}");
}
mysqli_free_result($result);
mysqli_close($connect);

?>