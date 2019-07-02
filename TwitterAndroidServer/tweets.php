<?php

require("DBInfo.inc");
if($_GET['op']==1){
		$query = "insert into tweets(user_id,tweet_text,tweet_picture) values ('" .$_GET['user_id'] . "','".$_GET['tweet_text']."','".$_GET['tweet_picture']."')";
}
// else if($_GET['op']==2){
// 		$query = "update tweets set tweet_following='".$_GET['tweet_following']."' where user_id='".$_GET['user_id']."'";
// }

// $query = "insert into tweets(user_id,tweet_text,tweet_picture,tweet_following) values ('" .$_GET['user_id'] . "','".$_GET['tweet_text']."','".$_GET['tweet_picture']."','".$_GET['tweet_following']."')";

$result = mysqli_query($connect,$query);

if(!$result){
	$output = "{'msg' : 'fail'}";
}else{
		$output = "{'msg' : 'tweet is added'}";

}

print($output);
mysqli_close($connect);

?>