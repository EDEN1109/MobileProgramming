<?php  
error_reporting(E_ALL); 
ini_set('display_errors',1); 

include('dbcon.php');

$id = $_POST["id"];

$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android ){
	$sql="select * from user where id = '$id'";
	$stmt = $con->prepare($sql);
	$stmt->execute();
 
	if($stmt->rowCount() == 0){
		echo "itrue";
	}
	else{
		echo "ifalse";	
	}
}	
?>