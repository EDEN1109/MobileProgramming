<?php
error_reporting(E_ALL); 
  ini_set('display_errors',1); 

  include('dbcon.php');
   
   $android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {
   
        $id=$_POST['id'];
        $password=$_POST['password'];
        $nickname=$_POST['nickname'];
        $phone=$_POST['phone'];
       
        
            try{
			
            $stmt = $con->prepare('INSERT INTO user VALUES(:id, :password, :nickname, :phone)');
               
                $stmt->bindParam(':id', $id);
                $stmt->bindParam(':password', $password);
                $stmt->bindParam(':nickname', $nickname);
                $stmt->bindParam(':phone', $phone);
           
                if($stmt->execute())
                {
                    $successMSG = "새로운 회원을 추가했습니다.";
                }
                else
                {
                    $errMSG = "회원 추가 에러";
                }

            } catch(PDOException $e) {
                die("Database error: " . $e->getMessage()); 
            }
       

    }

?>

