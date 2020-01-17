<?php
	error_reporting(E_ALL); 
	ini_set('display_errors',1); 
	
	include('dbcon.php');
	
	$id = $_POST["id"];
	$num = $_POST["num"];
	
	$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
	if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android ){
		
		
		
			$sql="select * from participate where num = $num and id = '$id'";
			$stmt = $con->prepare($sql);
			$stmt->execute();
		 
		 	//particapte possible
			if($stmt->rowCount() == 0){
				$next = true;
				echo "true";
			}
			//participate not possible
			else{
				$next = false;
				echo "false";	
			}
		
		if($next){
			try{
				$sql="select egroup,mgroup from board where num = $num";
				$stmt = $con->prepare($sql);
				$stmt->execute();
	 
				$row=$stmt->fetch(PDO::FETCH_ASSOC);
				$egroup = $row["egroup"];
				$mgroup = $row["mgroup"];
			
				if($egroup >= $mgroup){
					echo "max";
				}else{
				}
			} catch(PDOException $e) {
                die("Database error: " . $e->getMessage()); 
        }	
		}
	}	

?>