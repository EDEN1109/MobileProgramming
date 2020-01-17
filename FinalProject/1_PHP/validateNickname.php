<?php  
error_reporting(E_ALL); 
ini_set('display_errors',1); 

include('dbcon.php');

$nickname = $_POST["nickname"];

$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android ){
	$sql="select * from user where nickname = '$nickname'";
	$stmt = $con->prepare($sql);
	$stmt->execute();
 
	if($stmt->rowCount() == 0){
		echo "ntrue";
	}
	else{
		echo "nfalse";	
	}
}	
?>