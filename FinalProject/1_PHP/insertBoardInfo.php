<?php
  error_reporting(E_ALL); 
  ini_set('display_errors',1); 

  include('dbcon.php');
  
  
   
	
	$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

    if( (($_SERVER['REQUEST_METHOD'] == 'POST') && isset($_POST['submit'])) || $android )
    {
		$title=$_POST['title'];
        $ddate=$_POST['ddate'];
        $edate=$_POST['edate'];
        $egroup=$_POST['egroup'];
        $mgroup=$_POST['mgroup'];
        $orgPrice=$_POST['orgPrice'];
        $gbPrice=$_POST['gbPrice'];
        $image=$_POST['image'];
        $way=$_POST['way'];
        $description=$_POST['description'];
        $uid=$_POST['uid'];
		$address=$_POST['address'];
		$maddress=$_POST['maddress'];
   		$orgWeight=$_POST['orgWeight'];
   		$gbWeight=$_POST['gbWeight'];
   		$danwi=$_POST['danwi'];
   
        
   		$stmt = $con->prepare('INSERT INTO board(title, ddate, edate, egroup, mgroup, orgPrice, gbPrice, image, way, description, uid, address, maddress, orgWeight, gbWeight, danwi)
								 VALUES(:title, :ddate, :edate, :egroup, :mgroup, :orgPrice, :gbPrice, :image, :way, :description, :uid, :address, :maddress, :orgWeight, :gbWeight, :danwi)');
		$stmt->bindParam(':title', $title);
		$stmt->bindParam(':ddate', $ddate);
		$stmt->bindParam(':edate', $edate);
		$stmt->bindParam(':egroup', $egroup);
		$stmt->bindParam(':mgroup', $mgroup);
		$stmt->bindParam(':orgPrice', $orgPrice);
		$stmt->bindParam(':gbPrice', $gbPrice);
		$stmt->bindParam(':image', $image);
		$stmt->bindParam(':way', $way);
		$stmt->bindParam(':description', $description);
		$stmt->bindParam(':uid', $uid);
		$stmt->bindParam(':address', $address);
		$stmt->bindParam(':maddress', $maddress);
		$stmt->bindParam(':orgWeight', $orgWeight);
		$stmt->bindParam(':gbWeight', $gbWeight);
		$stmt->bindParam(':danwi', $danwi);
		
		if($stmt->execute()){
			echo "성공";
		}else{
			echo "실패";
		} 

        
		$stmt = $con->prepare('select num from board order by num desc limit 1');
		$stmt->execute();
		$row=$stmt->fetch(PDO::FETCH_ASSOC);
		$num = $row["num"];
		
		$stmt = $con->prepare('INSERT INTO participate VALUES(:num, :id, :edate)');
               
        $stmt->bindParam(':num', $num);
        $stmt->bindParam(':id', $uid);
       	$stmt->bindParam(':edate', $edate);
           
        if($stmt->execute())
        {
            echo "insert success";
        }
        else
        {
			echo "insert fail";
        }  

    }
	

?>