<?php

require("DBInfo.inc");
//op=1-follow,op=2-unfollow
if($_GET['op']==1){
		$query = " insert into following (user_id,following_user_id,following_txt) values ('" .$_GET['user_id'] . "','".$_GET['following_user_id']."','" .$_GET['following_txt'] . "')";
}else if($_GET['op']==2){
		$query = "delete from following where user_id= " .$_GET['user_id'] . " and following_user_id= ".$_GET['following_user_id']."";
}

$result = mysqli_query($connect,$query);

if(!$result){
	$output = "{'msg' : 'fail'}";
}else{
		$output = "{'msg' : 'following is updated'}";

}

print($output);
mysqli_close($connect);

?>