<?php

require("DBInfo.inc");

if ($_GET['op']==1) {
$query = "select * from login where email='".$_GET['email']."' and password = '".$_GET['password']."'";
}elseif ($_GET['op']==2) {//search user by search word
		$query ="select * from login where first_name like  '%". $_GET['query'] ."%' LIMIT 20 OFFSET ". $_GET['StartFrom']."" ;
}

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
	print("{'msg':'pass login','info':'". json_encode($userInfo) ."'}");
}else{
	print("{'msg':'cannot login'}");
}
mysqli_free_result($result);
mysqli_close($connect);

?>