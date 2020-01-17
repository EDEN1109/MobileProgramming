<?php  
header('Content-Type: application/json; charset=utf8');
       
error_reporting(E_ALL); 
ini_set('display_errors',1); 

include('dbcon.php');


$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

    $sql="select * from board order by num desc";
    $stmt = $con->prepare($sql);
    $stmt->execute();
 
  
         $data = array(); 

        while($row=$stmt->fetch(PDO::FETCH_ASSOC)){

           extract($row);

            array_push($data, 
                array('num'=>$row["num"],
            'title'=>$row["title"],
            'ddate'=>$row["ddate"],
            'edate'=>$row["edate"],
            'egroup'=>$row["egroup"],
            'mgroup'=>$row["mgroup"],
            'orgPrice'=>$row["orgPrice"],
            'gbPrice'=>$row["gbPrice"],
            'image'=>$row["image"],
            'way'=>$row["way"],
            'description'=>$row["description"],
            'uid'=>$row["uid"],
			'address'=>$row["address"],
			'maddress'=>$row["maddress"],
			'orgWeight'=>$row["orgWeight"],
			'gbWeight'=>$row["gbWeight"],
			'danwi'=>$row["danwi"]
            ));
        }


      
       header('Content-Type: application/json; charset=utf8');
       
       $json_string = prettyPrint(json_encode(array("webnautes"=>$data)));
    
       echo $json_string;
      
 function prettyPrint( $json )
{

    $result = '';

    $level = 0;

    $in_quotes = false;

    $in_escape = false;

    $ends_line_level = NULL;

    $json_length = strlen( $json );



    for( $i = 0; $i < $json_length; $i++ ) {

        $char = $json[$i];

        $new_line_level = NULL;

        $post = "";

        if( $ends_line_level !== NULL ) {

            $new_line_level = $ends_line_level;

            $ends_line_level = NULL;

        }

        if ( $in_escape ) {

            $in_escape = false;

        } else if( $char === '"' ) {

            $in_quotes = !$in_quotes;

        } else if( ! $in_quotes ) {

            switch( $char ) {

                case '}': case ']':

                    $level--;

                    $ends_line_level = NULL;

                    $new_line_level = $level;

                    break;



                case '{': case '[':

                    $level++;

                case ',':

                    $ends_line_level = $level;

                    break;



                case ':':

                    $post = " ";

                    break;



                case " ": case "\t": case "\n": case "\r":

                    $char = "";

                    $ends_line_level = $new_line_level;

                    $new_line_level = NULL;

                    break;

            }

        } else if ( $char  === '/' ) {

            $in_escape = true;

        }

        if( $new_line_level !== NULL ) {

            $result .= "\n".str_repeat( "\t", $new_line_level );

        }

        $result .= $char.$post;

    }

    return $result;

}

?>



<?php

$android = strpos($_SERVER['HTTP_USER_AGENT'], "Android");

if (!$android){
?>

<?php
}

   
?>