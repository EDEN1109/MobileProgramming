<?php

 error_reporting(E_ALL); 
  ini_set('display_errors',1); 

  include('dbcon.php');
   
   $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {
   
        $id =$_POST['id'];
		$num = $_POST['num'];
		
		$sql = "select uid from board where num = $num";
		$stmt = $con->prepare($sql);
		$stmt->execute();
 
		$row=$stmt->fetch(PDO::FETCH_ASSOC);
		$uid = $row["uid"];
		if($uid == $id){
			$sql = "delete from board where num = $num";
			$stmt = $con->prepare($sql);
		
	   		if($stmt->execute())
	   	     {
	   	         echo "주최자 success";
	   	     }
	   	     else
	   	     {
					echo "주최자 fail";
	   	     }
		}else{
			$sql = "delete from participate where id = '$id' and num = $num";
			$stmt = $con->prepare($sql);
		
	   		if($stmt->execute())
	   	     {
	   	         echo "cancel success";
	   	     }
	   	     else
	   	     {
					echo "cancel fail";
	   	     }
				
				$sql="select egroup from board where num = $num";
				$stmt = $con->prepare($sql);
				$stmt->execute();
	 	
				$row=$stmt->fetch(PDO::FETCH_ASSOC);
				$egroup = $row["egroup"];
				$egroup = $egroup - 1;
				
				$sql="update board set egroup=$egroup where num=$num";
				$stmt = $con->prepare($sql);
					
				if($stmt->execute())
	   	     {
					echo "update success";
				}
	   	     else
	   	     {
					echo "update fail";
	   	     }
				}
			

	}
?>