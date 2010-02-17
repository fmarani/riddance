<?php

define("PATH_TO_TAOBASE","/home/flagz/lavoro/phptests/taobase/classes");

require_once PATH_TO_TAOBASE.'/messaging/Client.php';
require_once PATH_TO_TAOBASE.'/messaging/Message.php';
require_once PATH_TO_TAOBASE.'/messaging/Destination.php';
require_once PATH_TO_TAOBASE.'/messaging/destination/Queue.php';
require_once PATH_TO_TAOBASE.'/messaging/destination/Factory.php';
require_once PATH_TO_TAOBASE.'/messaging/message/TextMessage.php';
require_once PATH_TO_TAOBASE.'/messaging/message/Factory.php';

require_once dirname(__FILE__).'/../../main/php/Controller.php';


class riddance_TestController extends PHPUnit_Framework_TestCase
{
	private $riddance;

	public function setUp()
	{
		$this->riddance = new riddance_Controller;
		$this->riddance->connect();
	}

	public function testRiddance()
	{
		$uow = new riddance_UnitOfWork;
		$uow->templateText = "Dear {name}, you received {object}. bye!";
		$uow->templateHtml = "<html><body>Dear <b>{name}</b>, <br/> you received {object}. bye!</body></html>";
		$uow->subject = "a very important email";
		$uow->emails = array("flagzeta@gmail.com","flagzeta@yahoo.it","flagz@localhost");
		$uow->templateVars = array(
				array(
					"name" => "flagzgmail",
					"object" => "ironboard"
				     ),
				array(
					"name" => "flagzyahoo",
					"object" => "surfboard"
				     ),
				array(
					"name" => "flagzlocal",
					"object" => "snowboard"
				     ));
		$this->riddance->release($uow);
	}

	public function tearDown()
	{
		$this->riddance->disconnect();
	}
}
