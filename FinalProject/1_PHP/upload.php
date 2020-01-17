<?php
 	$file_path = "./uploads/";

	$file_array = explode(".", strtolower($_FILES['uploaded_file']['name']));
	$file_ext = $file_array[count($file_array)-1];
	unset($file_array[count($file_array)-1]);
	$file_name = implode(".", $file_array);
	$file = $file_name."-".date("YmdHis")."-1.".$file_ext;
	$upload_file = $file_path . $file;
    if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $upload_file)) {
        echo $file_path . $file;
    } else{
        echo "fail";
    }
?>