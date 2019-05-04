<?php
if( $_POST["fname"] || $_POST["lname"] ) {
    if (preg_match("/[^A-Za-z'-]/",$_POST['name'] )) {
        die ("invalid name and name should be alpha");
    }
    echo "Welcome ". $_POST['fname']. "<br />";
    echo "You are ". $_POST['lname']. " years old.";

    exit();
}
?>