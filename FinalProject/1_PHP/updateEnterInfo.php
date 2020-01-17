<?php
	error_reporting(E_ALL); 
	ini_set('display_errors',1); 

    include('dbcon.php');
   
    $id=$_POST['id'];
    $num=$_POST['num'];
	$edate=$_POST['edate'];
	$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");
	$possible = false;
    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {

		$sql="select egroup from board where num = $num";
		$stmt = $con->prepare($sql);
		$stmt->execute();
 
		$row=$stmt->fetch(PDO::FETCH_ASSOC);
		$egroup = $row["egroup"];
		$egroup = $egroup + 1;
		
		
		try{
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

			
            $stmt = $con->prepare('INSERT INTO participate VALUES(:num, :id, :edate)');
               
                $stmt->bindParam(':num', $num);
                $stmt->bindParam(':id', $id);
                $stmt->bindParam(':edate', $edate);
           
                if($stmt->execute())
                {
                    echo "insert success";
                }
                else
                {
					echo "insert fail";
                }

            } catch(PDOException $e) {
                die("Database error: " . $e->getMessage()); 
            }
       }

?>